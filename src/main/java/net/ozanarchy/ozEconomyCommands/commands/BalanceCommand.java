package net.ozanarchy.ozEconomyCommands.commands;

import net.ozanarchy.ozEconomyCommands.Util;
import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.ozanarchy.ozEconomyCommands.OzEconomyCommands.config;

public class BalanceCommand implements TabExecutor {
    private final EconomyAPI economyAPI;
    private final String prefix = Util.prefix();

    public BalanceCommand(EconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player p)) {
            return true;
        }

        if (!p.hasPermission("ozecocommands.balance")) {
            p.sendMessage(Util.getColor(prefix + config.getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            // No target argument: return sender's own balance.
            economyAPI.getBalance(p.getUniqueId(), success ->
                    p.sendMessage(Util.getColor(prefix + "&aYour balance is &e" + success))
            );
            return true;
        }

        if (!p.hasPermission("ozecocommands.balance.others")) {
            p.sendMessage(Util.getColor(prefix + config.getString("messages.no-permission")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            p.sendMessage(Util.getColor(prefix + config.getString("messages.invalid-player")));
            return true;
        }

        economyAPI.getBalance(target.getUniqueId(), success ->
                p.sendMessage(Util.getColor(prefix + "&a" + (target.getName() == null ? "Unknown" : target.getName()) + "'s balance is &e" + success))
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player p && !p.hasPermission("ozecocommands.balance.others")) {
                return Collections.emptyList();
            }
            // Only suggest online names for predictable completion output.
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
