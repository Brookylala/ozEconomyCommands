package net.ozanarchy.ozEconomyCommands;

import net.ozanarchy.ozEconomyCommands.commands.AdminEcoCommand;
import net.ozanarchy.ozEconomyCommands.commands.BalanceCommand;
import net.ozanarchy.ozEconomyCommands.commands.BaltopCommand;
import net.ozanarchy.ozEconomyCommands.commands.PayCommand;
import net.ozanarchy.ozEconomyCommands.handlers.BankHandlers;
import net.ozanarchy.ozEconomyCommands.services.BaltopCache;
import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class OzEconomyCommands extends JavaPlugin {
    // Shared economy service provided by ozanarchy-economy.
    public EconomyAPI economyAPI;
    // Shared config reference used by command/helper classes.
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("ozEconomyCommands Enabled");

        config = getConfig();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        economyAPI = Bukkit.getServicesManager().load(EconomyAPI.class);
        if (economyAPI == null) {
            getLogger().severe("Economy API not found! Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        BankHandlers handlers = new BankHandlers(economyAPI);
        // Baltop is served from cache to avoid hitting storage every command execution.
        BaltopCache baltopCache = new BaltopCache(this, economyAPI, config.getInt("baltop.max-entries", 10));
        baltopCache.start(config.getInt("baltop.refresh-seconds", 30));

        getCommand("balance").setExecutor(new BalanceCommand(economyAPI));
        getCommand("pay").setExecutor(new PayCommand(handlers));
        getCommand("baltop").setExecutor(new BaltopCommand(baltopCache));
        getCommand("admineco").setExecutor(new AdminEcoCommand(economyAPI));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("ozEconomyCommands Disabled");
    }
}
