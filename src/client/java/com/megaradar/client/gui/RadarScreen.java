package com.megaradar.client.gui;

import com.megaradar.menu.RadarMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI del radar (dal wireframe Figma), nello stile dell'item:
 *  - lo schermo del radar è il decagono, con uno slot su ogni vertice;
 *  - un fascio di scansione ruota sullo schermo e fa "accendere" gli slot quando ci passa sopra;
 *  - slot vuoto = l'item richiesto al 50% di opacità; slot completo = bordo verde acceso;
 *  - bottone "Cerca" al centro: grigio finché non sono inseriti tutti gli elementi, poi verde e pulsante.
 */
public class RadarScreen extends AbstractContainerScreen<RadarMenu> {

    private static final int BUTTON_W = 50;
    private static final int BUTTON_H = 16;

    /** Velo al 50% del colore dello slot: sopra l'item lo fa apparire al 50% di opacità. */
    private static final int GHOST_VEIL = 0x80000000 | (RadarTheme.SCREEN_SLOT & 0xFFFFFF);

    // Fascio di scansione
    private static final int SWEEP_RADIUS = 46;
    private static final float SWEEP_TRAIL_DEG = 75f;
    private static final long PERIOD_IDLE_MS = 3200;
    private static final long PERIOD_READY_MS = 1600;
    private static final float SLOT_GLOW_DEG = 100f;

    /** Pixel del disco di scansione (dx, dy, angolo in gradi), calcolati una volta sola. */
    private static final int[] SWEEP_DX;
    private static final int[] SWEEP_DY;
    private static final float[] SWEEP_ANGLE;

    static {
        List<int[]> pts = new ArrayList<>();
        for (int dy = -SWEEP_RADIUS; dy <= SWEEP_RADIUS; dy++) {
            for (int dx = -SWEEP_RADIUS; dx <= SWEEP_RADIUS; dx++) {
                double r = Math.sqrt(dx * dx + dy * dy);
                if (r >= 2 && r <= SWEEP_RADIUS) pts.add(new int[]{dx, dy});
            }
        }
        SWEEP_DX = new int[pts.size()];
        SWEEP_DY = new int[pts.size()];
        SWEEP_ANGLE = new float[pts.size()];
        for (int i = 0; i < pts.size(); i++) {
            SWEEP_DX[i] = pts.get(i)[0];
            SWEEP_DY[i] = pts.get(i)[1];
            // y verso il basso: l'angolo cresce in senso orario, 0° = destra, -90° = alto
            SWEEP_ANGLE[i] = (float) Math.toDegrees(Math.atan2(SWEEP_DY[i], SWEEP_DX[i]));
        }
    }

    private final RadarTheme theme;
    private SearchButton searchButton;

    public RadarScreen(RadarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.theme = RadarTheme.of(menu.type());
        this.imageWidth = RadarMenu.IMAGE_WIDTH;
        this.imageHeight = RadarMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int bx = leftPos + RadarMenu.CENTER_X - BUTTON_W / 2;
        int by = topPos + RadarMenu.CENTER_Y - BUTTON_H / 2;
        searchButton = addRenderableWidget(new SearchButton(bx, by, BUTTON_W, BUTTON_H,
                Component.translatable("megaradar.gui.search"), this::onSearch));
        updateButton();
    }

