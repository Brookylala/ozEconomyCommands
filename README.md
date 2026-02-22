# ozEconomyCommands

A lightweight Paper/Spigot economy command plugin that hooks into `ozanarchy-economy`.

## Features

- `/balance` for self and other-player balance checks
- `/pay` player-to-player payments
- `/baltop` cached balance leaderboard
- `/admineco add/remove` admin balance management
- Tab completion for all commands

## Requirements

- Java 21
- Paper/Spigot 1.21+
- `ozanarchy-economy` plugin installed and registered as a service

## Commands

| Command | Description | Permission |
|---|---|---|
| `/balance [player]` | Check your balance (or others with permission) | `ozecocommands.balance` (`ozecocommands.balance.others` for target arg) |
| `/pay <player> <amount>` | Pay an online player | `ozecocommands.pay` |
| `/baltop` | Show cached top balances | `ozecocommands.baltop` |
| `/admineco add <player> <amount>` | Add balance to a player | `ozecocommands.admin.add` |
| `/admineco remove <player> <amount>` | Remove balance from a player | `ozecocommands.admin.remove` |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `ozecocommands.balance` | `true` | Allows checking own balance |
| `ozecocommands.balance.others` | `op` | Allows checking other players' balances |
| `ozecocommands.pay` | `true` | Allows paying other players |
| `ozecocommands.baltop` | `true` | Allows viewing balance leaderboard |
| `ozecocommands.admin.add` | `op` | Allows `/admineco add` |
| `ozecocommands.admin.remove` | `op` | Allows `/admineco remove` |

## Configuration

`src/main/resources/config.yml`

```yml
prefix: '&f&l[&c&lOz&e&lEconomy&f&l] '

baltop:
  refresh-seconds: 30
  max-entries: 10

messages:
  no-permission: '&cYou do not have permission to do this!'
  invalid-amount: '&cInvalid amount!'
  invalid-args: '&cInvalid arguments!'
  invalid-player: '&cPlayer not found!'
  payed-player: '&aYou have given &f${player} &e${amount}'
  received-money: '&aYou have received &e${amount} &afrom &f${player} '
  not-enough-money: '&cYou do not have enough money!'
  baltop-empty: '&cNo balance data available yet.'
  baltop-header: '&6Top Balances:'
  baltop-line: '&e#${rank} &f${player} &7- &a${amount}'
```

Notes:
- `baltop.refresh-seconds` controls cache refresh interval.
- `/baltop` reads from cache and does not query per command execution.

## Build

```bash
mvn clean package
```

Artifact:
- `target/ozEconomyCommands-1.0.jar`

## Install

1. Build the jar.
2. Put the jar into your server `plugins/` folder.
3. Ensure `ozanarchy-economy` is installed.
4. Start/restart the server.

