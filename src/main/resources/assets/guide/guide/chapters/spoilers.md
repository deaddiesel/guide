§6Chapter 13: Spoilers and Hidden Content

§7In this chapter, you will learn how to hide large blocks of text, lists, or lore behind clickable headers.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bSyntax

§7To create a spoiler, use two tags:
§6`@spoiler:Title`
... hidden content ...
§6`@endspoiler`

§7Parameters:
• §eTitle§7 — text that is always visible (the button)
• §eContent§7 — any text, images, or tags inside the spoiler

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bUsage Examples

§7§lSecret Lore (Hidden):
§6`@spoiler:🔒 Secret Record`

@spoiler:🔒 Secret Record
§7Text that the player will see only after clicking.
You can write long stories, dungeon descriptions, or hidden recipes here.
@endspoiler

§7§lLong Changelog (Hidden):
§6`@spoiler:📜 Version History`

@spoiler:📜 Version History
§7• §6v1.0§7 - Release
§7• §6v1.1§7 - Added spoilers
§7• §6v1.2§7 - Lazy Load optimization
@endspoiler

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bHow it works:

§7• By default, the spoiler is **collapsed** (▶ icon)
§7• Clicking it **expands** it (▼ icon) and shows the content
§7• Page height and scrollbar are **automatically recalculated**
§7• Spoilers save space in the guide, reducing visual clutter
§7• Inside a spoiler, you can use §6text§7, §6images§7, and even §6other tags§7

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bTips

§7• Use spoilers for §6additional information§7 that not all players need
§7• Do not hide §6important instructions§7 required for progression
§7• Titles should be short and clear

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Syntax](syntax) §7| [Structures](structures) §7| [Items](items)