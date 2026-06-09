package com.deaddiesel.mods.guide;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuideConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_DEV_MANUAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_MOB_SOUNDS;
    private static final Map<String, String> SOUNDS_MAP = new HashMap<>();

    public static List<String> getDefaults() {
        return Arrays.asList(
                "creeper=entity.creeper.primed",
                "iron_golem=entity.iron_golem.damage",
                "snow_golem=entity.snow_golem.shoot",
                "axolotl=entity.axolotl.idle_air",
                "turtle=entity.turtle.egg_crack",
                "slime=entity.slime.squish",
                "magma_cube=entity.magma_cube.squish",
                "cave_spider=entity.spider.ambient",
                "trader_llama=entity.llama.ambient",
                "bee=entity.bee.loop",
                "ender_dragon=entity.ender_dragon.growl",
                "cod=entity.cod.flop",
                "salmon=entity.salmon.flop",
                "tropical_fish=entity.tropical_fish.flop",
                "pufferfish=entity.pufferfish.blow_out"
        );
    }

    static {
        SHOW_DEV_MANUAL = BUILDER
                .comment("Show the developer manual in the book selector (disable for player modpacks)")
                .define("show_dev_manual", true);

        BUILDER.comment("Mob sound settings for the guide book").push("sounds");
        CUSTOM_MOB_SOUNDS = BUILDER
                .comment("Format: 'mob_id=sound_id'. You can use either the short name ('creeper') or the full ID ('minecraft:creeper')")
                .defineList("custom_mob_sounds", getDefaults(), obj -> obj instanceof String);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void loadMap() {
        SOUNDS_MAP.clear();
        for (String entry : CUSTOM_MOB_SOUNDS.get()) {
            if (entry == null || !entry.contains("=")) continue;
            String[] split = entry.split("=");
            if (split.length == 2) {
                String mob = split[0].trim();
                String sound = split[1].trim();
                SOUNDS_MAP.put(mob, sound);
            }
        }
    }

    public static String getSoundForMob(String mobPath) {
        if (SOUNDS_MAP.isEmpty()) {
            loadMap();
        }
        return SOUNDS_MAP.get(mobPath);
    }
}