§6Глава 10: Картинки и анимации

§7В этой главе вы узнаете, как добавлять изображения в страницы руководства.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bСинтаксис

§6`@image:путь,ширина,высота,выравнивание`

§7Параметры:
• §eпуть§7 — расположение текстуры
• §eширина/высота§7 — размер отображения в пикселях
• §eвыравнивание§7 — left (слева), center (по центру) или right (справа) (по умолчанию left)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bПримеры

§7§lТекстура камня из Minecraft (по центру):
§6`Пример текста: @image:minecraft:textures/block/stone.png,32,32,center`

@image:minecraft:textures/block/stone.png,32,32,center

§7§lТекстура земли из Minecraft (по левому краю):
§6`Пример текста: @image:minecraft:textures/block/dirt.png,64,64`

@image:minecraft:textures/block/dirt.png,64,64,left

§7§lТекстура обсидиана из Minecraft (по правому краю):
§6`Пример текста: @image:minecraft:textures/block/dirt.png,128,128`

@image:minecraft:textures/block/obsidian.png,128,128,right

§7§lСобственные текстуры мода:
§6`Пример текста: @image:guide:textures/images/schema.png,300,200,center`

(Ваш пример)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bВажно!

§7• Учитывайте пропорции текстуры, чтобы избежать искажений
§7• Формат: §6PNG
§7• Расположение: §6guide:textures/images/logo.png или minecraft:textures/block/stone.png)
§7• Рекомендуется размер кратный степени двойки §6(16, 64, 128, 256, 512)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bАнимации (GIF)

§7Теперь можно вставлять анимированные изображения. Синтаксис аналогичен картинкам.

§6`@gif:путь,ширина,высота,выравнивание`

§7Параметры:
• §eпуть§7 — расположение файла (только .gif)
• §eширина/высота§7 — размер отображения
• §eвыравнивание§7 — left, center, right (по умолчанию center)

§7§lПримеры:

§7Отображение слева: §6`@gif:guide:textures/gifs/parrot.gif,128,128,left`
@gif:guide:textures/gifs/parrot.gif,128,128,left

§7Отображение по центру: §6`@gif:guide:textures/gifs/bee.gif,128,128,center`
@gif:guide:textures/gifs/bee.gif,128,128,center

§7Отображение справа: §6`@gif:guide:textures/gifs/piglin.gif,128,128,right`
@gif:guide:textures/gifs/piglin.gif,128,128,right

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lСмотрите также:
[Крафты и Рецепты](crafts) §7| [Структуры](structures) §7| [Проекция](projection)