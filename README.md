# Blu3print Plugin

[![Latest Release](https://img.shields.io/github/v/release/bl3rune/Blu3Prints-Plugin)](https://github.com/bl3rune/Blu3Prints-Plugin/releases)
[![Build Status](https://github.com/bl3rune/Blu3Prints-Plugin/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bl3rune/Blu3Prints-Plugin/actions)

**Allows players to copy/paste/export/import blocks using blu3prints and using blocks in inventory** (or for free if in creative mode)

[![Watch the video](https://img.youtube.com/vi/iNgYVwC9tRA/maxresdefault.jpg)](https://www.youtube.com/watch?v=iNgYVwC9tRA)


Features
--------
- Craftable Blu3print writer item with customisable recipe
- Area selection method to copy block within selection
- Blu3print Writer is based on writable book project and so encoded Blu3prints can be imported directly using the book
- Blu3prints are namable to allow for at-a-glance visibility
- Blu3print UUID strucute allows less overhead for server operators for storing large quantities of blu3prints
- Ability for any Blu3print to be exported and shared as text; e.g.  `B=STONE|2:3:1|E-0-1~BA|B|`
- Blu3print items can be manipulated in a number of ways
    - **Rotated around blockface**
    - **Change blockface direction**
    - **Adjusting scale** (limitable)
    - **Renamed**
    - **Duplicated**
- Configuration allows for safeguards on usage and caps on blueprint sizes
- Blu3print writer has a cooldown to prevent spam
- Permissions to fine tune usage of blu3prints
- In-Game `/blu3print.help` command to guide new users

Getting started
--------
### Crafting the Blu3print writer
First start by crafting a Blu3print Writer in the crafting table using these materials in any configuration:
> [ PAPER, LAPIS_LAZULI, FEATHER ]

![Craftable!](/images/Crafting.png "Craftable!")

### Using the Blu3print writer
While holding a Blu3print Writer, you can interact by:
- Using left click on a block to set the first position of the selection area
- Using right click on a block to set the second position of the selection area
- Using right click on the air to set open the blu3print writer

![Writer](/images/Writer.png "Writer")

With the blu3print writer open:
- Click on sign and give the blu3print a name to complete it
- Or instead of using area selection, enter a blu3print code on the pages of the book and then sign and complete

### Using the Blu3print item
![Blu3print](/images/Completed.png "Blu3print")

While holding a completed Blu3print, you can interact by:
- Using left click on a block to build the blu3print on top of that block
- Using left click (while holding shift) on a block to build the blu3print from that block even if there are blocks in the way
- Using right click to print an explanation of the blu3print to chat like below

![Explain](/images/Explain.png "Explain")

### Using Cartography Table to manipulate the Blu3prints
The Cartography Table can be used to change the rotation, direction, scale of the blu3print.
It can also be used to rename or export the blu3print.
It also has a button to open the in-game help manual.
![Menu](/images/Menu.png "Menu")

### Exporting 
Blu3prints can be exported by using a cartography table or by using the `blu3print.export` command whilst holding the blu3print item.
![Export](/images/Export.png "Export")

### Permissions
By default all commands apart from `/blu3print.give` should be available for all non OP players.
There is also a default restriction on the maximum scale of a blu3print for safety reasons. This can be configured in the `config.yml` file.
![Safety](/images/Safety.png "Safety")


Configuration
--------
This plugin is fairly plug-and-play, simply place inside the `plugins` folder of your server and start 
the server. 

Configuration can be found in the `config.yml` file. 
- `blu3print.max-scale` - Maximum scale of blu3print (default 10)
- `blu3print.cooldown` - Minimum time in milliseconds between using Blu3print items / writer
- `blu3print.recipe.ingredients` - Customisable list of ingredients used in crafting the blu3print writer


License
--------
This project is licensed under the MIT License; 
see [MIT License](MIT.md) for details.