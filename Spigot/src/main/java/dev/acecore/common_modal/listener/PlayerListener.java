package dev.acecore.common_modal.listener;

import dev.acecore.common_modal.api.CommonModalAPI;
import dev.acecore.common_modal.api.ModalCheckerAPI;
import dev.acecore.common_modal.protocol.CommonModalChannels;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * プレイヤー在籍管理とアクティブなフォームセッションをクリーンアップするリスナ。
 *
 * <p>plugin-spec.md §5.②-1 に基づく。</p>
 */
public final class PlayerListener implements Listener {

    private final JavaPlugin plugin;

    public PlayerListener(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String json = "{}";
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();

        writeVarInt(out, jsonBytes.length);
        out.write(jsonBytes);

        player.sendPluginMessage(plugin, CommonModalChannels.CHECK, out.toByteArray());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CommonModalAPI.cleanupPlayer(player);
        ModalCheckerAPI.removeModalPlayer(player);
    }

    /** Minecraft仕様の VarInt を ByteArrayDataOutput に書き込むヘルパー */
    private static void writeVarInt(com.google.common.io.ByteArrayDataOutput out, int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }
}
