§6Chapter 9: Hot-Reload System

§7In this chapter, you will learn how the dynamic on-the-fly content update system works and how to use the reload command during guidebook development.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bQuick Debugging for Authors

§7You no longer need to close Minecraft and restart the game for every modified line of text, color code, or new craft recipe. The system supports instant data import:

§71. Open the desired `.md` chapter file of the guidebook in any text editor (like Notepad++ or VS Code).
§72. Make changes to the text, modify color codes, or add new item binding tags.
§73. Save the changes in your file.
§74. Return to the game, open the chat, and type the command: **§a/guide reload§7**.
§75. The guidebook will instantly reread the files from the disk and refresh the entire interface and cache right before your eyes!

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bTechnical Details

§7The `/guide reload` command is a service developer tool and features built-in protection:
* §7**Permission Level:** Available only to server operators (permission level §62§7 and higher) or in a singleplayer world with cheats enabled.
* §7**Cache Architecture:** Upon invocation, the command completely clears the static `@bind:` item registry, rebuilds the sidebar menu structure from the `index.md` file, and forces the current screen to redraw if the book is open.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[G-Key](g_key_integration) §7| [Syntax](syntax) §7| [Introduction](introduction)