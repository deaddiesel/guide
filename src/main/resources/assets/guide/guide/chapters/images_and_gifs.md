§6Chapter 10: Images & Animations

§7In this chapter, you will learn how to add images to guide pages.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bSyntax

§6`@image:path,width,height,alignment`

§7Parameters:
• §epath§7 — texture location
• §ewidth/height§7 — display size in pixels
• §ealignment§7 — left, center, or right (default: left)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bExamples

§7§lMinecraft Stone Texture (centered):
§6`Sample text: @image:minecraft:textures/block/stone.png,32,32,center`

@image:minecraft:textures/block/stone.png,32,32,center

§7§lMinecraft Dirt Texture (left-aligned):
§6`Sample text: @image:minecraft:textures/block/dirt.png,64,64`

@image:minecraft:textures/block/dirt.png,64,64,left

§7§lMinecraft Obsidian Texture (right-aligned):
§6`Sample text: @image:minecraft:textures/block/obsidian.png,128,128`

@image:minecraft:textures/block/obsidian.png,128,128,right

§7§lCustom Mod Textures:
§6`Sample text: @image:guide:textures/images/schema.png,300,200,center`

(Your example here)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bImportant!

§7• Consider texture proportions to avoid distortion
§7• Format: §6PNG
§7• Location: §6guide:textures/images/logo.png or minecraft:textures/block/stone.png
§7• Recommended sizes are powers of two: §6(16, 64, 128, 256, 512)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bAnimations (GIFs)

§7You can now insert animated images. The syntax is identical to static images.

§6`@gif:path,width,height,alignment`

§7Parameters:
• §epath§7 — file location (.gif only)
• §ewidth/height§7 — display size
• §ealignment§7 — left, center, right (default: center)

§7§lExamples:

§7Left-aligned: §6`@gif:guide:textures/gifs/parrot.gif,128,128,left`
@gif:guide:textures/gifs/parrot.gif,128,128,left

§7Centered: §6`@gif:guide:textures/gifs/bee.gif,128,128,center`
@gif:guide:textures/gifs/bee.gif,128,128,center

§7Right-aligned: §6`@gif:guide:textures/gifs/piglin.gif,128,128,right`
@gif:guide:textures/gifs/piglin.gif,128,128,right

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lSee also:
[Crafting & Recipes](crafts) §7| [Structures](structures) §7| [Projection](projection)