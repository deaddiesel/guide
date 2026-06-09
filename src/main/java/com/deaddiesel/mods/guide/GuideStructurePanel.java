package com.deaddiesel.mods.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuideStructurePanel {
    private static final int TAB_HEIGHT = 16;
    private static final int TAB_WIDTH = 44;
    private static final int TAB_GAP = 2;
    private static final int TAB_Y = 34;

    private static final int FRAME_SIZE = 140;
    private static final int NEON_COLOR = 0x4000FFFF;
    private static final int ACTIVE_BG = 0xFF555555;
    private static final int INACTIVE_BG = 0xFF222222;
    private static final int ACTIVE_TEXT = 0x00FFCC;
    private static final int INACTIVE_TEXT = 0x888888;
    private static final int LAYER_TEXT_COLOR = 0x55FF55;

    private final List<GuideModels.StructureTabEntry> structureTabs = new ArrayList<>();
    private int activeStructureIndex = 0;

    public float zoomScale = 1.0f;
    public float rotationY = 0.0f;
    public int currentRenderLayer = -1;
    public boolean isAutoRotating = false;

    private int contentLeftX = 0;
    private int syncLayerBtnY = 0;

    public void syncLayout(int leftX, int layerBtnY) {
        this.contentLeftX = leftX;
        this.syncLayerBtnY = layerBtnY;
    }

    public List<GuideModels.StructureTabEntry> getStructureTabs() {
        return Collections.unmodifiableList(structureTabs);
    }

    public void addStructureTab(GuideModels.StructureTabEntry entry) {
        if (entry != null && structureTabs.size() < 3) structureTabs.add(entry);
    }

    public void clearTabs() {
        structureTabs.clear();
        activeStructureIndex = 0;
    }

    public void setActiveStructureIndex(int index) {
        if (index >= 0 && index < structureTabs.size()) {
            activeStructureIndex = index;
            zoomScale = 1.0f; rotationY = 0.0f; currentRenderLayer = -1;
        }
    }

    public int getActiveStructureIndex() {
        return activeStructureIndex;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (structureTabs.isEmpty()) return;
        var mc = Minecraft.getInstance();
        var font = mc.font;

        int tabOffset = 2;
        int tabCount = structureTabs.size();

        for (int i = 0; i < tabCount; i++) {
            GuideModels.StructureTabEntry entry = structureTabs.get(i);
            boolean isActive = (i == activeStructureIndex);
            int tabX = contentLeftX + tabOffset + i * (TAB_WIDTH + TAB_GAP);

            String displayText = "#" + (i + 1);
            guiGraphics.fill(tabX, TAB_Y, tabX + TAB_WIDTH, TAB_Y + TAB_HEIGHT, isActive ? ACTIVE_BG : INACTIVE_BG);

            int textColor = isActive ? ACTIVE_TEXT : INACTIVE_TEXT;
            int textX = tabX + (TAB_WIDTH / 2) - (font.width(displayText) / 2);
            guiGraphics.drawString(font, displayText, textX, TAB_Y + 4, textColor, false);

            if (isActive) {
                guiGraphics.fill(tabX, TAB_Y + TAB_HEIGHT - 1, tabX + TAB_WIDTH, TAB_Y + TAB_HEIGHT, ACTIVE_TEXT);
            }

            if (mouseX >= tabX && mouseX <= tabX + TAB_WIDTH && mouseY >= TAB_Y && mouseY <= TAB_Y + TAB_HEIGHT) {
                Component tooltipText = entry.hasCustomName()
                        ? Component.literal(entry.tabName())
                        : Component.translatable("guide.tooltip.missing_tab_name").withStyle(net.minecraft.ChatFormatting.YELLOW);
                guiGraphics.renderTooltip(font, tooltipText, mouseX, mouseY);
            }
        }

        int frameTopY = TAB_Y + TAB_HEIGHT + 5;
        int frameX = contentLeftX;
        int frameCenterX = contentLeftX + FRAME_SIZE / 2;
        int frameCenterY = frameTopY + FRAME_SIZE / 2;

        renderStructureFrame(guiGraphics, frameX, frameTopY);

        if (!structureTabs.isEmpty() && activeStructureIndex < structureTabs.size()) {
            var activeEntry = structureTabs.get(activeStructureIndex);
            StructureRenderer.renderStructureInGui(guiGraphics, activeEntry.location(), frameCenterX, frameCenterY, zoomScale, rotationY, currentRenderLayer, mouseX, mouseY);
        }

        String layerVal = currentRenderLayer == -1 ? Component.translatable("guide.text.all").getString() : String.valueOf(currentRenderLayer);
        Component layerDisplay = Component.translatable("guide.text.layer").append(": " + layerVal);

        int frameBottomY = frameTopY + FRAME_SIZE;
        int textY = frameBottomY + 10;
        int buttonTopY = syncLayerBtnY;

        if (textY + 10 > buttonTopY - 8) textY = frameBottomY - 20;

        guiGraphics.drawCenteredString(font, layerDisplay, frameCenterX, textY, LAYER_TEXT_COLOR);
    }

    private void renderStructureFrame(GuiGraphics gui, int x, int y) {
        gui.fill(x, y, x + FRAME_SIZE, y + 1, NEON_COLOR);
        gui.fill(x, y + FRAME_SIZE - 1, x + FRAME_SIZE, y + FRAME_SIZE, NEON_COLOR);
        gui.fill(x, y, x + 1, y + FRAME_SIZE, NEON_COLOR);
        gui.fill(x + FRAME_SIZE - 1, y, x + FRAME_SIZE, y + FRAME_SIZE, NEON_COLOR);
    }

    public boolean handleTabClick(double mx, double my) {
        if (structureTabs.isEmpty()) return false;
        int tabOffset = 2;
        int tabCount = structureTabs.size();

        for (int i = 0; i < tabCount; i++) {
            int tabX = contentLeftX + tabOffset + i * (TAB_WIDTH + TAB_GAP);
            if (mx >= tabX && mx <= tabX + TAB_WIDTH && my >= TAB_Y && my <= TAB_Y + TAB_HEIGHT) {
                setActiveStructureIndex(i);
                return true;
            }
        }
        return false;
    }
}