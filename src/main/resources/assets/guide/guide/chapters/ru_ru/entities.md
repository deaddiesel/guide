§6Глава 11: 3D Модели сущностей

§7В этой главе вы узнаете, как добавлять вращающиеся 3D модели любых сущностей Minecraft.

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bСинтаксис

§6`@mob:сущность,ширина,высота,выравнивание`

§7Параметры:
• §eсущность§7 — идентификатор (например, minecraft:zombie)
• §eширина/высота§7 — размер отображения в пикселях
• §eвыравнивание§7 — left, center, right (по умолчанию center)

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bПримеры: Живые существа

§7§lЗомби (по центру):
§6`@mob:minecraft:zombie,45,45,center`

@mob:minecraft:zombie,45,45,center

§7§lКрипер (слева):
§6`@mob:minecraft:creeper,45,45,left`

@mob:minecraft:creeper,45,45,left

§7§lПопугай (справа):
§6`@mob:minecraft:parrot,40,40,right`

@mob:minecraft:parrot,40,40,right

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bПримеры: Боссы и крупные сущности

§7§lЭндер Дракон:
§6`@mob:minecraft:ender_dragon,30,30,center`



@mob:minecraft:ender_dragon,30,30,center

§7§lИссушитель:
§6`@mob:minecraft:wither,35,35,center`



@mob:minecraft:wither,35,35,center

§7§lДревний Страж:
§6`@mob:minecraft:elder_guardian,30,30,center`



@mob:minecraft:elder_guardian,30,30,center

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bПримеры: Транспорт и предметы

§7§lЛодка:
§6`@mob:minecraft:boat,50,50,center`

@mob:minecraft:boat,50,50,center

§7§lВагонетка:
§6`@mob:minecraft:minecart,45,45,center`

@mob:minecraft:minecart,45,45,center

§7§lКристалл Энда:
§6`@mob:minecraft:end_crystal,35,35,center`

@mob:minecraft:end_crystal,35,35,center

§7§lСтойка для брони:
§6`@mob:minecraft:armor_stand,40,40,center`

@mob:minecraft:armor_stand,40,40,center

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bВажно!

§7• Модели **автоматически вращаются** при отображении
§7• Поддерживаются §6ВСЕ сущности§7: мобы, транспорт, предметы, боссы
§7• §6Новый масштаб§7: модели рендерятся крупнее и чётче (коэффициент 0.32)
§7• §6Тултипы§7: наведите курсор на любую сущность, чтобы увидеть локализованное имя, категорию, здоровье и размер хитбокса
§7• Система автоматически ограничивает размер, чтобы ничего не вылезало за границы

§7§lТаблица рекомендуемых размеров (актуальная):
§7• Стандартные мобы (зомби, свинья): §640-50§7
§7• Крупные мобы (страж, иссушитель): §650-60§7
§7• Эндер Дракон: §665-75§7
§7• Транспорт (лодки, вагонетки): §645-55§7
§7• Предметы и декорации: §630-40§7

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§bПопулярные ID сущностей

§7§l🔴 Монстры:
§7• §eminecraft:zombie§7, §eminecraft:creeper§7, §eminecraft:skeleton§7
§7• §eminecraft:spider§7, §eminecraft:enderman§7, §eminecraft:witch§7

§7§l🟢 Животные и мирные:
§7• §eminecraft:cow§7, §eminecraft:pig§7, §eminecraft:sheep§7
§7• §eminecraft:chicken§7, §eminecraft:horse§7, §eminecraft:parrot§7

§7§l🔵 Транспорт:
§7• §eminecraft:boat§7, §eminecraft:minecart§7, §eminecraft:chest_minecart§7

§7§l💀 Боссы:
§7• §eminecraft:ender_dragon§7, §eminecraft:wither§7, §eminecraft:warden§7

§7§l🟡 Предметы:
§7• §eminecraft:item§7, §eminecraft:end_crystal§7, §eminecraft:armor_stand§7

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7§lСмотрите также:
[Картинки и анимации](images_and_gifs) §7| [Структуры](structures) §7| [Проекция](projection)