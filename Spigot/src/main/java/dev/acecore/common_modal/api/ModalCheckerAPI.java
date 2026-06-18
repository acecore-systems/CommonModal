package dev.acecore.common_modal.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * commonModal クライアント導入チェックの結果を保持する API。
 *
 * <p>Spigot 側はプレイヤー参加時に Fabric クライアントへ導入確認を送信し、
 * 応答が返ってきたプレイヤーを {@link #MODAL_PLAYERS} に保存する。</p>
 */
public final class ModalCheckerAPI {

    /** commonModal API バージョン。 */
    public static final int API_VERSION = 1;

    private static final Set<UUID> MODAL_PLAYERS = ConcurrentHashMap.newKeySet();

    private ModalCheckerAPI() {
        throw new UnsupportedOperationException("utility");
    }

    /**
     * 指定プレイヤーを commonModal 導入済みとして登録する。
     *
     * @param player 対象プレイヤー
     */
    public static void addModalPlayer(@NotNull Player player) {
        MODAL_PLAYERS.add(player.getUniqueId());
    }

    /**
     * 指定プレイヤーが commonModal 導入済みかどうかを返す。
     *
     * @param player 対象プレイヤー
     * @return commonModal 導入済みの場合 {@code true}
     */
    public static boolean isModalPlayer(@NotNull Player player) {
        return MODAL_PLAYERS.contains(player.getUniqueId());
    }

    /**
     * 登録済みの commonModal 導入プレイヤー UUID を読み取り専用セットで返す。
     *
     * @return 導入プレイヤー UUID のセット
     */
    public static Set<UUID> getModalPlayers() {
        return Collections.unmodifiableSet(MODAL_PLAYERS);
    }

    /**
     * 指定プレイヤーの導入状態を削除する。
     *
     * @param player 対象プレイヤー
     */
    public static void removeModalPlayer(@NotNull Player player) {
        MODAL_PLAYERS.remove(player.getUniqueId());
    }

    /** すべての導入状態をクリアする。 */
    public static void clear() {
        MODAL_PLAYERS.clear();
    }
}
