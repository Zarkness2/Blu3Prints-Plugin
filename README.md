# Blu3print Plugin

[![Latest Release](https://img.shields.io/github/v/release/bl3rune/Blu3Prints-Plugin)](https://github.com/bl3rune/Blu3Prints-Plugin/releases)
[![Build Status](https://github.com/bl3rune/Blu3Prints-Plugin/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bl3rune/Blu3Prints-Plugin/actions)
![Minimum MC Version](https://img.shields.io/badge/Spigot_Versions-v1.18_--_v1.21.5-orange)

### **A blueprint tool that allows players to capture/share/manipulate designs within Minecraft** 
### Blu3prints are placed using blocks in inventory in survival (or free in creative mode / with permissions)

![Banner](/images/Banner.png "Banner")


![usage!](/images/Blu3print.gif "Usage")

## Video tutorials playlist
[![Watch the video](https://img.youtube.com/vi/iNgYVwC9tRA/maxresdefault.jpg)](https://www.youtube.com/watch?v=iNgYVwC9tRA&list=PLAnUQqo3m_n1ULxKJXg3neW2GYDa3U3Sj)

## Features
- Craftable Blu3print writer item with customisable recipe
- Area selection method to copy block within selection
- Blu3print Writer is based on writable book and so encoded Blu3prints can be imported directly using the book
- Blu3prints are nameable to allow for at-a-glance visibility
- Blu3print UUID structure allows less overhead for server operators for storing large quantities of blu3prints
- Blu3prints can be explained in chat by right clicking on it
- Ability for any Blu3print to be exported and shared as copyable text; e.g.  `B=STONE|2:3:1|E-0-1~BA|B|`
- Shared Blu3prints can be imported by other people on the same server, or even on entirely different servers running this plugin. Allowing players to preserve their favourite creations and rebuild them on other servers with ease!
- Blu3print items can be manipulated in a number of ways
    - **Rotated around blockface**
    - **Change blockface direction**
    - **Adjusting scale** (limitable)
    - **Renamed**
    - **Duplicated**
- Blu3prints can also be manipulated from cartography table
- Configuration allows for safeguards on usage and caps on blueprint sizes
- Blu3print writer has a cooldown to prevent spam
- Permissions to fine tune usage of blu3prints
- In-Game `/blu3print.help` command to guide new users

## Getting started

### Crafting the Blu3print writer
First start by crafting a Blu3print Writer in the crafting table using these materials in any configuration:
> [ PAPER, LAPIS_LAZULI, FEATHER ]

![Craftable!](/images/Crafting.png "Craftable!")


### Using the Blu3print writer
------
While holding a Blu3print Writer, you can interact by:
- Using left click on a block to set the first position of the selection area (this clears the ignore list if there is one)
- Using left click (while sneaking) on a block to ignore/unignore the block within a selection
- Using right click on a block to set the second position of the selection area (this clears the ignore list if there is one)
- Using right click (while sneaking) on a block to show the ignore block list within the current selection
- Using right click on the air to set open the blu3print writer

![Writer](/images/Writer.png "Writer")

With the blu3print writer open:
- Click on sign and give the blu3print a name to complete it
- Or instead of using area selection, enter a blu3print code on the pages of the book and then sign and complete


### Using the Blu3print item
------
![Blu3print](/images/Completed.png "Blu3print")

While holding a completed Blu3print, you can interact by:
- Blocks are always placed from the most northwest corner of the design (minimum x and z axis coordinate)
- Using left click on a block to build the blu3print on top of that block
- Using left click (while sneaking) on a block to build the blu3print from that block even if there are blocks in the way
- Using right click on the air to print an explanation of the blu3print to chat like below
- Using right click on a block to place a holographic representaation of the blocks about to be placed (with no cost)
- Using right click (while sneaking) on a block to force place on the same height as the block clicked (useful for bridges)

![Explain](/images/Explain.png "Explain")

### Using Cartography Table to manipulate the Blu3prints
------
The Cartography Table can be used to change the rotation, direction, scale of the blu3print.
It can also be used to rename or export the blu3print.
It also has a button to open the in-game help manual.
![Menu](/images/Menu.png "Menu")


### Exporting & Importing
------
Blu3prints can be exported by using a cartography table or by using the `blu3print.export` command whilst holding the blu3print item.
![Export](/images/Export.png "Export")
This code can be shared to other players and even be used on other servers running this plugin, allowing you to preserve your creations across servers or even showcase them online (visualiser website in the pipeline!)
To use the encoded blu3prints, simply run the command `/blu3print.import <name> <encoded-string>`


### Blu3print export data structure
------
#### Header section
- Ingredient Mapping section >> B=OAK_LOGS-C=OAK_PLANKS (A is always AIR)
- "|" separator
- Sizes section >> 4:4:4 >> X-Size : Y-Size : Z-Size 
- "|" separator
- Directional and scaling data section:
    - Direction Facing (N=NORTH, S=SOUTH, E=EAST, W=WEST, U=UP, D=DOWN)
    - ":" separator
    - Rotation (TOP=0, RIGHT=1, BOTTOM=2, LEFT=3)
    - ":" separator
    - Scaling (1=Default Size, 2=Twice as big in every direction, ...)
- "~" Header end separator
#### Body section
Encoded structure of the blu3print e.g.
- "B-" >> OAK_LOGS until, END OF ROW
- "BA-" >> 1 OAK_LOGS and then AIR until, END OF ROW
- "B2A-" >> 2 OAK_LOGS and then AIR until, END OF ROW
- "ACA|" >> AIR, OAK_PLANK, AIR until, END OF COLUMN
- "-" >> AIR until, END OF ROW

- ...







## Permissions
By default all commands apart from `/blu3print.give` should be available for all non OP players.
There is also a default restrictions on the maximum size/scale of a blu3print for safety reasons. This can be configured in the `config.yml` file.
![Safety](/images/Safety.png "Safety")
![Max Size](/images/Max-size.png "Max Size")

| Permission | default    | description    |
| :---:   | :---: | :---: |
| blu3print.* |    | Allowed to use all Blu3PrintPlugin commands with no restrictions or costs   |
| blu3print.basics  | `not op` | Allowed to use Blu3Prints |
| blu3print.no-size-limit  | | No restrictions on size of Blu3prints |
| blu3print.no-scale-limit  | | No restrictions on scaling of Blu3prints |
| blu3print.force-place-discount  | false | Force placing Blu3prints no longer costs blocks unable to place |
| blu3print.no-block-cost  | false | Removes block cost for placing Blu3prints in survival |
| blu3print.holograms | true | Allowed to place hologram previews of Blu3prints |


## Help
Players can use the `/blu3print.help` command to get help with usage in-game
![Help](/images/Help.png "Help")


## Configuration
This plugin is fairly plug-and-play, simply place inside the `plugins` folder of your server and start 
the server. 

Configuration can be found in the `config.yml` file. 
| Configuration | default  | description |
| :---:  | :---:   | :---: |
| blu3print.max-size` | 100 (blocks) |  Maximum size of any one side of a blu3print |
| blu3print.max-scale | 10 (multiplier) | Maximum scale of blu3print |
| blu3print.max-overall-size | 200 (blocks) | Maximum size of any one side of a blu3print times the scale |
| blu3print.cooldown | 500 (milliseconds) | Minimum time between using Blu3print items / writer |
| blu3print.recipe.ingredients | PAPER, LAPIS_LAZULI, FEATHER | List of ingredients used in crafting the blu3print writer |
| blu3print.ignored-materials | AIR, WATER, LAVA, GRASS | List of materials to ignore when building / placing blu3prints |
| blu3print.hologram-ttl | 10 (seconds) | Time in seconds before a hologram disappears after being placed |
| **Messaging** | blu3print.messaging.* | Fine tune messaging for the Blu3Print plugin |
| *.free-placement-message.enabled | true | Enable free placement message |
| *.force-placement-message.enabled | true | Enable force placement message |
| *.discount-placement-message.enabled | true | Enable discount placement message |



## License
This project is licensed under the MIT License; 
see [MIT License](MIT.md) for details.