package com.megaradar.client;

import com.megaradar.item.RadarItem;
import com.megaradar.radar.RadarTargets;
import com.megaradar.radar.RadarType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Finché un radar con un bersaglio è nella hotbar (o nella mano secondaria),
 * in alto a sinistra compare:  [freccia]  Megaroide · 342 m  (1/3)
 * La freccia ruota con la visuale del giocatore, come una bussola.
 */
public final class RadarHud {

    /** 8 direzioni: 0 = avanti, poi in senso orario. */
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    private RadarHud() {}

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui) return;

        // Un radar per tipo: prima la mano secondaria, poi la hotbar
        Map<RadarType, ItemStack> radars = new EnumMap<>(RadarType.class);
        collect(player.getOffhandItem(), radars);
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            collect(player.getInventory().getItem(i), radars);
        }
        if (radars.isEmpty()) return;

        Font font = mc.font;
        int x = 6;
        int y = 6;
        ResourceLocation currentDim = mc.level.dimension().location();

        for (Map.Entry<RadarType, ItemStack> entry : radars.entrySet()) {
            RadarType type = entry.getKey();
            var dataOpt = RadarTargets.read(entry.getValue());
            if (dataOpt.isEmpty()) continue;
            RadarTargets.Data d = dataOpt.get();

            Component line;
            if (!d.hasCurrent()) {
                line = Component.translatable("megaradar.hud.done", type.displayName());
            } else if (!d.dimension().equals(currentDim)) {
                line = Component.translatable("megaradar.hud.other_dimension", type.displayName());
            } else {
                BlockPos target = d.current();
                double dx = target.getX() + 0.5 - player.getX();
                double dz = target.getZ() + 0.5 - player.getZ();
                int dist = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
                // Yaw di Minecraft: 0 = sud (+Z), 90 = ovest (-X); cresce girando a destra.
                float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                float relative = Mth.wrapDegrees(targetYaw - player.getYRot());
                int idx = Math.floorMod(Math.round(relative / 45f), 8);
                line = Component.translatable("megaradar.hud.line", ARROWS[idx], type.displayName(), dist,
                        d.index() + 1, d.targets().length);
            }

            int w = font.width(line);
            graphics.fill(x - 3, y - 3, x + w + 3, y + font.lineHeight + 1, 0x90000000);
            graphics.drawString(font, line, x, y, type.hudColor(), true);
            y += font.lineHeight + 6;
        }
    }

    private static void collect(ItemStack stack, Map<RadarType, ItemStack> out) {
        if (stack.getItem() instanceof RadarItem radar && !out.containsKey(radar.radarType())
                && RadarTargets.read(stack).isPresent()) {
            out.put(radar.radarType(), stack);
        }
    }
}
