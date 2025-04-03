# Blu3print Plugin

[![Latest Release](https://img.shields.io/github/v/release/bl3rune/Blu3Prints-Plugin)](https://github.com/bl3rune/Blu3Prints-Plugin/releases)
[![Build Status](https://github.com/bl3rune/Blu3Prints-Plugin/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bl3rune/Blu3Prints-Plugin/actions)

![Banner](/images/Banner.png "Banner")

**Allows players to copy/paste/export/import blocks using blu3prints and using blocks in inventory** (or for free if in creative mode)
![usage!](/images/Blu3print.gif "Usage")

## Basic Usage tutorial
[![Watch the video](https://img.youtube.com/vi/iNgYVwC9tRA/maxresdefault.jpg)](https://www.youtube.com/watch?v=iNgYVwC9tRA)

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
- Using left click on a block to build the blu3print on top of that block
- Using left click (while sneaking) on a block to build the blu3print from that block even if there are blocks in the way
- Using right click to print an explanation of the blu3print to chat like below

![Explain](/images/Explain.png "Explain")

## Advanced Usage tutorial
[![Watch the advance video](https://img.youtube.com/vi/Cub94f1ckE8/maxresdefault.jpg)](https://www.youtube.com/watch?v=Cub94f1ckE8)

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


## Help
Players can use the `/blu3print.help` command to get help with usage in-game
![Help](/images/Help.png "Help")


## Configuration
This plugin is fairly plug-and-play, simply place inside the `plugins` folder of your server and start 
the server. 

Configuration can be found in the `config.yml` file. 
- `blu3print.max-size` - Maximum size of any one side of a blu3print (default 100 blocks)
- `blu3print.max-scale` - Maximum scale of blu3print (default 10 blocks)
- `blu3print.max-overall-size` - Maximum size of any one side of a blu3print times the scale (default 200 blocks)
- `blu3print.cooldown` - Minimum time in milliseconds between using Blu3print items / writer
- `blu3print.recipe.ingredients` - Customisable list of ingredients used in crafting the blu3print writer
- `blu3print.ignored-materials` - Customisable list of materials to ignore when building / placing blu3prints


## License
This project is licensed under the MIT License; 
see [MIT License](MIT.md) for details.