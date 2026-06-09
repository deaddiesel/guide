package com.deaddiesel.mods.guide;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MobEntityRenderer {
    private Entity entity;
    private float rotationY = 0f;

    private boolean wasHovered = false;
    private int hoverSoundCooldown = 0;

    public void setEntity(String entityId) {
        try {
            var type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(entityId));
            if (type != null && Minecraft.getInstance().level != null) {
                this.entity = type.create(Minecraft.getInstance().level);

                if (this.entity != null) {
                    if (this.entity instanceof LivingEntity living) {
                        living.yBodyRot = 0;
                        living.yHeadRot = 0;
                        living.walkAnimation.setSpeed(0f);
                        living.walkAnimation.position(0f);

                        if (living instanceof net.minecraft.world.entity.animal.SnowGolem golem) {
                            golem.setPumpkin(false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create entity: " + entityId + " -> " + e.getMessage());
        }
    }

    public void tick() {
        if (entity == null) return;
        rotationY += 2.0f;
        if (rotationY >= 360f) rotationY -= 360f;

        if (entity instanceof LivingEntity living) {
            living.yBodyRot = rotationY;
            living.yHeadRot = rotationY;
        }

        if (hoverSoundCooldown > 0) {
            hoverSoundCooldown--;
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int x, int y, int width, int height) {
        if (entity == null) return;

        var mc = Minecraft.getInstance();
        poseStack.pushPose();

        poseStack.translate(x + width / 2f, y + height / 2f, 100f);

        float baseScale = 0.35f;
        float autoScale = width * baseScale;
        poseStack.scale(autoScale, autoScale, autoScale);

        float partialTicks = mc.getFrameTimeNs() / 1E9f;
        float smoothRotation = rotationY + (partialTicks * 40.0f);

        poseStack.mulPose(Axis.YP.rotationDegrees(smoothRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));

        if (entity instanceof net.minecraft.world.entity.animal.AbstractFish) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
        }

        RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();
        Lighting.setupForEntityInInventory();

        if (entity instanceof LivingEntity living) {
            if (living instanceof net.minecraft.world.entity.animal.AbstractFish) {
                living.setXRot(0f);
                living.xRotO = 0f;
            }

            living.yBodyRot = smoothRotation;
            living.yBodyRotO = smoothRotation;
            living.yHeadRot = smoothRotation;
            living.yHeadRotO = smoothRotation;
            living.yRotO = smoothRotation;
            living.setYRot(smoothRotation);
        }

        boolean oldNoCulling = entity.noCulling;
        entity.noCulling = true;

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);

        dispatcher.render(entity, 0, 0, 0, smoothRotation, 1.0f, poseStack, buffer, 15728880);

        dispatcher.setRenderShadow(true);
        entity.noCulling = oldNoCulling;

        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        poseStack.popPose();
    }

    public Entity getEntity() { return entity; }

    public void clear() {
        if (entity != null) {
            entity.discard();
            entity = null;
        }
    }

    private String getMobSoundPath(String entityPath) {
        return switch (entityPath) {
            case "creeper" -> "entity.creeper.primed";
            case "iron_golem" -> "entity.iron_golem.damage";
            case "snow_golem" -> "entity.snow_golem.shoot";
            case "axolotl" -> "entity.axolotl.idle_air";
            case "turtle" -> "entity.turtle.egg_crack";
            case "slime" -> "entity.slime.squish";
            case "magma_cube" -> "entity.magma_cube.squish";
            case "cave_spider" -> "entity.spider.ambient";
            case "trader_llama" -> "entity.llama.ambient";
            case "bee" -> "entity.bee.loop";
            case "ender_dragon" -> "entity.ender_dragon.growl";
            default -> "entity." + entityPath + ".ambient";
        };
    }

    private void playMobSound() {
        if (!(entity instanceof LivingEntity)) return;

        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        var entityId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) return;

        String soundPath = getMobSoundPath(entityId.getPath());

        if (entity instanceof net.minecraft.world.entity.animal.AbstractFish) {
            if (entityId.getPath().equals("pufferfish")) {
                soundPath = "entity.puffer_fish.blow_up";
            } else {
                soundPath = "entity." + entityId.getPath() + ".flop";
            }
        }

        ResourceLocation ambientId = soundPath.contains(":") ? ResourceLocation.tryParse(soundPath) : ResourceLocation.tryParse(entityId.getNamespace() + ":" + soundPath);
        if (ambientId == null) return;

        net.minecraft.sounds.SoundEvent ambientSound = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(ambientId);

        net.minecraft.client.resources.sounds.SimpleSoundInstance guiSound = new net.minecraft.client.resources.sounds.SimpleSoundInstance(
                ambientSound.getLocation(),
                net.minecraft.sounds.SoundSource.NEUTRAL,
                0.4f,
                1.0f,
                net.minecraft.util.RandomSource.create(),
                false,
                0,
                net.minecraft.client.resources.sounds.SimpleSoundInstance.Attenuation.NONE,
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                true
        );

        mc.getSoundManager().play(guiSound);
    }

    public void renderTooltip(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        if (entity == null) return;

        var mc = Minecraft.getInstance();
        if (mc.screen == null) return;
        int mouseX = (int) mc.mouseHandler.xpos();
        int mouseY = (int) mc.mouseHandler.ypos();
        mouseX = (int) (mouseX / mc.getWindow().getGuiScale());
        mouseY = (int) (mouseY / mc.getWindow().getGuiScale());

        boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        if (isHovered) {
            if (!wasHovered && hoverSoundCooldown <= 0) {
                playMobSound();
                hoverSoundCooldown = 60;
            }
            wasHovered = true;
        } else {
            wasHovered = false;
        }

        if (isHovered) {
            List<Component> tooltip = new ArrayList<>();

            if (entity instanceof LivingEntity living) {
                Component entityName = entity.getType().getDescription();
                String nameTemplate = Component.translatable("guide.tooltip.entity_name").getString();
                tooltip.add(Component.literal(String.format(nameTemplate, entityName.getString())));

                ResourceLocation registryKey = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                String idString = registryKey != null ? registryKey.toString() : "unknown";
                tooltip.add(Component.literal(String.format(
                        Component.translatable("guide.tooltip.id").getString(),
                        idString
                )));

                String catKey = switch (living.getType().getCategory()) {
                    case MONSTER -> "guide.tooltip.hostile";
                    case CREATURE -> "guide.tooltip.friendly";
                    case AMBIENT -> "guide.tooltip.neutral";
                    case WATER_AMBIENT -> "guide.tooltip.aquatic";
                    case MISC -> "guide.tooltip.misc";
                    default -> "guide.tooltip.unknown";
                };
                tooltip.add(Component.literal(String.format(
                        Component.translatable("guide.tooltip.type").getString(),
                        Component.translatable(catKey).getString()
                )));

                if (living.getMaxHealth() > 0) {
                    tooltip.add(Component.literal(String.format(
                            Component.translatable("guide.tooltip.health").getString(),
                            Math.round(living.getMaxHealth())
                    )));
                }

                tooltip.add(Component.literal(String.format(
                        Component.translatable("guide.tooltip.size").getString(),
                        entity.getBbWidth(),
                        entity.getBbHeight()
                )));
            } else {
                Component entityName = entity.getType().getDescription();
                String nameTemplate = Component.translatable("guide.tooltip.entity_name").getString();
                tooltip.add(Component.literal(String.format(nameTemplate, entityName.getString())));

                ResourceLocation registryKey = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                String idString = registryKey != null ? registryKey.toString() : "unknown";
                tooltip.add(Component.literal(String.format(
                        Component.translatable("guide.tooltip.id").getString(),
                        idString
                )));

                tooltip.add(Component.literal(String.format(
                        Component.translatable("guide.tooltip.type").getString(),
                        Component.translatable("guide.tooltip.other").getString()
                )));
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 500);
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
            guiGraphics.pose().popPose();
        }
    }
}