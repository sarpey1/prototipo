package com.megaradar.client.gui;

import com.megaradar.MegaRadar;
import com.megaradar.menu.RadarMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI del radar (dal wireframe Figma):
 *  - decagono con uno slot su ogni vertice;
 *  - slot vuoto = l'item richiesto appare al 50% di opacità (stato vuoto);
 *  - bottone "Cerca" al centro: grigio finché non sono inseriti tutti gli elementi, poi verde.
 */
public class RadarScreen extends AbstractContainerScreen<RadarMenu> {

    private static final ResourceLocation TEXTURE = MegaRadar.id("textures/gui/radar.png");

    private static final int BUTTON_W = 50;
    private static final int BUTTON_H = 16;

    // Colori slot vanilla
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    /** Velo color-slot al 50%: sopra l'item lo fa apparire al 50% di opacità. */
    private static final int GHOST_VEIL = 0x808B8B8B;

    private SearchButton searchButton;

    public RadarScreen(RadarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        for (int i = 0; i < menu.fuelCount(); i++) {
            RadarMenu.FuelSlot slot = (RadarMenu.FuelSlot) menu.slots.get(i);
            int x = leftPos + slot.x;
            int y = topPos + slot.y;

            // Cornice dello slot sul vertice del decagono
            graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_DARK);
            graphics.fill(x, y, x + 17, y + 17, SLOT_LIGHT);
            graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);

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

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        Component status = statusText();
        if (status != null) {
            int color = menu.status() != RadarMenu.STATUS_NONE ? 0xFFB02E26 : 0xFF555555;
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
            // Tooltip dello slot vuoto: cosa inserire
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("megaradar.gui.needs",
                            fuelSlot.required().getDescription(), fuelSlot.needed()).withStyle(ChatFormatting.GOLD)),
                    mouseX, mouseY);
        } else if (searchButton != null && !searchButton.active && isOverButton(mouseX, mouseY)) {
            // Tooltip del bottone grigio: quanto manca
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
