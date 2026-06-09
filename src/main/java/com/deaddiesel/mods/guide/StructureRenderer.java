package com.deaddiesel.mods.guide;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector4f;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StructureRenderer {
    private static final float BASE_SCALE = 14.0F;
    private static final float HOVER_RADIUS = 7.0f;
    private static final float ROTATION_X = 22.0f;
    private static final float Z_OFFSET = 500.0f;
    private static final float HOVER_BRIGHTNESS = 1.15f;
    private static final int MAX_SIZE = 16;

    private static final List<StructureTemplate.StructureBlockInfo> manualBlocks = new ArrayList<>();
    private static ResourceLocation lastLoc = null;
    private static Vec3i cachedSize = null;

    private static BlockState currentHoveredState = null;
    private static BlockPos currentHoveredPos = null;

    public static void resetCache() {
        manualBlocks.clear();
        lastLoc = null;
        cachedSize = null;
        currentHoveredState = null;
        currentHoveredPos = null;
    }

    public static void renderStructureInGui(GuiGraphics gui, ResourceLocation loc, int x, int y, float scale, float rot, int layer, int mx, int my) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (!Objects.equals(lastLoc, loc)) {
            resetCache();
            loadStructure(loc);
        }

        if (manualBlocks.isEmpty()) return;

        currentHoveredState = null;
        currentHoveredPos = null;

        PoseStack pose = gui.pose();
        pose.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        Lighting.setupFor3DItems();

        pose.translate((float)x, (float)y, Z_OFFSET);
        float finalScale = BASE_SCALE * scale;

        pose.scale(finalScale, -finalScale, finalScale);
        pose.mulPose(Axis.XP.rotationDegrees(ROTATION_X));
        pose.mulPose(Axis.YP.rotationDegrees(rot));

        Vec3i size = getCachedSize();
        pose.translate(-size.getX() / 2.0F, -size.getY() / 2.0F, -size.getZ() / 2.0F);

        detectHover(layer, mx, my, scale, pose);
        renderBlocks(mc, layer, pose);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        mc.renderBuffers().bufferSource().endBatch();

        pose.popPose();
        Lighting.setupForFlatItems();
    }

    private static void loadStructure(ResourceLocation loc) {
        // Logger removed

        try (InputStream is = openStructureStream(loc)) {
            if (is == null) {
                reportError("guide.error.structure_not_found");
                return;
            }

            CompoundTag nbt = NbtIo.readCompressed(is);

            if (validateSize(nbt)) {
                parseNbt(nbt);
                lastLoc = loc;
                // Logger removed
            }

        } catch (Exception e) {
            // Logger removed
            reportError("guide.error.nbt_load_failed");
        }
    }

    private static InputStream openStructureStream(ResourceLocation loc) {
        String resourcePath = "data/" + loc.getNamespace() + "/" + loc.getPath() + ".nbt";
        InputStream is = StructureRenderer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            is = StructureRenderer.class.getResourceAsStream("/" + resourcePath);
        }
        return is;
    }

    private static boolean validateSize(CompoundTag nbt) {
        if (!nbt.contains("size", 9)) return true;
        var sizeList = nbt.getList("size", 3);
        int sx = sizeList.getInt(0), sy = sizeList.getInt(1), sz = sizeList.getInt(2);
        if (sx > MAX_SIZE || sy > MAX_SIZE || sz > MAX_SIZE) {
            // Logger removed
            reportError("guide.error.structure_too_large");
            return false;
        }
        return true;
    }

    private static void reportError(String key) {
        var screen = Minecraft.getInstance().screen;
        if (screen instanceof GuideScreen gs) gs.addStructureError(key);
    }

    private static Vec3i getCachedSize() {
        if (cachedSize == null) cachedSize = calculateSize();
        return cachedSize;
    }

    private static void detectHover(int layer, int mx, int my, float scale, PoseStack pose) {
        float bestZ = -Float.MAX_VALUE;
        float radius = HOVER_RADIUS * scale;
        for (var info : manualBlocks) {
            if (layer != -1 && info.pos().getY() > layer) continue;
            Vector4f center = new Vector4f(info.pos().getX()+0.5f, info.pos().getY()+0.5f, info.pos().getZ()+0.5f, 1.0f);
            center.mul(pose.last().pose());
            if (Math.abs(center.x()-mx) < radius && Math.abs(center.y()-my) < radius && center.z() > bestZ) {
                bestZ = center.z();
                currentHoveredState = info.state();
                currentHoveredPos = info.pos();
            }
        }
    }

    private static void renderBlocks(Minecraft mc, int layer, PoseStack pose) {
        if (manualBlocks.isEmpty()) return;

        var bufferSource = mc.renderBuffers().bufferSource();
        var level = mc.level;
        if (level == null) return;

        for (var info : manualBlocks) {
            if (layer != -1 && info.pos().getY() > layer) continue;

            pose.pushPose();
            pose.translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());

            boolean hover = Objects.equals(info.pos(), currentHoveredPos);
            RenderSystem.setShaderColor(hover ? HOVER_BRIGHTNESS : 1.0f, hover ? HOVER_BRIGHTNESS : 1.0f, hover ? HOVER_BRIGHTNESS : 1.0f, 1.0f);

            mc.getBlockRenderer().renderSingleBlock(
                    info.state(),
                    pose,
                    bufferSource,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    net.minecraftforge.client.model.data.ModelData.EMPTY,
                    net.minecraft.client.renderer.RenderType.cutout()
            );

            pose.popPose();
        }
    }

    public static BlockState getHoveredBlock() { return currentHoveredState; }

    private static void parseNbt(CompoundTag nbt) {
        manualBlocks.clear();

        if (!nbt.contains("palette", 9)) {
            // Logger removed
            return;
        }
        if (!nbt.contains("blocks", 9)) {
            // Logger removed
            return;
        }

        ListTag paletteNbt = nbt.getList("palette", 10);
        ListTag blocksNbt = nbt.getList("blocks", 10);

        // Logger removed

        BlockState[] palette = new BlockState[paletteNbt.size()];
        for (int i = 0; i < paletteNbt.size(); i++) {
            CompoundTag p = paletteNbt.getCompound(i);
            String name = p.getString("Name");
            var block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(name));
            palette[i] = (block != null) ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        for (int i = 0; i < blocksNbt.size(); i++) {
            CompoundTag b = blocksNbt.getCompound(i);
            ListTag pos = b.getList("pos", 3);
            BlockPos blockPos = new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2));
            int stateIdx = b.getInt("state");

            if (stateIdx < 0 || stateIdx >= palette.length) continue;

            BlockState state = palette[stateIdx];
            if (state != null && !state.isAir()) {
                manualBlocks.add(new StructureTemplate.StructureBlockInfo(blockPos, state, null));
            }
        }
        cachedSize = null;
    }

    private static Vec3i calculateSize() {
        if (manualBlocks.isEmpty()) return Vec3i.ZERO;
        int mx = Integer.MIN_VALUE, my = Integer.MIN_VALUE, mz = Integer.MIN_VALUE;
        for (var b : manualBlocks) {
            mx = Math.max(mx, b.pos().getX());
            my = Math.max(my, b.pos().getY());
            mz = Math.max(mz, b.pos().getZ());
        }
        return new Vec3i(mx + 1, my + 1, mz + 1);
    }
}