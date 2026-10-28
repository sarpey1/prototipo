package com.megaradar.menu;

import com.megaradar.config.RadarConfig;
import com.megaradar.fuel.FuelManager;
import com.megaradar.item.RadarItem;
import com.megaradar.radar.RadarType;
import com.megaradar.registry.ModItems;
import com.megaradar.registry.ModMenus;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Menu del radar: 10 slot carburante disposti sui vertici di un decagono + inventario del giocatore.
 *
 * Rete:
 *  - apertura: il server manda {@link OpenData} (tipo radar, mano, requisiti di ogni slot) con il pacchetto
 *    di apertura di Fabric (ExtendedScreenHandlerType), così client e server costruiscono lo stesso menu;
 *  - bottone "Cerca": pacchetto vanilla C2S "container button click" (id {@link #BUTTON_SEARCH}),
 *    il server riverifica tutto in {@link #clickMenuButton} (autoritativo);
 *  - esito: S2C tramite DataSlot sincronizzato ({@link #status()}) + messaggi in chat.
 */
public class RadarMenu extends AbstractContainerMenu {

    public static final int BUTTON_SEARCH = 0;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_NOT_FOUND = 1;
    public static final int STATUS_NO_TARGETS = 2;
    public static final int STATUS_COOLDOWN = 3;
    public static final int STATUS_MISSING_FUEL = 4;

    // Geometria (pixel della GUI): decagono con un vertice in alto
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 232;
    public static final int CENTER_X = 88;
    public static final int CENTER_Y = 74;
    public static final int RADIUS = 48;
    public static final int INVENTORY_Y = IMAGE_HEIGHT - 82;

    /** Dati di apertura, inviati dal server al client. */
    public record OpenData(int radarType, int hand, List<FuelManager.FuelReq> fuel) {
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenData> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, OpenData::radarType,
                ByteBufCodecs.VAR_INT, OpenData::hand,
                FuelManager.FuelReq.LIST_CODEC, OpenData::fuel,
                OpenData::new);
    }

    /** Factory lato server usata da RadarItem#use per aprire la GUI. */
    public record Provider(RadarType type, InteractionHand hand, Component title)
            implements ExtendedScreenHandlerFactory<OpenData> {

        @Override
        public OpenData getScreenOpeningData(ServerPlayer player) {
            return new OpenData(type.ordinal(), hand.ordinal(), FuelManager.guiFuel());
        }

        @Override
        public Component getDisplayName() {
            return title;
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new RadarMenu(containerId, inventory, getScreenOpeningData((ServerPlayer) player));
        }
    }

    private final RadarType type;
    private final InteractionHand hand;
    private final int lockedHotbarSlot;
    private final SimpleContainer fuel;
    private final int fuelCount;
    private final DataSlot status = DataSlot.standalone();

    public RadarMenu(int containerId, Inventory inventory, OpenData data) {
        super(ModMenus.RADAR, containerId);
        this.type = RadarType.byOrdinal(data.radarType());
        this.hand = data.hand() == InteractionHand.OFF_HAND.ordinal() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        this.lockedHotbarSlot = hand == InteractionHand.MAIN_HAND ? inventory.selected : -1;

        List<FuelManager.FuelReq> reqs = data.fuel();
        this.fuelCount = Math.min(reqs.size(), FuelManager.MAX_SLOTS);
        this.fuel = new SimpleContainer(Math.max(1, fuelCount));

        // Slot 0..n-1: carburante sui vertici del decagono (in senso orario partendo dall'alto)
        for (int i = 0; i < fuelCount; i++) {
            FuelManager.FuelReq req = reqs.get(i);
            Item item = BuiltInRegistries.ITEM.get(req.id());
            int[] pos = slotPos(i);
            addSlot(new FuelSlot(fuel, i, pos[0], pos[1], item, req.count()));
        }

        // Inventario del giocatore
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, INVENTORY_Y + row * 18));
            }
        }
        // Hotbar: lo slot che contiene il radar è bloccato (niente duplicazioni o spostamenti)
        for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            int y = INVENTORY_Y + 58;
            addSlot(col == lockedHotbarSlot ? new LockedSlot(inventory, col, x, y) : new Slot(inventory, col, x, y));
        }

        addDataSlot(status);

        // Se il giocatore cambia il carburante, il messaggio di esito precedente sparisce
        fuel.addListener(c -> {
            if (!inventory.player.level().isClientSide()) status.set(STATUS_NONE);
        });
    }

    /** Posizione (angolo in alto a sinistra dell'item) dello slot i sul decagono. */
    public static int[] slotPos(int i) {
        double angle = Math.toRadians(-90 + 36.0 * i);
        int x = (int) Math.round(CENTER_X + RADIUS * Math.cos(angle)) - 8;
        int y = (int) Math.round(CENTER_Y + RADIUS * Math.sin(angle)) - 8;
        return new int[]{x, y};
    }

    // ---------------------------------------------------------------- stato

    public RadarType type() { return type; }
    public int fuelCount() { return fuelCount; }
    public int status() { return status.get(); }

    public RadarItem radarItem() {
        return ModItems.forType(type);
    }

    public int filledCount() {
        int n = 0;
        for (int i = 0; i < fuelCount; i++) {
            if (((FuelSlot) slots.get(i)).isComplete()) n++;
        }
        return n;
    }

    public boolean isFuelReady() {
        return filledCount() == fuelCount;
    }

    /** Usato dal client per colorare il bottone (grigio / verde). */
    public boolean canSearch(Player player) {
        if (player.getCooldowns().isOnCooldown(radarItem())) return false;
        return player.getAbilities().instabuild || isFuelReady();
    }

    // ---------------------------------------------------------------- bottone "Cerca" (lato server)

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_SEARCH) return false;
        if (player.level().isClientSide()) return true;
        if (!(player instanceof ServerPlayer serverPlayer) || !(player.level() instanceof ServerLevel level)) return false;

        ItemStack radarStack = serverPlayer.getItemInHand(hand);
        if (!(radarStack.getItem() instanceof RadarItem radar) || radar.radarType() != type) return false;

        boolean creative = serverPlayer.getAbilities().instabuild;
        if (serverPlayer.getCooldowns().isOnCooldown(radar)) {
            status.set(STATUS_COOLDOWN);
            return true;
        }
        if (!creative && !isFuelReady()) {
            status.set(STATUS_MISSING_FUEL);
            return true;
        }

        switch (radar.performScan(serverPlayer, level, radarStack)) {
            case FOUND -> {
                if (!creative) consumeFuel();
                status.set(STATUS_NONE);
                serverPlayer.closeContainer(); // chiude la GUI: ora c'è la freccia sull'HUD
            }
            case NOT_FOUND -> {
                if (!creative && !RadarConfig.get().consumeFuelOnlyIfFound) consumeFuel();
                status.set(STATUS_NOT_FOUND);
            }
            case NO_TARGETS -> status.set(STATUS_NO_TARGETS);
        }
        return true;
    }

    private void consumeFuel() {
        for (int i = 0; i < fuelCount; i++) {
            fuel.removeItem(i, ((FuelSlot) slots.get(i)).needed());
        }
    }

    // ---------------------------------------------------------------- inventario

    @Override
    public boolean stillValid(Player player) {
        ItemStack held = player.getItemInHand(hand);
        return held.getItem() instanceof RadarItem radar && radar.radarType() == type;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Impedisce di spostare il radar con i tasti numerici (1-9) o con F (mano secondaria)
        if (clickType == ClickType.SWAP) {
            if (hand == InteractionHand.MAIN_HAND && button == lockedHotbarSlot) return;
            if (hand == InteractionHand.OFF_HAND && button == 40) return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int invStart = fuelCount;
        int invEnd = invStart + 27;
        int hotbarEnd = invEnd + 9;

        if (index < fuelCount) {
            // dal decagono all'inventario
            if (!moveItemStackTo(stack, invStart, hotbarEnd, true)) return ItemStack.EMPTY;
        } else {
            // dall'inventario: prima nello slot carburante giusto, altrimenti tra inventario e hotbar
            boolean moved = false;
            for (int i = 0; i < fuelCount && !moved; i++) {
                if (slots.get(i).mayPlace(stack)) moved = moveItemStackTo(stack, i, i + 1, false);
            }
            if (!moved) {
                if (index < invEnd) {
                    if (!moveItemStackTo(stack, invEnd, hotbarEnd, false)) return ItemStack.EMPTY;
                } else if (!moveItemStackTo(stack, invStart, invEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Come il banco da lavoro: alla chiusura il carburante non usato torna al giocatore
        if (!player.level().isClientSide()) clearContainer(player, fuel);
    }

    // ---------------------------------------------------------------- slot

    /** Accetta solo il suo minerale, fino alla quantità richiesta. */
    public static class FuelSlot extends Slot {
        private final Item required;
        private final int needed;

        public FuelSlot(Container container, int index, int x, int y, Item required, int needed) {
            super(container, index, x, y);
            this.required = required;
            this.needed = needed;
        }

        public Item required() { return required; }
        public int needed() { return needed; }

        public boolean isComplete() {
            return getItem().is(required) && getItem().getCount() >= needed;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(required);
        }

        @Override
        public int getMaxStackSize() {
            return needed;
        }
    }

    /** Lo slot della hotbar che contiene il radar aperto. */
    public static class LockedSlot extends Slot {
        public LockedSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
