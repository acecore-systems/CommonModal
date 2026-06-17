package dev.acecore.commonmodal.listener;

import dev.acecore.commonmodal.api.CommonModalAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * プレイヤー切断時にアクティブなフォームセッションをクリーンアップするリスナ。
 *
 * <p>plugin-spec.md §5.②-1 に基づく。</p>
 */
public final class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        CommonModalAPI.cleanupPlayer(event.getPlayer());
    }
}
