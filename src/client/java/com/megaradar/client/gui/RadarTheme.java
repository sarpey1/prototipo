package com.megaradar.client.gui;

import com.megaradar.MegaRadar;
import com.megaradar.radar.RadarType;
import net.minecraft.resources.ResourceLocation;

/** Palette della GUI, ripresa dall'item: ghiera viola (Megaroidi) o azzurra (Megasite), schermo a fosfori verdi. */
public record RadarTheme(ResourceLocation texture, int accent, int accentLight) {

    public static final RadarTheme MEGAROID = new RadarTheme(
            MegaRadar.id("textures/gui/radar_megaroid.png"), 0xFF9646C8, 0xFFE678FF);
    public static final RadarTheme MEGA_CITY = new RadarTheme(
            MegaRadar.id("textures/gui/radar_mega_city.png"), 0xFF2896BE, 0xFF6EF0FF);

    // Colori comuni
    public static final int PHOSPHOR = 0xFF3CC86E;        // verde dello schermo
    public static final int PHOSPHOR_RGB = 0x3CC86E;
    public static final int PHOSPHOR_BRIGHT = 0xFF5CFF8E;
    public static final int SCREEN_SLOT = 0xFF0F2318;     // fondo degli slot sul decagono
    public static final int SCREEN_SLOT_EDGE = 0xFF2A5A3C;
    public static final int LABEL = 0xFF9AA3B2;
    public static final int ERROR = 0xFFFF6B6B;
    public static final int MUTED = 0xFF8A93A3;

    public static RadarTheme of(RadarType type) {
        return type == RadarType.MEGA_CITY ? MEGA_CITY : MEGAROID;
    }

    /** Interpolazione lineare tra due colori ARGB opachi. */
    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
}
