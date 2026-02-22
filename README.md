# Ozanarchy Economy

Simple Paper/Spigot economy provider backed by MySQL.

## What this plugin provides

- Automatic user creation on player join
- MySQL-backed coin balances (`users` table)
- Public `EconomyAPI` service registered with Bukkit `ServicesManager`
- Async SQL execution with callbacks returned on the main thread

## Database schema

The plugin ensures this table exists on startup:

```sql
CREATE TABLE IF NOT EXISTS users (
  UUID VARCHAR(36) PRIMARY KEY,
  username VARCHAR(16) NOT NULL,
  coins DOUBLE DEFAULT 0.0
);
```

## Configuration

Set your database values in `config.yml`:

```yml
mysql:
  host: localhost
  port: 3306
  username: root
  password: your_password
  database: ozanarchy
```

## Developer integration

### 1. Declare dependency in your plugin

In your `plugin.yml`:

```yml
depend: [OzanarchyEconomy]
```

Use `softdepend` instead if your plugin can run without economy.

### 2. Resolve the API from ServicesManager

```java
import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import org.bukkit.plugin.RegisteredServiceProvider;

public EconomyAPI getEconomyApi() {
    RegisteredServiceProvider<EconomyAPI> rsp =
            getServer().getServicesManager().getRegistration(EconomyAPI.class);
    return rsp != null ? rsp.getProvider() : null;
}
```

### 3. Use the API methods

```java
EconomyAPI economy = getEconomyApi();
if (economy == null) {
    getLogger().warning("OzanarchyEconomy service not found.");
    return;
}

// Add coins
economy.add(player.getUniqueId(), 25.0);

// Remove coins (callback returns success/failure)
economy.remove(player.getUniqueId(), 10.0, success -> {
    if (success) {
        player.sendMessage("Purchase successful.");
    } else {
        player.sendMessage("Not enough coins.");
    }
});

// Read balance
economy.getBalance(player, balance -> {
    player.sendMessage("Balance: " + balance);
});
```

## Notes for integrators

- Do not assume synchronous results from `getBalance` or `remove`.
- Always use the callback for follow-up logic.
- Handle `null` when the service is not registered.
