package net.ozanarchy.ozanarchyEconomy.events;

import net.ozanarchy.ozanarchyEconomy.OzanarchyEconomy;
import net.ozanarchy.ozanarchyEconomy.handlers.JoinHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {
    private final OzanarchyEconomy plugin;
    private final JoinHandler joinHandler;

    public JoinEvent(OzanarchyEconomy plugin, JoinHandler joinHandler) {
        this.plugin = plugin;
        this.joinHandler = joinHandler;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onConnection(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Run DB work async to avoid blocking the main server thread.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            joinHandler.generateUser(player.getUniqueId(), player.getName());
        });
    }
}
