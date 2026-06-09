§6Chapter 14: Creating a Book

§7In this chapter, you will learn how to add a custom guide to your mod, configure its appearance in the menu, and hide the developer manual from players.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bFile Structure

§7To add a book, create the following structure in your mod's resources:
§6`assets/<modid>/guide/`
• §6`book.json`§7 — book configuration (name, icon, settings)
• §6`chapters/`§7 — folder for chapters in English (default)
• §6`chapters/ru_ru/`§7 — folder for Russian chapters (optional)
• §6`chapters/your_language/`§7 — folder for chapters in your language (optional)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bConfiguring book.json

§7Create the §6`book.json`§7 file next to the §6`chapters/`§7 folder:
§6`{
§7  "name": "your_mod.book.guide",
§7  "namespace": "your_mod",
§7  "default_chapter": "introduction",
§7  "icon": "your_mod:textures/gui/book_icon.png",
§7  "dev_only": false
§6}`

§7Parameters:

• §e`name`§7 — localization key for the book's name in the menu (lang file)
• §e`namespace`§7 — your mod's ID (must match the folder name in assets)
• §e`default_chapter`§7 — filename of the first chapter (without .md)
• §e`icon`§7 — path to a 16x16 icon in ResourceLocation format
• §e`dev_only`§7 — if `§6true§7`, the book is hidden via config

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bName Localization

§7Add the translation for the §6`name`§7 key to your language files:
§6`assets/<modid>/lang/(your_language).json`
§6`{
§7  "your_mod.book.guide": "§6Your Mod: Guide§7"
§6}`

§7The game will automatically apply the correct language when opening the book selector menu.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bHiding the Developer Manual

§7If you are using §6Guide§7 as a library, the developer manual is hidden from players by default.
§7To enable or disable it, open the config:
§6`config/guide/guide-client.toml`
§7Find the parameter:
§6`show_dev_manual = true`

§7Set it to §6false§7 to hide the manual from the selector menu. Modpack developers can change this value in their builds.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bHow it works:

§7• Upon game launch, §6BookRegistry§7 automatically scans §6assets/*/guide/book.json§7 across all mods
§7• Each book is isolated by §6namespace§7 and uses its own resources
§7• The §6show_dev_manual§7 config filters out books with the §6dev_only: true§7 flag
§7• The selector menu (§6BookSelectorScreen§7) renders the filtered list of books with icons
§7• Clicking a book opens §6GuideScreen§7 with the passed §6namespace§7, loading only the relevant files

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bTips

§7• Use §616x16 pixel§7 icons for correct rendering in the menu
§7• Do not set §6dev_only: true§7 for guides intended for players
§7• Always prefix paths to structures and images with your §6namespace§7 to avoid conflicts
§7• Only update §6guide-favorites.properties§7 manually if needed — the system manages it automatically

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Hot Reload](hot_reload) §7| [Structures](structures) §7| [G-Key Integration](g_key_integration)