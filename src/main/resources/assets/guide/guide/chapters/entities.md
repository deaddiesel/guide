§6Chapter 11: 3D Entity Models

§7In this chapter, you will learn how to embed interactive, rotating 3D models of any Minecraft entity directly into your guide pages.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bSyntax

§6`@mob:entity,width,height,alignment`

§7Parameters:
• §eentity§7 — unique identifier (e.g., minecraft:zombie)
• §ewidth/height§7 — display size in pixels
• §ealignment§7 — left, center, right (default: center)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bExamples: Living Entities

§7§lZombie (centered):
§6`@mob:minecraft:zombie,45,45,center`

@mob:minecraft:zombie,45,45,center

§7§lCreeper (left):
§6`@mob:minecraft:creeper,45,45,left`

@mob:minecraft:creeper,45,45,left

§7§lParrot (right):
§6`@mob:minecraft:parrot,40,40,right`

@mob:minecraft:parrot,40,40,right

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bExamples: Bosses & Large Entities

§7§lEnder Dragon:
§6`@mob:minecraft:ender_dragon,30,30,center`



@mob:minecraft:ender_dragon,30,30,center

§7§lWither:
§6`@mob:minecraft:wither,35,35,center`



@mob:minecraft:wither,35,35,center

§7§lElder Guardian:
§6`@mob:minecraft:elder_guardian,30,30,center`



@mob:minecraft:elder_guardian,30,30,center

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bExamples: Transport & Items

§7§lBoat:
§6`@mob:minecraft:boat,50,50,center`

@mob:minecraft:boat,50,50,center

§7§lMinecart:
§6`@mob:minecraft:minecart,45,45,center`

@mob:minecraft:minecart,45,45,center

§7§lEnd Crystal:
§6`@mob:minecraft:end_crystal,35,35,center`

@mob:minecraft:end_crystal,35,35,center

§7§lArmor Stand:
§6`@mob:minecraft:armor_stand,40,40,center`

@mob:minecraft:armor_stand,40,40,center

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bImportant!

§7• Models **rotate automatically** when displayed
§7• Supports §6ALL entities§7: mobs, transport, items, bosses
§7• §6Optimized scale§7: models render larger and clearer (coefficient 0.32)
§7• §6Tooltips§7: hover over any entity to see its localized name, category, health, and hitbox size
§7• The system automatically clamps dimensions to prevent overflow beyond text boundaries

§7§lRecommended Size Chart (Current):
§7• Standard mobs (zombie, pig): §640-50§7
§7• Large mobs (guardian, wither): §650-60§7
§7• Ender Dragon: §665-75§7
§7• Transport (boats, minecarts): §645-55§7
§7• Items & decorations: §630-40§7

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bPopular Entity IDs

§7§l🔴 Hostile Mobs:
§7• §eminecraft:zombie§7, §eminecraft:creeper§7, §eminecraft:skeleton§7
§7• §eminecraft:spider§7, §eminecraft:enderman§7, §eminecraft:witch§7

§7§l🟢 Animals & Passive:
§7• §eminecraft:cow§7, §eminecraft:pig§7, §eminecraft:sheep§7
§7• §eminecraft:chicken§7, §eminecraft:horse§7, §eminecraft:parrot§7

§7§l🔵 Transport:
§7• §eminecraft:boat§7, §eminecraft:minecart§7, §eminecraft:chest_minecart§7

§7§l💀 Bosses:
§7• §eminecraft:ender_dragon§7, §eminecraft:wither§7, §eminecraft:warden§7

§7§l🟡 Items:
§7• §eminecraft:item§7, §eminecraft:end_crystal§7, §eminecraft:armor_stand§7

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Images & Animations](images_and_gifs) §7| [Structures](structures) §7| [Projection](projection)