@bind:minecraft:book,minecraft:writable_book
§6Chapter 8: G-Key Integration

§7In this chapter, you will learn how the revolutionary quick item lookup system works, how the Sci-Fi tooltip scanner is built, and how to bind mechanisms to guidebook pages.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bQuick Lookup for Players

§7You no longer need to manually flip through the guidebook to find the right section. The book engine allows you to instantly open articles directly from your inventory:

§71. Open your inventory, a chest, or any crafting interface.
§72. Hover your mouse cursor over the component or block you are interested in.
§73. Press and hold the **§6G§7** key for **§a1.5 seconds§7**.
§74. An animated orange scanning bar will launch right inside the item's tooltip.
§75. As soon as the loading reaches **§a100%§7**, the book will open directly on the corresponding page!

§bScanning Indicator

§7A futuristic progress bar renders in real-time right inside the item description:
§7［§6┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃§8┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃§7］［§a40%§7］

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bBound Items

@inline_item:minecraft:book
@inline_item:minecraft:writable_book

§7You can hover your mouse over the icons above — thanks to the integration, a lookup hint and an animated scanning bar will now automatically appear at the bottom of their tooltips.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bGuide for Authors

§7To bind items to your chapter, add the special service tag **@bind:** to the very first line of your `.md` file.

§7Example of binding tag syntax:
§6`@bind:minecraft:furnace,minecraft:crafting_table`

§7You can specify any number of item IDs separated by a regular comma. If an item is bound to multiple chapters, the book's sidebar menu will automatically filter and show the player only those specific articles, hiding everything else!

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Crafts](crafts) §7| [Syntax](syntax) §7| [Introduction](introduction)