package dev.acecore.common_modal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandsTabList implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 返却する候補のリスト
        List<String> completions = new ArrayList<>();



        if(label.equalsIgnoreCase("end")){
            Collections.emptyList();
        }

        return completions.isEmpty() ? Collections.emptyList() : completions;
    }
}
