§6Chapter 12: 3D Item Models

§7In this chapter, you will learn how to display interactive 3D item models directly in the guide text.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bSyntax

§6`@item:item_id,size,alignment`

§7Parameters:
• §eitem_id§7 — item identifier (e.g., minecraft:diamond)
• §esize§7 — display size in pixels (square)
• §ealignment§7 — left, center, right (default is center)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bExamples: Resources & Tools

§7§lDiamond (center):
§6`@item:minecraft:diamond,100,center`

@item:minecraft:diamond,100,center

§7§lNetherite Sword (left):
§6`@item:minecraft:netherite_sword,100,left`

@item:minecraft:netherite_sword,100,left

§7§lTotem of Undying (right):
§6`@item:minecraft:totem_of_undying,100,right`

@item:minecraft:totem_of_undying,100,right

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bExamples: Special Items

§7§lNether Star:
§6`@item:minecraft:nether_star,100,center`

@item:minecraft:nether_star,100,center

§7§lDragon Egg:
§6`@item:minecraft:dragon_egg,100,center`

@item:minecraft:dragon_egg,100,center

§7§lMusic Disc (11):
§6`@item:minecraft:music_disc_11,100,center`

@item:minecraft:music_disc_11,100,center

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bFeatures

§7• Items **rotate** in mid-air, just like in your inventory
§7• Hovering reveals a §6tooltip§7 with the full name and stack size
§7• §6Lazy loading§7: models are only loaded when you scroll to them (saves memory)
§7• Supports §6any item§7 from vanilla Minecraft and mods
§7• Syntax is similar to `@mob`, but simpler (only one size parameter)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Entities](entities) §7| [Crafts](crafts) §7| [Spoilers](spoilers)