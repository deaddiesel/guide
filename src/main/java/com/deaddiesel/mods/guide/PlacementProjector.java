package com.deaddiesel.mods.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = GuideMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlacementProjector {

    public enum State { INACTIVE, READY, PLACED }
    private static State state = State.INACTIVE;

    private static ResourceLocation activeStructure = null;
    private static List<CachedBlock> cachedBlocks = null;
    private static BlockPos anchorBlock = null;
    private static int rotationIndex = 0;

    private static int placedCount = 0;
    private static int totalCount = 0;
    private static long lastCompletionTime = 0;
    private static long spawnTime = 0;

    private static String customStructureName = "";
    private static final List<BlockPos> wrongBlockPositions = new ArrayList<>();

    private static final Object LOCK = new Object();

    private static final int HUD_X = 10, HUD_Y = 10, BTN_W = 60, BTN_H = 22, GAP = 4;
    private static final List<int[]> buttonBounds = new ArrayList<>();
    private static final List<Runnable> buttonActions = new ArrayList<>();
    private static final List<String> buttonLabels = new ArrayList<>();
    private static long lastDeleteAttemptTime = 0;
    private static final long DELETE_CONFIRM_WINDOW = 3000;

    private record CachedBlock(BlockPos pos, BlockState state) {}

    public static void setActiveStructure(ResourceLocation loc, String displayName) {
        synchronized (LOCK) {
            activeStructure = loc;
            customStructureName = displayName.toUpperCase();
            state = State.READY;
            cachedBlocks = null;
            anchorBlock = null;
            rotationIndex = 0;
            placedCount = 0;
            totalCount = 0;
            lastCompletionTime = 0;
            spawnTime = 0;
            wrongBlockPositions.clear();
            // Logger removed
        }
    }
    public static void moveProjection(int dx, int dy, int dz) {
        synchronized (LOCK) {
            if (anchorBlock != null) {
                anchorBlock = anchorBlock.offset(dx, dy, dz);
                updateProgress();
            }
        }
    }

    public static void clear() {
        synchronized (LOCK) {
            state = State.INACTIVE;
            activeStructure = null;
            cachedBlocks = null;
            anchorBlock = null;
            placedCount = 0;
            totalCount = 0;
            lastCompletionTime = 0;
            spawnTime = 0;
            customStructureName = "";
            wrongBlockPositions.clear();
            buttonBounds.clear();
            buttonActions.clear();
            buttonLabels.clear();
        }
    }

    private static List<CachedBlock> loadStructure(ResourceLocation loc) {
        try {
            String path = "data/" + loc.getNamespace() + "/" + loc.getPath() + ".nbt";
            InputStream is = PlacementProjector.class.getClassLoader().getResourceAsStream(path);
            if (is == null) is = PlacementProjector.class.getResourceAsStream("/" + path);
            if (is == null) return null;

            CompoundTag nbt = NbtIo.readCompressed(is);
            return parseNbt(nbt);
        } catch (Exception e) {
            // Logger removed
            return null;
        }
    }

    private static List<CachedBlock> parseNbt(CompoundTag nbt) {
        List<CachedBlock> blocks = new ArrayList<>();
        ListTag paletteNbt = nbt.getList("palette", 10);
        ListTag blocksNbt = nbt.getList("blocks", 10);

        BlockState[] palette = new BlockState[paletteNbt.size()];
        for (int i = 0; i < paletteNbt.size(); i++) {
            String name = paletteNbt.getCompound(i).getString("Name");
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(name));
            palette[i] = (block != null) ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }
        List<BlockPos> rawPositions = new ArrayList<>();
        List<Integer> rawStates = new ArrayList<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;

        for (int i = 0; i < blocksNbt.size(); i++) {
            CompoundTag b = blocksNbt.getCompound(i);
            ListTag posTag = b.getList("pos", 3);
            int x = posTag.getInt(0), y = posTag.getInt(1), z = posTag.getInt(2);
            int stateIdx = b.getInt("state");

            rawPositions.add(new BlockPos(x, y, z));
            rawStates.add(stateIdx);
            minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
        }

        for (int i = 0; i < rawPositions.size(); i++) {
            BlockPos pos = rawPositions.get(i);
            int stateIdx = rawStates.get(i);
            if (stateIdx < palette.length && !palette[stateIdx].isAir()) {
                blocks.add(new CachedBlock(
                        new BlockPos(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ),
                        palette[stateIdx]
                ));
            }
        }
        return blocks;
    }

    private static BlockPos rotatePosition(BlockPos pos, int rotIndex, int maxX, int maxZ) {
        if (rotIndex == 0) return pos;
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return switch (rotIndex) {
            case 1 -> new BlockPos(maxZ - z, y, x);
            case 2 -> new BlockPos(maxX - x, y, maxZ - z);
            case 3 -> new BlockPos(z, y, maxX - x);
            default -> pos;
        };
    }

    public static void updateProgress() {
        synchronized (LOCK) {
            if (state != State.PLACED || anchorBlock == null || cachedBlocks == null) {
                placedCount = 0;
                totalCount = 0;
                wrongBlockPositions.clear();
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            totalCount = cachedBlocks.size();
            int matches = 0;
            wrongBlockPositions.clear();

            int maxX = 0, maxZ = 0;
            for (CachedBlock b : cachedBlocks) {
                if (b.pos().getX() > maxX) maxX = b.pos().getX();
                if (b.pos().getZ() > maxZ) maxZ = b.pos().getZ();
            }

            for (CachedBlock cb : cachedBlocks) {
                BlockPos rotatedPos = rotatePosition(cb.pos(), rotationIndex, maxX, maxZ);
                BlockPos worldPos = anchorBlock.offset(rotatedPos.getX(), rotatedPos.getY(), rotatedPos.getZ());
                BlockState worldState = mc.level.getBlockState(worldPos);

                Block worldBlock = worldState.getBlock();
                Block expectedBlock = cb.state().getBlock();

                if (worldBlock == expectedBlock) {
                    matches++;
                } else {
                    if (!worldState.isAir()) {
                        wrongBlockPositions.add(cb.pos());
                    }
                }
            }

            placedCount = matches;

            if (placedCount == totalCount && totalCount > 0 && lastCompletionTime == 0) {
                lastCompletionTime = System.currentTimeMillis();

                if (mc.level != null && anchorBlock != null) {
                    mc.level.playLocalSound(anchorBlock, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.4f, false);
                    mc.level.playLocalSound(anchorBlock, net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f, false);

                    java.util.Random rand = new java.util.Random();
                    for (int i = 0; i < 60; i++) {
                        double px = anchorBlock.getX() + (rand.nextDouble() * maxX);
                        double py = anchorBlock.getY() + (rand.nextDouble() * 3);
                        double pz = anchorBlock.getZ() + (rand.nextDouble() * maxZ);

                        mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, px, py, pz, (rand.nextDouble() - 0.5) * 0.2, 0.1, (rand.nextDouble() - 0.5) * 0.2);
                        mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.05, 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) return;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;

        synchronized (LOCK) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            if (state == State.PLACED && event.getEntity().isShiftKeyDown() && !(mc.screen instanceof ProjectionControlScreen)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDeleteAttemptTime < DELETE_CONFIRM_WINDOW) {
                    clear();
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 0.6f));
                    event.getEntity().displayClientMessage(Component.translatable("guide.projector.deleted"), true);
                } else {
                    lastDeleteAttemptTime = currentTime;
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS, 0.5f));
                    event.getEntity().displayClientMessage(Component.translatable("guide.projector.delete_warn"), true);
                }
                event.setCanceled(true);
                return;
            }

            if (state != State.READY) return;
            if (event.getEntity().isShiftKeyDown()) return;

            HitResult hit = mc.hitResult;
            if (!(hit instanceof BlockHitResult bhr)) return;

            BlockPos clicked = bhr.getBlockPos();

            cachedBlocks = loadStructure(activeStructure);
            if (cachedBlocks == null || cachedBlocks.isEmpty()) {
                mc.player.displayClientMessage(Component.translatable("guide.projector.not_found"), true);
                state = State.INACTIVE;
                return;
            }

            int maxX = 0, maxZ = 0;
            for (CachedBlock cb : cachedBlocks) {
                if (cb.pos().getX() > maxX) maxX = cb.pos().getX();
                if (cb.pos().getZ() > maxZ) maxZ = cb.pos().getZ();
            }

            int centerX = clicked.getX() - (maxX / 2);
            int centerY = clicked.getY() + 1;
            int centerZ = clicked.getZ() - (maxZ / 2);

            anchorBlock = new BlockPos(centerX, centerY, centerZ);
            state = State.PLACED;
            rotationIndex = 0;
            spawnTime = System.currentTimeMillis();

            mc.level.playLocalSound(anchorBlock, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.2f, false);
            mc.player.displayClientMessage(Component.translatable("guide.projector.placed"), true);

            updateProgress();

            mc.tell(() -> mc.setScreen(new ProjectionControlScreen()));

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onRightClickAir(PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getLevel().isClientSide()) return;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;

        synchronized (LOCK) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            if (state == State.PLACED && event.getEntity().isShiftKeyDown() && mc.screen == null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDeleteAttemptTime < DELETE_CONFIRM_WINDOW) {
                    clear();
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 0.6f));
                    event.getEntity().displayClientMessage(Component.translatable("guide.projector.deleted"), true);
                } else {
                    lastDeleteAttemptTime = currentTime;
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS, 0.5f));
                    event.getEntity().displayClientMessage(Component.translatable("guide.projector.delete_warn"), true);
                }
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;

        synchronized (LOCK) {
            if (state != State.PLACED || anchorBlock == null || cachedBlocks == null) return;

            if (mc.level.getGameTime() % 20 == 0) {
                updateProgress();
            }

            net.minecraft.world.phys.Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

            int maxX = 0, maxZ = 0;
            for (CachedBlock cb : cachedBlocks) {
                if (cb.pos().getX() > maxX) maxX = cb.pos().getX();
                if (cb.pos().getZ() > maxZ) maxZ = cb.pos().getZ();
            }

            PoseStack pose = event.getPoseStack();
            pose.pushPose();

            double renderX = anchorBlock.getX() - camPos.x;
            double renderY = anchorBlock.getY() - camPos.y;
            double renderZ = anchorBlock.getZ() - camPos.z;
            pose.translate(renderX, renderY, renderZ);
            RenderSystem.enableDepthTest();
            RenderSystem.polygonOffset(-1.0f, -1.0f);
            RenderSystem.enablePolygonOffset();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            long timePassed = System.currentTimeMillis() - spawnTime;
            float animationProgress = timePassed / 1000.0f;
            if (animationProgress > 1.0f) animationProgress = 1.0f;
            float baseAlpha = 0.4f * animationProgress;

            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            net.minecraft.client.renderer.RenderType translucentType = net.minecraft.client.renderer.RenderType.translucent();

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, baseAlpha);

            for (CachedBlock cb : cachedBlocks) {
                BlockPos rotatedPos = rotatePosition(cb.pos(), rotationIndex, maxX, maxZ);
                BlockPos worldPos = anchorBlock.offset(rotatedPos.getX(), rotatedPos.getY(), rotatedPos.getZ());
                BlockState worldState = mc.level.getBlockState(worldPos);

                if (worldState.getBlock() == cb.state().getBlock()) {
                    continue;
                }

                pose.pushPose();
                pose.translate(rotatedPos.getX(), rotatedPos.getY(), rotatedPos.getZ());

                pose.translate(0.5, 0.0, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(rotationIndex * -90f));
                pose.translate(-0.5, 0.0, -0.5);

                mc.getBlockRenderer().renderSingleBlock(
                        cb.state(),
                        pose,
                        bufferSource,
                        15728880,
                        OverlayTexture.NO_OVERLAY,
                        net.minecraftforge.client.model.data.ModelData.EMPTY,
                        translucentType
                );

                pose.popPose();
            }

            bufferSource.endBatch(translucentType);
            RenderSystem.disablePolygonOffset();

            if (!wrongBlockPositions.isEmpty()) {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.disableCull();
                RenderSystem.disableDepthTest();

                var lineBuffer = bufferSource.getBuffer(RenderType.lines());

                for (BlockPos badPos : wrongBlockPositions) {
                    BlockPos rotatedPos = rotatePosition(badPos, rotationIndex, maxX, maxZ);
                    float x = rotatedPos.getX();
                    float y = rotatedPos.getY();
                    float z = rotatedPos.getZ();

                    var matrix = pose.last().pose();
                    int r = 255, g = 40, b = 40, a = 255;

                    // 12 линий куба
                    lineBuffer.vertex(matrix, x, y, z).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x + 1, y, z).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x + 1, y, z).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x + 1, y, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x + 1, y, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x, y, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x, y, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x, y, z).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x, y + 1, z).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x + 1, y + 1, z).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x + 1, y + 1, z).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x + 1, y + 1, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x + 1, y + 1, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x, y + 1, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x, y + 1, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x, y + 1, z).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x, y, z).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x, y + 1, z).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x + 1, y, z).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x + 1, y + 1, z).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x + 1, y, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x + 1, y + 1, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();

                    lineBuffer.vertex(matrix, x, y, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();
                    lineBuffer.vertex(matrix, x, y + 1, z + 1).color(r, g, b, a).normal(0, 1, 0).endVertex();
                }

                bufferSource.endBatch(RenderType.lines());

                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            pose.popPose();
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onRenderGui(RenderGuiEvent.Post event) {
        synchronized (LOCK) {
            if (state != State.PLACED) return;

            Minecraft mc = Minecraft.getInstance();
            GuiGraphics gui = event.getGuiGraphics();
            var font = mc.font;
            int screenWidth = event.getWindow().getGuiScaledWidth();

            if (totalCount > 0 && placedCount == totalCount) {
                if (lastCompletionTime != 0 && System.currentTimeMillis() - lastCompletionTime > 3000) {
                    clear();
                    lastCompletionTime = 0;

                    if (mc.screen instanceof ProjectionControlScreen) {
                        mc.tell(() -> mc.setScreen(null));
                    }
                    return;
                }
            } else {
                lastCompletionTime = 0;
            }

            int barW = 182, barH = 5;
            int barX = (screenWidth - barW) / 2;
            int barY = 18;

            ResourceLocation bossBarTexture = ResourceLocation.withDefaultNamespace("textures/gui/bars.png");
            float progress = totalCount > 0 ? (float) placedCount / totalCount : 0.0f;
            int progressW = (int) (progress * barW);

            int backgroundY = getBossBarTextureY(progress, false);
            int progressY = getBossBarTextureY(progress, true);

            gui.blit(bossBarTexture, barX, barY, 0, backgroundY, barW, barH, 256, 256);

            if (progressW > 0) {
                gui.blit(bossBarTexture, barX, barY, 0, progressY, progressW, barH, 256, 256);
            }

            Component mainText = placedCount == totalCount ?
                    Component.translatable("guide.projector.hud.finished") :
                    Component.literal(customStructureName).withStyle(net.minecraft.ChatFormatting.AQUA);

            int remaining = totalCount - placedCount;
            Component subText = placedCount == totalCount ?
                    Component.translatable("guide.projector.hud.closing") :
                    Component.translatable("guide.projector.hud.progress", placedCount, totalCount, remaining);

            gui.drawCenteredString(font, mainText.getString(), screenWidth / 2, barY - 12, 0xFFFFFF);
            gui.drawCenteredString(font, subText.getString(), screenWidth / 2, barY + barH + 4, 0xFFFFFF);

            if (!(mc.screen instanceof ProjectionControlScreen)) return;

            double guiScale = event.getWindow().getGuiScale();
            int mouseX = (int) (mc.mouseHandler.xpos() / guiScale);
            int mouseY = (int) (mc.mouseHandler.ypos() / guiScale);

            int menuY = HUD_Y + 36;
            int panelW = (BTN_W * 2) + GAP + 12;
            int panelH = (BTN_H * 5) + (GAP * 4) + 28;

            gui.fill(HUD_X, menuY, HUD_X + panelW, menuY + panelH, 0xD0101215);
            renderFrame(gui, HUD_X, menuY, HUD_X + panelW, menuY + panelH, 0x4000D0FF);

            Component titleText = Component.translatable("guide.projector.gui.title");
            int titleW = font.width(titleText);
            gui.drawString(font, titleText, HUD_X + (panelW - titleW) / 2, menuY + 7, 0x00D0FF, false);

            for (int i = 0; i < buttonBounds.size(); i++) {
                int[] b = buttonBounds.get(i);
                boolean hover = mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3];

                int bgColor = hover ? 0x3000D0FF : 0x15FFFFFF;
                int frameColor = hover ? 0xFF00D0FF : 0x25FFFFFF;
                int textColor = hover ? 0xFFFFFF : 0xBBBBBB;

                String labelKey = buttonLabels.get(i);
                if (labelKey.contains("done") && hover) { bgColor = 0x3000FF55; frameColor = 0xFF00FF55; }
                if (labelKey.contains("cancel") && hover) { bgColor = 0x30FF2244; frameColor = 0xFFFF2244; }

                gui.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], bgColor);
                renderFrame(gui, b[0], b[1], b[0] + b[2], b[1] + b[3], frameColor);

                Component labelText = Component.translatable(labelKey);
                int textW = font.width(labelText);
                int textX = b[0] + (b[2] - textW) / 2;
                int textY = b[1] + (b[3] - 8) / 2;

                gui.drawString(font, labelText, textX, textY, textColor, false);
            }
        }
    }

    private static void addBtn(int x, int y, String label, Runnable action) {
        buttonBounds.add(new int[]{x, y, BTN_W, BTN_H});
        buttonActions.add(action);
        buttonLabels.add(label);
    }

    private static void renderFrame(GuiGraphics gui, int x1, int y1, int x2, int y2, int color) {
        gui.fill(x1, y1, x2, y1 + 1, color);
        gui.fill(x1, y2 - 1, x2, y2, color);
        gui.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        gui.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private static int getBossBarTextureY(float progress, boolean isProgressOffset) {
        net.minecraft.world.BossEvent.BossBarColor barColor = net.minecraft.world.BossEvent.BossBarColor.PINK;
        if (progress > 0.4f && progress < 0.9f) {
            barColor = net.minecraft.world.BossEvent.BossBarColor.BLUE;
        } else if (progress >= 0.9f) {
            barColor = net.minecraft.world.BossEvent.BossBarColor.GREEN;
        }
        int backgroundY = barColor.ordinal() * 5 * 2;
        return isProgressOffset ? (backgroundY + 5) : backgroundY;
    }

    public static class ProjectionControlScreen extends net.minecraft.client.gui.screens.Screen {
        public ProjectionControlScreen() {
            super(Component.translatable("guide.projector.gui.title"));
        }

        @Override
        protected void init() {
            super.init();
            synchronized (LOCK) {
                buttonBounds.clear(); buttonActions.clear(); buttonLabels.clear();

                int menuY = HUD_Y + 36;
                int startX = HUD_X + 6;
                int startY = menuY + 22;
                int currentX = startX;
                int currentY = startY;

                net.minecraft.core.Direction facing = Minecraft.getInstance().player != null ?
                        Minecraft.getInstance().player.getDirection() : net.minecraft.core.Direction.NORTH;

                final int fdx, fdz;
                final int rdx, rdz;

                switch (facing) {
                    case SOUTH -> { fdx = 0;  fdz = 1;  rdx = -1; rdz = 0;  }
                    case WEST  -> { fdx = -1; fdz = 0;  rdx = 0;  rdz = -1; }
                    case EAST  -> { fdx = 1;  fdz = 0;  rdx = 0;  rdz = 1;  }
                    default    -> { fdx = 0;  fdz = -1; rdx = 1;  rdz = 0;  }
                }

                addBtn(currentX, currentY, "guide.projector.btn.x_minus", () -> moveProjection(-fdx, 0, -fdz)); currentX += BTN_W + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.x_plus",  () -> moveProjection(fdx, 0, fdz));

                currentX = startX; currentY += BTN_H + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.y_minus", () -> moveProjection(0, -1, 0)); currentX += BTN_W + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.y_plus",  () -> moveProjection(0, 1, 0));

                currentX = startX; currentY += BTN_H + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.z_minus", () -> moveProjection(-rdx, 0, -rdz)); currentX += BTN_W + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.z_plus",  () -> moveProjection(rdx, 0, rdz));

                currentX = startX; currentY += BTN_H + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.rot_minus", () -> { rotationIndex = (rotationIndex + 3) % 4; updateProgress(); }); currentX += BTN_W + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.rot_plus",  () -> { rotationIndex = (rotationIndex + 1) % 4; updateProgress(); });

                currentX = startX; currentY += BTN_H + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.done",   () -> Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(null))); currentX += BTN_W + GAP;
                addBtn(currentX, currentY, "guide.projector.btn.cancel", () -> { clear(); Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(null)); });
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                synchronized (LOCK) {
                    if (state == State.PLACED && !buttonBounds.isEmpty()) {
                        for (int i = 0; i < buttonBounds.size(); i++) {
                            int[] b = buttonBounds.get(i);
                            if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {

                                Minecraft mc = Minecraft.getInstance();
                                if (mc.player != null) {
                                    String labelKey = buttonLabels.get(i);
                                    if (labelKey.contains("done")) {
                                        mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f));
                                    } else if (labelKey.contains("cancel")) {
                                        mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 0.5f));
                                    } else {
                                        mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.SCULK_CLICKING, 1.5f));
                                    }
                                }

                                Runnable action = buttonActions.get(i);
                                action.run();

                                return true;
                            }
                        }
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public void onClose() {
            super.onClose();
        }
    }
}