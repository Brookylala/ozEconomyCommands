package net.ozanarchy.ozEconomyCommands;

import org.bukkit.ChatColor;

import static net.ozanarchy.ozEconomyCommands.OzEconomyCommands.config;

public class Util {
    // Applies legacy '&' color codes and safely handles null text.
    public static String getColor(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // Centralized prefix lookup so command classes stay consistent.
    public static String prefix() {
        return config.getString("prefix", "");
    }
}
