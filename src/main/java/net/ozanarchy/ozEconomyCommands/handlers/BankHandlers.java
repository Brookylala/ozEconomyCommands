package net.ozanarchy.ozEconomyCommands.handlers;

import net.ozanarchy.ozEconomyCommands.Util;
import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import org.bukkit.entity.Player;

import static net.ozanarchy.ozEconomyCommands.OzEconomyCommands.config;

public class BankHandlers {
    private final EconomyAPI economyAPI;
    private final String prefix = Util.prefix();

    public BankHandlers(EconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
    }

    public void pay(Player sender, Player receiver, double amount) {
        // Remove first; only add to receiver when sender debit succeeded.
        economyAPI.remove(sender.getUniqueId(), amount, success -> {
            if (!success) {
                sender.sendMessage(Util.getColor(prefix + config.getString("messages.not-enough-money")));
            } else {
                economyAPI.add(receiver.getUniqueId(), amount);
                sender.sendMessage(Util.getColor(prefix + config.getString("messages.payed-player")
                        .replace("${player}", receiver.getName())
                        .replace("${amount}", String.valueOf(amount))));
                receiver.sendMessage(Util.getColor(prefix + config.getString("messages.received-money")
                        .replace("${amount}", String.valueOf(amount))
                        .replace("${player}", sender.getName())));
            }
        });
    }
}
