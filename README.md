# SkyMines
## Description
* Provides the ability to apply specific perks to new players.

## Features
* New players can be made invulnerable, given access to /fly and /is fly, given keep inventory, keep experience, and void teleport (VoidTeleport addon).

## Dependencies
* LuckPerms
* SkyLib

## Soft Dependencies
* Essentials
* IslandFly Addon
* VoidTeleport Addon

## Commands
- /newplayerperks reload - Command to reload the plugin
- /newplayerperks add <player_name> - Command to apply new player perks to a player.
- /newplayerperks remove <player_name> - Command to remove new player perks from a player.

## Permisisons
- `skymines.commands.skymines` - The permission to access the /skymines command.
- `skymines.commands.skymines.reload` - The permission to access /skymines reload.
- `skymines.commands.skymines.help` - The permission to access /skymines help.
- `skymines.commands.skymines.time` - The permission to access /skymines time.
- `skymines.commands.skymines.add` - The permission to access /skymines add.
- `skymines.commands.skymines.remove` - The permission to access /skymines remove.
- `skymines.commands.skymines.set` - The permission to access /skymines set.

## Issues, Bugs, or Suggestions
* Please create a new [Github Issue](https://github.com/lukesky19/NewPlayerPerks/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyShop and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.

## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, and 1.21.7.

Q: Are there any plans to support any other versions?

A: I will always do my best to support the latest versions of the game. I will sometimes support other versions until I no longer use them.

Q: Does this work on Spigot? Paper? (Insert other server software here)?

A: I only support Paper, but this will likely also work on forks of Paper (untested). There are no plans to support any other server software (i.e., Spigot or Folia).

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
