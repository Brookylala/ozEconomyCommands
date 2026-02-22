package net.ozanarchy.ozEconomyCommands.services;

import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BaltopCache {
    private final JavaPlugin plugin;
    private final EconomyAPI economyAPI;
    private final int maxEntries;
    private volatile List<Entry> cachedTop = List.of();
    private volatile long lastRefreshMillis = 0L;

    public BaltopCache(JavaPlugin plugin, EconomyAPI economyAPI, int maxEntries) {
        this.plugin = plugin;
        this.economyAPI = economyAPI;
        this.maxEntries = maxEntries;
    }

    public void start(int refreshSeconds) {
        // Protect against overly aggressive refresh values in config.
        int safeSeconds = Math.max(5, refreshSeconds);
        long refreshTicks = safeSeconds * 20L;
        // Prime cache immediately so first /baltop call has data sooner.
        refresh();
        Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, refreshTicks, refreshTicks);
    }

    public List<Entry> getCachedTop() {
        return cachedTop;
    }

    public long getLastRefreshMillis() {
        return lastRefreshMillis;
    }

    public void refresh() {
        // Current source is Bukkit known players (online + seen offline players).
        OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        if (players.length == 0) {
            cachedTop = List.of();
            lastRefreshMillis = System.currentTimeMillis();
            return;
        }

        ConcurrentMap<UUID, Double> balances = new ConcurrentHashMap<>();
        AtomicInteger remaining = new AtomicInteger(players.length);

        for (OfflinePlayer player : players) {
            UUID uuid = player.getUniqueId();
            economyAPI.getBalance(uuid, balance -> {
                if (balance != null) {
                    balances.put(uuid, balance);
                }

                if (remaining.decrementAndGet() == 0) {
                    // Build immutable snapshot once all async callbacks are complete.
                    finalizeCache(players, balances);
                }
            });
        }
    }

    private void finalizeCache(OfflinePlayer[] players, ConcurrentMap<UUID, Double> balances) {
        ConcurrentMap<UUID, String> names = new ConcurrentHashMap<>();
        for (OfflinePlayer player : players) {
            String name = player.getName();
            if (name == null || name.isBlank()) {
                name = player.getUniqueId().toString().substring(0, 8);
            }
            names.put(player.getUniqueId(), name);
        }

        List<Entry> top = new ArrayList<>();
        for (var entry : balances.entrySet()) {
            top.add(new Entry(names.getOrDefault(entry.getKey(), entry.getKey().toString().substring(0, 8)), entry.getValue()));
        }

        top.sort(Comparator.comparingDouble(Entry::balance).reversed());
        if (top.size() > maxEntries) {
            top = new ArrayList<>(top.subList(0, maxEntries));
        }

        // Atomic replace keeps readers lock-free and consistent.
        cachedTop = List.copyOf(top);
        lastRefreshMillis = System.currentTimeMillis();
    }

    public record Entry(String name, double balance) {
    }
}