    private void onSearch() {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) return;
        if (menu.clickMenuButton(minecraft.player, RadarMenu.BUTTON_SEARCH)) {
            // Pacchetto C2S vanilla: il server ricontrolla carburante e cooldown prima di scansionare
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, RadarMenu.BUTTON_SEARCH);
        }
    }

    private void updateButton() {
        if (searchButton != null && minecraft != null && minecraft.player != null) {
            searchButton.active = menu.canSearch(minecraft.player);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButton();
    }

    /** Angolo attuale del fascio (gradi, senso orario, 0° = destra). */
    private float sweepAngle() {
        boolean ready = searchButton != null && searchButton.active;
        long period = ready ? PERIOD_READY_MS : PERIOD_IDLE_MS;
        return (Util.getMillis() % period) / (float) period * 360f - 90f;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(theme.texture(), leftPos, topPos, 0, 0, imageWidth, imageHeight);

        boolean ready = searchButton != null && searchButton.active;
        float sweep = sweepAngle();
        renderSweep(graphics, sweep, ready);

        for (int i = 0; i < menu.fuelCount(); i++) {
            RadarMenu.FuelSlot slot = (RadarMenu.FuelSlot) menu.slots.get(i);
            int x = leftPos + slot.x;
            int y = topPos + slot.y;

            // Bordo: verde acceso se completo, altrimenti si illumina del colore del radar al passaggio del fascio
            int border;
            if (slot.isComplete()) {
                border = RadarTheme.PHOSPHOR_BRIGHT;
            } else {
                float slotAngle = -90f + 36f * i;
                float diff = Math.floorMod((int) (sweep - slotAngle), 360);
                border = RadarTheme.lerp(RadarTheme.SCREEN_SLOT_EDGE, theme.accentLight(), 1f - diff / SLOT_GLOW_DEG);
            }
            graphics.fill(x - 1, y - 1, x + 17, y + 17, border);
            graphics.fill(x, y, x + 16, y + 16, RadarTheme.SCREEN_SLOT);

            // Stato vuoto: l'item richiesto al 50% di opacità (con la quantità)
            if (!slot.hasItem()) {
                ItemStack ghost = new ItemStack(slot.required(), slot.needed());
                graphics.renderFakeItem(ghost, x, y);
                graphics.renderItemDecorations(font, ghost, x, y);
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 210); // sopra item e numero, sotto l'item tenuto dal mouse
                graphics.fill(x, y, x + 16, y + 16, GHOST_VEIL);
                graphics.pose().popPose();
            }
        }
    }

    /** Cono di scansione con dissolvenza, disegnato pixel per pixel sullo schermo a decagono. */
    private void renderSweep(GuiGraphics graphics, float sweep, boolean ready) {
        int cx = leftPos + RadarMenu.CENTER_X;
        int cy = topPos + RadarMenu.CENTER_Y;
        int baseAlpha = ready ? 0xA0 : 0x70;
        for (int i = 0; i < SWEEP_DX.length; i++) {
            float diff = sweep - SWEEP_ANGLE[i];
            diff = ((diff % 360f) + 360f) % 360f;
            if (diff > SWEEP_TRAIL_DEG) continue;
            int alpha;
            if (diff < 3f) {
                alpha = Math.min(255, baseAlpha + 0x40); // bordo d'attacco più luminoso
            } else {
                float t = 1f - diff / SWEEP_TRAIL_DEG;
                alpha = (int) (baseAlpha * t * t);
            }
            if (alpha < 6) continue;
            int x = cx + SWEEP_DX[i];
            int y = cy + SWEEP_DY[i];
            graphics.fill(x, y, x + 1, y + 1, (alpha << 24) | RadarTheme.PHOSPHOR_RGB);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, theme.accentLight(), true);

        // Contatore carburante in alto a destra
        Component progress = Component.literal(menu.filledCount() + "/" + menu.fuelCount());
        int progressColor = menu.isFuelReady() ? RadarTheme.PHOSPHOR_BRIGHT : RadarTheme.MUTED;
        graphics.drawString(font, progress, imageWidth - 8 - font.width(progress), titleLabelY, progressColor, true);

        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, RadarTheme.LABEL, false);

        Component status = statusText();
        if (status != null) {
            int color = menu.status() != RadarMenu.STATUS_NONE ? RadarTheme.ERROR : RadarTheme.MUTED;
            graphics.drawString(font, status, imageWidth - 8 - font.width(status), inventoryLabelY, color, false);
        }
    }

    private Component statusText() {
        String key = switch (menu.status()) {
            case RadarMenu.STATUS_NOT_FOUND -> "not_found";
            case RadarMenu.STATUS_NO_TARGETS -> "no_targets";
            case RadarMenu.STATUS_COOLDOWN -> "cooldown";
            case RadarMenu.STATUS_MISSING_FUEL -> "missing_fuel";
            default -> null;
        };
        if (key == null && minecraft != null && minecraft.player != null
                && minecraft.player.getCooldowns().isOnCooldown(menu.radarItem())) {
            key = "cooldown";
        }
        return key == null ? null : Component.translatable("megaradar.gui.status." + key);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        Slot hovered = this.hoveredSlot;
        if (hovered instanceof RadarMenu.FuelSlot fuelSlot && !fuelSlot.hasItem() && menu.getCarried().isEmpty()) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("megaradar.gui.needs",
                            fuelSlot.required().getDescription(), fuelSlot.needed()).withStyle(ChatFormatting.GOLD)),
                    mouseX, mouseY);
        } else if (searchButton != null && !searchButton.active && isOverButton(mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("megaradar.gui.locked"));
            lines.add(Component.translatable("megaradar.gui.progress", menu.filledCount(), menu.fuelCount())
                    .withStyle(ChatFormatting.GRAY));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private boolean isOverButton(int mouseX, int mouseY) {
        return mouseX >= searchButton.getX() && mouseX < searchButton.getX() + searchButton.getWidth()
                && mouseY >= searchButton.getY() && mouseY < searchButton.getY() + searchButton.getHeight();
    }
}
