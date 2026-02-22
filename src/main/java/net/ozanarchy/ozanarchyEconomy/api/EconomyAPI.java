package net.ozanarchy.ozanarchyEconomy.api;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Public economy service exposed through Bukkit's ServicesManager.
 *
 * All callback methods are expected to be delivered on the main thread by the implementation.
 */
public interface EconomyAPI {
    /**
     * Adds coins to the given account asynchronously.
     */
    void add(UUID uuid, double amount);

    /**
     * Removes coins if balance is sufficient.
     *
     * Callback receives true on success, false on insufficient funds or SQL error.
     */
    void remove(UUID uuid, double amount, Consumer<Boolean> callback);

    /**
     * Resolves the balance asynchronously and returns it via callback.
     */
    void getBalance(UUID uuid, Consumer<Double> callback);

    default void getBalance(Player player, Consumer<Double> callback) {
        getBalance(player.getUniqueId(), callback);
    }
}
