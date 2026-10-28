package com.megaradar.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Bottone "Cerca" disegnato a mano (angoli arrotondati di 1 pixel, come nel wireframe).
 * Grigio e disattivato di default, verde quando tutti gli slot sono pieni.
 */
public class SearchButton extends AbstractButton {

    private static final int BORDER = 0xFF1E1E1E;

    private static final int GRAY_FILL = 0xFFAAAAAA;
    private static final int GRAY_HIGHLIGHT = 0xFFC8C8C8;
    private static final int GRAY_SHADOW = 0xFF7E7E7E;
    private static final int GRAY_TEXT = 0xFF3C3C3C;

    private static final int GREEN_FILL = 0xFF3E9B2A;
    private static final int GREEN_FILL_HOVER = 0xFF4FB838;
    private static final int GREEN_HIGHLIGHT = 0xFF86DB63;
    private static final int GREEN_SHADOW = 0xFF2A6B1C;

    private final Runnable action;

    public SearchButton(int x, int y, int width, int height, Component message, Runnable action) {
        super(x, y, width, height, message);
        this.action = action;
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        boolean ready = this.active;
        boolean hover = ready && isHoveredOrFocused();

        int fill = ready ? (hover ? GREEN_FILL_HOVER : GREEN_FILL) : GRAY_FILL;
        int highlight = ready ? GREEN_HIGHLIGHT : GRAY_HIGHLIGHT;
        int shadow = ready ? GREEN_SHADOW : GRAY_SHADOW;

        // bordo con angoli smussati
        graphics.fill(x + 1, y, x + w - 1, y + h, BORDER);
        graphics.fill(x, y + 1, x + w, y + h - 1, BORDER);
        // corpo + rilievo
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, highlight);
        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, shadow);

        Font font = Minecraft.getInstance().font;
        Component label = getMessage();
        int tx = x + (w - font.width(label)) / 2;
        int ty = y + (h - 8) / 2;
        graphics.drawString(font, label, tx, ty, ready ? 0xFFFFFFFF : GRAY_TEXT, ready);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
