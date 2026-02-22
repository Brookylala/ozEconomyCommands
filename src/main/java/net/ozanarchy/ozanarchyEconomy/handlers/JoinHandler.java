package net.ozanarchy.ozanarchyEconomy.handlers;

import net.ozanarchy.ozanarchyEconomy.OzanarchyEconomy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class JoinHandler {
    private final OzanarchyEconomy plugin;

    public JoinHandler(OzanarchyEconomy plugin) {
        this.plugin = plugin;
    }

    // Creates a user row if missing; no-op if the UUID already exists.
    public void generateUser(UUID uuid, String name) {
        String sql = """
                INSERT INTO users (UUID, username)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE UUID = UUID
                """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
