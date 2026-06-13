package dev.acecore.commonModal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーからのみ実行できます");
            return true;
        }
        Player player = (Player) sender;
        String cmdName = command.getName().toLowerCase();

        switch (cmdName){
            case "コマンド":
        }


        return true;
    }
}