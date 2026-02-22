package net.ozanarchy.ozEconomyCommands.commands;

import net.ozanarchy.ozEconomyCommands.Util;
import net.ozanarchy.ozEconomyCommands.services.BaltopCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static net.ozanarchy.ozEconomyCommands.OzEconomyCommands.config;

public class BaltopCommand implements TabExecutor {
    private final BaltopCache baltopCache;
    private final String prefix = Util.prefix();

    public BaltopCommand(BaltopCache baltopCache) {
        this.baltopCache = baltopCache;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("ozecocommands.baltop")) {
            player.sendMessage(Util.getColor(prefix + config.getString("messages.no-permission")));
            return true;
        }

        // Served from periodically refreshed cache (no live DB request here).
        List<BaltopCache.Entry> top = baltopCache.getCachedTop();
        if (top.isEmpty()) {
            player.sendMessage(Util.getColor(prefix + config.getString("messages.baltop-empty", "&cNo balance data available yet.")));
            return true;
        }

        String header = config.getString("messages.baltop-header", "&6Top Balances:");
        player.sendMessage(Util.getColor(prefix + header));

        int rank = 1;
        for (BaltopCache.Entry entry : top) {
            String line = config.getString("messages.baltop-line", "&e#${rank} &f${player} &7- &a${amount}")
                    .replace("${rank}", String.valueOf(rank))
                    .replace("${player}", entry.name())
                    .replace("${amount}", String.valueOf(entry.balance()));
            player.sendMessage(Util.getColor(line));
            rank++;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
