§6Chapter 5: Crafting & Recipes

§7In this chapter, you will learn how to view crafting recipes for mechanisms directly on the pages of the interactive guide.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bRequired Materials

§7To assemble certain complex structures, you will need basic components. The list below shows key resources you should prepare in advance:

@inline_item:minecraft:redstone
@inline_item:minecraft:iron_ingot

§7You can hover over any item icon in the list to see its full name and in-game description.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bCrafting Grid (3x3)

§7Below is the exact layout for the crafting grid to create a basic energy block:

@matrix_craft:minecraft:furnace, minecraft:cobblestone, minecraft:cobblestone, minecraft:cobblestone, minecraft:cobblestone, minecraft:redstone, minecraft:cobblestone, minecraft:cobblestone, minecraft:cobblestone, minecraft:cobblestone,center

§7Replicate this layout in a standard crafting table to obtain the output item shown in the right slot.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bCrafting Grid Alignment

§7Like images, the crafting grid can be aligned horizontally. To do this, add the alignment parameter to the end of the directive:

§7§lCentered:
§6`@matrix_craft:result, slot1, ..., slot9, center`

§7§lLeft Aligned:
§6`@matrix_craft:result, slot1, ..., slot9, left`

§7§lRight Aligned:
§6`@matrix_craft:result, slot1, ..., slot9, right`

§7The alignment parameter must always be specified **§6last§7**, separated by a comma after all 9 crafting slots.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Images & Animations](images_and_gifs) §7| [Structures](structures) §7| [Syntax & Links](syntax)