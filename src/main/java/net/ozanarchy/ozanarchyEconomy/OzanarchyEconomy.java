package net.ozanarchy.ozanarchyEconomy;

import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import net.ozanarchy.ozanarchyEconomy.events.JoinEvent;
import net.ozanarchy.ozanarchyEconomy.handlers.CoinHandler;
import net.ozanarchy.ozanarchyEconomy.handlers.JoinHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class OzanarchyEconomy extends JavaPlugin {
    private Connection connection;
    public String host, database, username, password;
    public int port;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        // Load config defaults on first boot.
        config = getConfig();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Connect to MySQL and ensure required tables exist.
        setupMySql();
        createTables();

        // Register join listener that creates user rows lazily.
        JoinHandler joinHandler = new JoinHandler(this);
        getServer().getPluginManager().registerEvents(new JoinEvent(this, joinHandler), this);

        // Expose the economy service for other plugins.
        CoinHandler coinHandler = new CoinHandler(this);
        getServer().getServicesManager().register(
                EconomyAPI.class,
                coinHandler,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void setupMySql() {
        host = config.getString("mysql.host");
        port = config.getInt("mysql.port");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
        database = config.getString("mysql.database");

        try {
            connectMySql();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Lazily connects or reconnects to MySQL when the connection is unavailable.
    private synchronized void connectMySql() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database
                + "?useSSL=false&allowPublicKeyRetrieval=true&tcpKeepAlive=true&connectTimeout=5000&socketTimeout=10000";
        setConnection(DriverManager.getConnection(url, this.username, this.password));
        getLogger().info("MYSQL Connected Successfully");
    }

    public void createTables() {
        try (Statement stmt = getConnection().createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                         "UUID VARCHAR(36) PRIMARY KEY," +
                         "username VARCHAR(16) NOT NULL," +
                         "coins DOUBLE DEFAULT 0.0" +
                         ")";
            stmt.executeUpdate(sql);
            getLogger().info("Tables checked/created successfully.");
        } catch (SQLException e) {
            getLogger().severe("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ignored) {
                    }
                }
                connection = null;
                connectMySql();
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to establish MySQL connection", e);
        }
        return connection;
    }

    private void setConnection(Connection connection) {
        this.connection = connection;
    }
}
