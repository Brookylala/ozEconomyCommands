package net.ozanarchy.ozEconomyCommands.commands;

import net.ozanarchy.ozEconomyCommands.Util;
import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
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

public class AdminEcoCommand implements TabExecutor {
    private final EconomyAPI economyAPI;
    private final String prefix = Util.prefix();

    public AdminEcoCommand(EconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /admineco <add|remove> <player> <amount>");
            return true;
        }

        // Subcommand controls both behavior and permission node.
        String action = args[0].toLowerCase();
        String permission = action.equals("add")
                ? "ozecocommands.admin.add"
                : action.equals("remove") ? "ozecocommands.admin.remove" : null;

        if (permission == null) {
            sender.sendMessage(ChatColor.RED + "Usage: /admineco <add|remove> <player> <amount>");
            return true;
        }

        if (!sender.hasPermission(permission)) {
            sender.sendMessage(Util.getColor(prefix + config.getString("messages.no-permission")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            sender.sendMessage(Util.getColor(prefix + config.getString("messages.invalid-player")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Util.getColor(prefix + config.getString("messages.invalid-amount")));
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(Util.getColor(prefix + config.getString("messages.invalid-amount")));
            return true;
        }

        String targetName = target.getName() == null ? "Unknown" : target.getName();

        if (action.equals("add")) {
            // Add is a direct write operation.
            economyAPI.add(target.getUniqueId(), amount);
            sender.sendMessage(Util.getColor(prefix + "&aAdded &e" + amount + " &ato &f" + targetName));
            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(Util.getColor(prefix + "&aAn admin added &e" + amount + " &ato your balance."));
            }
            return true;
        }

        // Remove validates through API callback (e.g. not enough funds).
        economyAPI.remove(target.getUniqueId(), amount, success -> {
            if (!success) {
                sender.sendMessage(Util.getColor(prefix + config.getString("messages.not-enough-money")));
                return;
            }

            sender.sendMessage(Util.getColor(prefix + "&aRemoved &e" + amount + " &afrom &f" + targetName));
            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(Util.getColor(prefix + "&cAn admin removed &e" + amount + " &cfrom your balance."));
            }
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Only suggest actions the sender can actually run.
            List<String> actions = new ArrayList<>();
            if (sender.hasPermission("ozecocommands.admin.add")) {
                actions.add("add");
            }
            if (sender.hasPermission("ozecocommands.admin.remove")) {
                actions.add("remove");
            }
            return StringUtil.copyPartialMatches(args[0], actions, new ArrayList<>());
        }

        if (args.length == 2) {
            // Suggest online players for admin target selection.
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return StringUtil.copyPartialMatches(args[1], names, new ArrayList<>());
        }

        if (args.length == 3) {
            List<String> amounts = List.of("1", "10", "100", "1000");
            return StringUtil.copyPartialMatches(args[2], amounts, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
