§6Chapter 6: 3D Structures

§7In this chapter, you will learn how to add three-dimensional structures into the guidebook.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bWhat are Structures?
§7Structures are 3D models of multiblock mechanisms and buildings.

§7Features:
§6• §7Rotation via mouse or buttons
§6• §7Zoom (in / out)
§6• §7Layer-by-layer viewing
§6• §7Projection into the world (hologram)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bHow to Add a Structure?

§7§lStep 1: Create a .nbt File
§7Save your building in §6assets/guide/structures/name.nbt

§7Saving methods:
§6• §7The §d/structure save§7 command in Minecraft
§6• §7Structure Block
§6• §7MCEdit / WorldEdit

§7§lStep 2: Add the Tag to the Chapter
§7At the beginning of your .md file, write:

§6`@structure:file_name`

§7Important: §cDO NOT put a space after the colon!
§7Correct: §6`@structure:generator_setup`
§7Incorrect: §c`@structure: generator_setup`

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bTab Names (@tab)

§7By default, tabs are named §6#1, #2, #3

§7To specify your own title, add §6`@tab:Name`§7 right before the structure tag:

§6`@tab:Chassis`
`@structure:generator_setup`

§7If @tab is not specified:
§6• §7On the tab: §f#1, #2, #3
§6• §7In the tooltip: ⚠ Warning

§7If @tab is specified:
§6• §7On the tab: §f#1, #2, #3
§6• §7In the tooltip: full title

§7Limit: §cmaximum 3 structures per chapter

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bView Controls

§6[ ◀ ] [ ▶ ] §7— Rotation
§6[ Auto ] §7— Auto-rotation
§6[ 🔍 Zoom - / + ] §7— Zoom (0.25x - 1.4x)
§6[ Layer - / All / + ] §7— Layer-by-layer mode

§7Hover over a block → you will see its name

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bTips

§7§l1. Size
§7Optimal: §6up to 16x16x16 blocks
§7Large structures cause lag

§7§l2. Orientation
§7The structure should face §6South

§7§l3. Optimization
§7Remove air blocks from the .nbt file

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bExample

§7Creating a multiblock generator:

§6`@tab:Chassis`
`@structure:generator_frame`

§6`@tab:Core`
`@structure:generator_core`

§6`@tab:Cooling`
`@structure:generator_cooling`

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bTroubleshooting

§c[Error] File not found
§7→ Check the path: §6assets/guide/structures/

§c[Error] Structure too large
§7→ Maximum size is §616x16x16

§c[Error] Failed to load NBT
§7→ File may be corrupted

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Crafts](crafts) §7| [Syntax and Links](syntax) §7| [Projection](projection)

§a★ Happy building!

@tab:Chassis
@structure:generator_setup

@tab:Very long title for the chassis setup
@structure:generator_setup

@structure:generator_setup