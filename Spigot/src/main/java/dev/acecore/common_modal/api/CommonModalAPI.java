package dev.acecore.common_modal.api;

import dev.acecore.common_modal.api.form.CustomForm;
import dev.acecore.common_modal.api.form.Form;
import dev.acecore.common_modal.api.form.ModalForm;
import dev.acecore.common_modal.api.form.SimpleForm;
import dev.acecore.common_modal.protocol.CommonModalChannels;
import dev.acecore.common_modal.protocol.FormCodec;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * commonModal Spigot プラグインの統制 API。
 *
 * <p>plugin-spec.md §5 / plan.md §3 に基づき、以下を行う。
 * <ul>
 *   <li>フォーム ID 割り当てと、プレイヤーごとのアクティブフォームセッション管理</li>
 *   <li>{@code commonmodal:form} パケットの送信</li>
 *   <li>{@code commonmodal:response} パケット受信時のコールバック実行</li>
 *   <li>ログアウト/タイムアウトによるセッションクリーンアップ</li>
 * </ul>
 */
public final class CommonModalAPI {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, ActiveFormSession>> ACTIVE_FORMS =
            new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private static JavaPlugin plugin;

    private CommonModalAPI() {
        throw new UnsupportedOperationException("utility");
    }

    /** プラグイン初期化時に 1 度だけ呼ぶ。 */
    public static void initialize(@NotNull JavaPlugin plugin) {
        CommonModalAPI.plugin = plugin;
        startCleanupTask();
    }

    /** プラグイン無効化時に呼ぶ。 */
    public static void shutdown() {
        ACTIVE_FORMS.clear();
        plugin = null;
    }

    /**
     * フォームを指定プレイヤーに送信する。
     *
     * <p>各 {@link Form} 実装の {@link Form#send(Player)} から内部で呼ばれる。</p>
     *
     * @param player 対象プレイヤー
     * @param form   送信するフォーム
     */
    public static void sendForm(@NotNull Player player, @NotNull Form form) {
        if (plugin == null) {
            throw new IllegalStateException("CommonModalAPI is not initialized");
        }

        int formId = ID_COUNTER.getAndIncrement();
        ActiveFormSession session = new ActiveFormSession(formId, form, System.currentTimeMillis());
        ACTIVE_FORMS.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>()).put(formId, session);

        String json = FormCodec.encodeForm(formId, form);

        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();

        writeVarInt(out, jsonBytes.length);
        out.write(jsonBytes);

        player.sendPluginMessage(plugin, CommonModalChannels.FORM, out.toByteArray());
    }
    /** Minecraft仕様の VarInt を ByteArrayDataOutput に書き込むヘルパー */
    private static void writeVarInt(com.google.common.io.ByteArrayDataOutput out, int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    /**
     * クライアントからの応答を処理する。
     *
     * @param player       応答元プレイヤー
     * @param formId       フォーム ID
     * @param responseValue 応答値。キャンセル時は {@code null}
     */
    public static void handleResponse(@NotNull Player player, int formId, Object responseValue) {
        ConcurrentHashMap<Integer, ActiveFormSession> playerSessions = ACTIVE_FORMS.get(player.getUniqueId());
        if (playerSessions == null) {
            return;
        }

        ActiveFormSession session = playerSessions.remove(formId);
        if (session == null) {
            return;
        }

        if (playerSessions.isEmpty()) {
            ACTIVE_FORMS.remove(player.getUniqueId());
        }

        session.triggerCallback(player, responseValue);
    }

    /** 指定プレイヤーのアクティブなフォームセッションを全て破棄する。 */
    public static void cleanupPlayer(@NotNull Player player) {
        ACTIVE_FORMS.remove(player.getUniqueId());
    }

    private static void startCleanupTask() {
        if (plugin == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, CommonModalAPI::cleanupExpiredSessions,
                20L * 60, 20L * 60);
    }

    private static void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        ACTIVE_FORMS.values().forEach(sessions ->
                sessions.entrySet().removeIf(entry -> now - entry.getValue().createdAt() > DEFAULT_TIMEOUT_MILLIS)
        );
        ACTIVE_FORMS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /** アクティブフォームセッション。 */
    record ActiveFormSession(int formId, Form form, long createdAt) {

        void triggerCallback(Player player, Object value) {
            boolean closed = value == null;
            switch (form) {
                case ModalForm modal -> {
                    if (closed) {
                        modal.getClosedHandler().accept(player);
                    } else if (value instanceof Boolean booleanValue) {
                        modal.getResultHandler().accept(player, booleanValue);
                    }
                }
                case SimpleForm simple -> {
                    if (closed) {
                        simple.getClosedHandler().accept(player);
                    } else if (value instanceof Number numberValue) {
                        simple.getResultHandler().accept(player, numberValue.intValue());
                    }
                }
                case CustomForm custom -> {
                    if (closed) {
                        custom.getClosedHandler().accept(player);
                    } else if (value instanceof List<?> listValue) {
                        @SuppressWarnings("unchecked")
                        List<Object> values = (List<Object>) listValue;
                        custom.getResultHandler().accept(player, values);
                    }
                }
                default -> throw new IllegalArgumentException("Unknown form type: " + form.getType());
            }
        }
    }
}
