package net.ozanarchy.ozanarchyEconomy.handlers;

import net.ozanarchy.ozanarchyEconomy.OzanarchyEconomy;
import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

public class CoinHandler implements EconomyAPI {
    private final OzanarchyEconomy plugin;

    public CoinHandler(OzanarchyEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void add(UUID uuid, double amount) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE users SET coins = coins + ? WHERE UUID = ?";

            try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void remove(UUID uuid, double amount, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE users SET coins = coins - ? WHERE UUID = ? AND coins >= ?";

            boolean success = false;

            try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, uuid.toString());
                stmt.setDouble(3, amount);

                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Return callback on the main thread for Bukkit API safety.
            final boolean operationSuccess = success;
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(operationSuccess));
        });
    }

    @Override
    public void getBalance(UUID uuid, Consumer<Double> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            double balance = 0.0;
            String sql = "SELECT coins FROM users WHERE UUID = ?";

            try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        balance = rs.getDouble("coins");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Return callback on the main thread for Bukkit API safety.
            final double resolvedBalance = balance;
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(resolvedBalance));
        });
    }
}
