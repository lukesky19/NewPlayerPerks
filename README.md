# NewPlayerPerks
## Description
* Provides the ability to apply specific perks to new players.

## Features
* New players can be made invulnerable, given access to /fly, given access to, /is fly, given keep inventory, keep experience, and void teleport (VoidTeleport addon).

## Dependencies
* LuckPerms
* SkyLib

## Soft Dependencies
* Essentials
* IslandFly Addon
* SkyFlight
* VoidTeleport Addon

## Commands
* /newplayerperks - The base command and the command to prestige an island.
    * Aliases: /npp, /perks, /perk
- /newplayerperks reload - Command to reload the plugin
- /newplayerperks add <player_name> <duration> - Command to apply new player perks to a player.
- /newplayerperks remove <player_name> - Command to remove new player perks from a player.
- /newplayerperks enable - Command for a player to enable their perks.
- /newplayerperks disable - Command for a player to disable their perks.
- /newplayerperks time \[player] - Command to view the perk time a player has or your own perk time.

## Permisisons
- `newplayerperks.commands.newplayerperks` - The permission to access the /newplayerperks command.
- `newplayerperks.commands.newplayerperks.reload` - The permission to access /newplayerperks reload.
- `newplayerperks.commands.newplayerperks.add` - The permission to access /newplayerperks add.
- `newplayerperks.commands.newplayerperks.remove` - The permission to access /newplayerperks remove.
- `newplayerperks.commands.newplayerperks.enable` - The permission to access /newplayerperks enable.
- `newplayerperks.commands.newplayerperks.disable` - The permission to access /newplayerperks disable.
- `newplayerperks.commands.newplayerperks.time` - The permission to access /newplayerperks time.
- `newplayerperks.commands.newplayerperks.time.other` - The permission to access /newplayerperks time \[player].
- `newplayerperks.commands.newplayerperks.help` - The permission to access /newplayerperks help.

## Issues, Bugs, or Suggestions
* Please create a new [Github Issue](https://github.com/lukesky19/NewPlayerPerks/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to NewPlayerPerks and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.

## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1, 26.1.1, and 26.1.2.
Note: Java 25 is required even on versions older than 26.1.

Q: I get the following error: "NewPlayerPerks has been compiled by a more recent version of the Java Runtime
(class file version 69.0), this version of the Java Runtime only recognizes class file versions up to 65.0"

A: NewPlayerPerks is compiled using Java 25 as part of it's support of 26.1 and beyond. NewPlayerPerks still works on older version as long as Java 25 is used.

Q: Are there any plans to support any other versions?

A: I will always do my best to support the latest versions of the game. I will sometimes support other versions until I no longer use them.

Q: Does this work on Spigot? Paper? (Insert other server software here)?

A: I only support Paper, but this will likely also work on forks of Paper (untested). There are no plans to support any other server software (i.e., Spigot or Folia).

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Go to [SkyFlight](https://github.com/lukesky19/SkyFlight) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
