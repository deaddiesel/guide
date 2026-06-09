package com.deaddiesel.mods.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class BookSelectorScreen extends Screen {

    private static final int BG_COLOR = 0xCC1A1A1A;
    private static final int BORDER_COLOR = 0xFF4A4A4A;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 22;
    private static final int GAP = 4;

    private final List<BookRegistry.BookEntry> availableBooks = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private boolean isDragging = false;
    private double dragStartY = 0;
    private int dragStartOffset = 0;

    public BookSelectorScreen() {
        super(Component.translatable("guide.selector.title"));
    }

    @Override
    protected void init() {
        super.init();
        availableBooks.clear();
        availableBooks.addAll(BookRegistry.getVisibleBooks());
        updateScrollLimits();
        rebuildButtons();
    }

    private void updateScrollLimits() {
        int buttonHeightWithGap = BUTTON_HEIGHT + GAP;
        int totalHeight = availableBooks.size() * buttonHeightWithGap;
        int visibleHeight = height - 40 - 28 - 12;

        maxScroll = Math.max(0, totalHeight - visibleHeight);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private void rebuildButtons() {
        clearWidgets();

        int panelY = 20;
        int panelH = height - 40;
        int headerHeight = 28;

        int topMargin = panelY + headerHeight + 6;
        int bottomMargin = panelY + panelH - 6;
        int visibleAreaHeight = bottomMargin - topMargin;
        int buttonHeightWithGap = BUTTON_HEIGHT + GAP;

        int startX = (width - BUTTON_WIDTH) / 2;
        int firstVisible = Math.max(0, scrollOffset / buttonHeightWithGap);
        int lastVisible = Math.min(availableBooks.size() - 1, (scrollOffset + visibleAreaHeight) / buttonHeightWithGap + 1);

        for (int i = firstVisible; i <= lastVisible; i++) {
            BookRegistry.BookEntry book = availableBooks.get(i);
            int btnY = topMargin + i * buttonHeightWithGap - scrollOffset;

            if (btnY + BUTTON_HEIGHT >= topMargin && btnY <= bottomMargin) {
                addRenderableWidget(new BookButton(
                        startX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT,
                        book.getDisplayName(),
                        book.iconPath(),
                        b -> {
                            Minecraft mc = Minecraft.getInstance();
                            mc.setScreen(new GuideScreen(book.namespace()));
                        }
                ));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int panelX = (width - 220) / 2;
        int panelY = 20;
        int panelW = 220;
        int panelH = height - 40;

        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);
        renderFrame(guiGraphics, panelX, panelY, panelX + panelW, panelY + panelH);

        int headerHeight = 28;
        int topMargin = panelY + headerHeight + 6;
        int bottomMargin = panelY + panelH - 6;

        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + headerHeight, 0xCC111111);
        guiGraphics.fill(panelX, panelY + headerHeight - 1, panelX + panelW, panelY + headerHeight, 0xFF4A4A4A);

        Component title = Component.translatable("guide.selector.title");
        String countText = " (" + availableBooks.size() + ")";
        String fullTitle = title.getString() + countText;
        int titleWidth = font.width(fullTitle);
        guiGraphics.drawString(font, fullTitle, panelX + (panelW - titleWidth) / 2, panelY + 9, 0xFFFFFF, false);

        if (availableBooks.isEmpty()) {
            Component noBooks = Component.translatable("guide.selector.no_books");
            guiGraphics.drawCenteredString(font, noBooks, width / 2, height / 2, 0x888888);
        }

        renderScrollbar(guiGraphics, panelX + panelW + 6, panelY, panelH);

        guiGraphics.enableScissor(panelX, topMargin, panelX + panelW, bottomMargin);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
    }

    private void renderFrame(GuiGraphics gui, int x1, int y1, int x2, int y2) {
        gui.fill(x1, y1, x2, y1 + 1, BORDER_COLOR);
        gui.fill(x1, y2 - 1, x2, y2, BORDER_COLOR);
        gui.fill(x1, y1 + 1, x1 + 1, y2 - 1, BORDER_COLOR);
        gui.fill(x2 - 1, y1 + 1, x2, y2 - 1, BORDER_COLOR);
    }

    private void renderScrollbar(GuiGraphics gui, int x, int y, int height) {
        if (maxScroll <= 0) return;

        int trackTop = y + 20;
        int trackBottom = y + height - 20;
        int trackH = trackBottom - trackTop;

        gui.fill(x, trackTop, x + 4, trackBottom, 0x55111111);

        int thumbH = Math.max(10, (int) ((float) trackH * trackH / (maxScroll + trackH)));
        int thumbY = trackTop + (int) ((float) scrollOffset * (trackH - thumbH) / maxScroll);

        gui.fill(x, thumbY, x + 4, thumbY + thumbH, 0xFF8B8B8B);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - amount * 12));
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && maxScroll > 0) {
            int panelX = (width - 220) / 2;
            int panelY = 20;
            int panelH = height - 40;

            int scrollX = panelX + 220 + 6;
            int trackTop = panelY + 20;
            int trackBottom = panelY + panelH - 20;

            if (mouseX >= scrollX && mouseX <= scrollX + 4 && mouseY >= trackTop && mouseY <= trackBottom) {
                isDragging = true;
                dragStartY = mouseY;
                dragStartOffset = scrollOffset;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && maxScroll > 0) {
            int panelY = 20;
            int panelH = height - 40;
            int trackTop = panelY + 20;
            int trackBottom = panelY + panelH - 20;
            int trackH = trackBottom - trackTop;
            int thumbH = Math.max(10, (int) ((float) trackH * trackH / (maxScroll + trackH)));

            double ratio = (mouseY - dragStartY) / (trackH - thumbH);
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, dragStartOffset + ratio * maxScroll));
            rebuildButtons();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft mc = Minecraft.getInstance();
        if (availableBooks.size() == 1) {
            BookRegistry.BookEntry book = availableBooks.get(0);
            mc.setScreen(new GuideScreen(book.namespace()));
        } else {
            super.onClose();
        }
    }

    private static class BookButton extends Button {
        private final ResourceLocation iconLocation;
        private static final int ICON_SIZE = 16;
        private static final int ICON_OFFSET = 4;
        private static final int TEXT_OFFSET = 28;

        public BookButton(int x, int y, int w, int h, Component msg, String iconPath, OnPress onPress) {
            super(x, y, w, h, msg, onPress, DEFAULT_NARRATION);
            this.iconLocation = iconPath.isEmpty() ? null : ResourceLocation.tryParse(iconPath);
        }

        @Override
        protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int bgColor = isActive() ? (isHoveredOrFocused() ? 0xFF3A3A3A : 0xFF2A2A2A) : 0xFF1A1A1A;
            int borderColor = isHoveredOrFocused() ? 0xFF00D0FF : BORDER_COLOR;

            gui.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
            gui.fill(getX(), getY(), getX() + width, getY() + 1, borderColor);
            gui.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderColor);
            gui.fill(getX(), getY(), getX() + 1, getY() + height, borderColor);
            gui.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderColor);

            if (iconLocation != null && Minecraft.getInstance().getResourceManager().getResource(iconLocation).isPresent()) {
                int iconY = getY() + (height - ICON_SIZE) / 2;
                gui.blit(iconLocation, getX() + ICON_OFFSET, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

            var mcFont = Minecraft.getInstance().font;
            int textX = getX() + TEXT_OFFSET;
            int textY = getY() + (height - 8) / 2;
            int textColor = isActive() ? 0xFFFFFF : 0x888888;

            gui.drawString(mcFont, getMessage(), textX, textY, textColor, false);
        }
    }
}
