package com.deaddiesel.mods.guide;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class GuideEditBox extends EditBox {
    private static final int MAX_SEARCH_LENGTH = 64;
    private static final int TEXT_COLOR_WHITE = 0xFFFFFF;

    public GuideEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, Component.empty());
        this.setSuggestion(message.getString());
        this.setBordered(true);
        this.setMaxLength(MAX_SEARCH_LENGTH);
        this.setTextColor(TEXT_COLOR_WHITE);
    }
}