package com.megaradar.client.gui;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Bottone "Cerca" in stile pannello del radar.
 * Grigio (spento) di default, verde fosfori con un alone pulsante quando tutti gli slot sono pieni.
 */
public class SearchButton extends AbstractButton {

    private static final int BORDER = 0xFF0E1014;

    private static final int OFF_FILL = 0xFF3A3F4A;
    private static final int OFF_HIGHLIGHT = 0xFF4E5461;
    private static final int OFF_SHADOW = 0xFF262A32;
    private static final int OFF_TEXT = 0xFF8A93A3;

    private static final int ON_FILL = 0xFF2FA352;
    private static final int ON_FILL_HOVER = 0xFF3CC86E;
    private static final int ON_HIGHLIGHT = 0xFF6EE89A;
    private static final int ON_SHADOW = 0xFF1C6A35;

    private static final long PULSE_MS = 1200;

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

        if (ready) {
            // Alone pulsante: il radar è pronto
            double phase = (Util.getMillis() % PULSE_MS) / (double) PULSE_MS * Math.PI * 2;
            float pulse = (float) (0.5 + 0.5 * Math.sin(phase));
            int outer = (int) (0x20 + 0x50 * pulse);
            int inner = (int) (0x60 + 0x80 * pulse);
            graphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, (outer << 24) | RadarTheme.PHOSPHOR_RGB);
            graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, (inner << 24) | RadarTheme.PHOSPHOR_RGB);
        }

        int fill = ready ? (hover ? ON_FILL_HOVER : ON_FILL) : OFF_FILL;
        int highlight = ready ? ON_HIGHLIGHT : OFF_HIGHLIGHT;
        int shadow = ready ? ON_SHADOW : OFF_SHADOW;

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
        graphics.drawString(font, label, tx, ty, ready ? 0xFFFFFFFF : OFF_TEXT, ready);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
