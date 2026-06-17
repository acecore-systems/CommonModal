package dev.acecore.commonmodal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.acecore.commonmodal.api.CommonModalAPI;
import dev.acecore.commonmodal.listener.PlayerListener;
import dev.acecore.commonmodal.protocol.CommonModalChannels;
import dev.acecore.commonmodal.protocol.FormCodec;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * common-modal-spigot プラグインのエントリポイント。
 *
 * <p>plugin-spec.md §5 / plan.md §3 に基づき、以下を行う。
 * <ul>
 *   <li>{@code commonmodal:form} チャネル (S2C) の登録</li>
 *   <li>{@code commonmodal:response} チャネル (C2S) の受信ハンドラ登録</li>
 *   <li>プレイヤー切断時のセッションクリーンアップリスナ登録</li>
 * </ul>
 */
public final class CommonModalPlugin extends JavaPlugin implements PluginMessageListener {

    @Override
    public void onEnable() {
        CommonModalAPI.initialize(this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, CommonModalChannels.FORM);
        getServer().getMessenger().registerIncomingPluginChannel(this, CommonModalChannels.RESPONSE, this);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);

        getLogger().info("commonModal enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        CommonModalAPI.shutdown();

        getServer().getMessenger().unregisterOutgoingPluginChannel(this, CommonModalChannels.FORM);
        getServer().getMessenger().unregisterIncomingPluginChannel(this, CommonModalChannels.RESPONSE, this);

        getLogger().info("commonModal disabled.");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CommonModalChannels.RESPONSE.equals(channel)) {
            return;
        }

        try {
            String json = new String(message, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            int id = root.get("id").getAsInt();
            JsonElement valueElement = root.get("value");
            Object value = FormCodec.decodeResponseValue(valueElement);
            CommonModalAPI.handleResponse(player, id, value);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to decode commonmodal:response: " + e.getMessage(), e);
        }
    }
}
