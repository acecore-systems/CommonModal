package dev.acecore.commonModal;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public final class CommonModal extends JavaPlugin {
    static Plugin plugin;
    static Server server;
    static String pluginName = "commonModal";
    private static final String FILE_PATH = "./plugins/"+pluginName+"/.env";
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin=this;
        server= Bukkit.getServer();

        reset();

        // イベントハンドラを実装したクラスの登録
        Bukkit.getServer().getPluginManager().registerEvents(new Events(),this);

        //下の行みたいな感じに登録するコマンド一覧を書いておく　候補にちゃんと出るようにresources/plugin.ymlのcommands:のとこに書いておくことを忘れずに!!(コマンド名:　だけを追加すれば大丈夫)
        List<String> commandNames = List.of();
        Commands commonExecutor = new Commands();
        CommandsTabList commonCompleter = new CommandsTabList();
        for (String commandName : commandNames) {
            PluginCommand command = getCommand(commandName);
            if (command != null) {
                command.setExecutor(commonExecutor);
                command.setTabCompleter(commonCompleter);
                Bukkit.getLogger().log(Level.INFO,"["+pluginName+"]:/"+commandName+"を登録しました");
            }

        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    static void reset(){
        // Plugin reset logic
    }
}
