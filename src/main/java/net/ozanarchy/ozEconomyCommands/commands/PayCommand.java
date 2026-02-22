package net.ozanarchy.ozEconomyCommands.commands;

import net.ozanarchy.ozEconomyCommands.Util;
import net.ozanarchy.ozEconomyCommands.handlers.BankHandlers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class PayCommand implements TabExecutor {
    private final BankHandlers handlers;
    private final String prefix = Util.prefix();

    public PayCommand(BankHandlers handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player p)) {
            return true;
        }

        if (!p.hasPermission("ozecocommands.pay")) {
            p.sendMessage(Util.getColor(prefix + config.getString("messages.no-permission")));
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }

        // Pay only supports online targets to guarantee immediate delivery feedback.
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getPlayer() == null) {
            p.sendMessage(Util.getColor(prefix + config.getString("messages.invalid-player")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            p.sendMessage(Util.getColor(prefix + config.getString("messages.invalid-amount")));
            return true;
        }

        if (amount <= 0) {
            p.sendMessage(Util.getColor(prefix + config.getString("messages.invalid-amount")));
            return true;
        }

        handlers.pay(p, target.getPlayer(), amount);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest currently online players for quick targeting.
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
        }

        if (args.length == 2) {
            // Simple starter values for amount entry.
            List<String> amounts = List.of("1", "10", "100", "1000");
            return StringUtil.copyPartialMatches(args[1], amounts, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
