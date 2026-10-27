package com.megaradar.item;

import com.megaradar.config.RadarConfig;
import com.megaradar.data.VisitedData;
import com.megaradar.fuel.FuelManager;
import com.megaradar.radar.RadarTargets;
import com.megaradar.radar.RadarType;
import com.megaradar.radar.StructureScanner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Optional;

/**
 * Radar. Tutta la logica gira SUL SERVER.
 *
 *  - Clic destro: consuma il carburante e memorizza nel radar le N strutture più vicine
 *    che il giocatore NON ha ancora raggiunto.
 *  - Mentre lo tieni nella hotbar, l'HUD mostra la freccia verso il bersaglio attuale.
 *  - Quando arrivi vicino al bersaglio, viene segnato come visitato e il radar passa
 *    automaticamente al successivo (senza consumare carburante).
 *  - Sneak + clic destro: salta il bersaglio attuale (gratis).
 */
public class RadarItem extends Item {

    private final RadarType type;

    public RadarItem(RadarType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public RadarType radarType() {
        return type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isShiftKeyDown()) {
            skipCurrent(serverPlayer, serverLevel, stack);
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.consume(stack);
        }
        scan(serverPlayer, serverLevel, stack);
        return InteractionResultHolder.consume(stack);
    }

    private void scan(ServerPlayer player, ServerLevel level, ItemStack stack) {
        RadarConfig cfg = RadarConfig.get();
        boolean creative = player.getAbilities().instabuild;
        BlockPos origin = player.blockPosition();
        ResourceLocation dimension = level.dimension().location();

        List<Holder<Structure>> targets = StructureScanner.targets(level, type);
        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable("megaradar.msg.no_targets")
                    .withStyle(ChatFormatting.RED), false);
            return;
        }

        // Controllo del carburante PRIMA della scansione (che è costosa)
        if (!creative) {
            List<FuelManager.FuelEntry> missing = FuelManager.missing(player);
            if (!missing.isEmpty()) {
                player.displayClientMessage(Component.translatable("megaradar.msg.missing_fuel")
                        .withStyle(ChatFormatting.RED), false);
                for (FuelManager.FuelEntry m : missing) {
                    player.displayClientMessage(Component.translatable("megaradar.msg.fuel_line",
                            m.item().getDescription(), m.count()).withStyle(ChatFormatting.GRAY), false);
                }
                level.playSound(null, origin, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 0.6f);
                return;
            }
        }

        player.displayClientMessage(Component.translatable("megaradar.msg.scanning", type.displayName())
                .withStyle(ChatFormatting.DARK_AQUA), true);
        level.playSound(null, origin, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.7f);

        VisitedData visited = VisitedData.get(level.getServer());
        List<BlockPos> found = StructureScanner.findNearest(level, targets, origin,
                cfg.searchRadiusChunks, cfg.resultsPerScan,
                pos -> visited.isVisited(player.getUUID(), type, dimension, pos));
        player.getCooldowns().addCooldown(this, cfg.cooldownTicks);

        if (found.isEmpty()) {
            if (!creative && !cfg.consumeFuelOnlyIfFound) FuelManager.consume(player);
            player.displayClientMessage(Component.translatable("megaradar.msg.not_found",
                    type.displayName(), cfg.searchRadiusChunks).withStyle(ChatFormatting.YELLOW), false);
            level.playSound(null, origin, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6f, 1.2f);
            return;
        }

        if (!creative) FuelManager.consume(player);
        RadarTargets.write(stack, dimension, found, 0);

        player.displayClientMessage(Component.translatable("megaradar.msg.found_header",
                found.size(), type.displayName()).withStyle(ChatFormatting.GREEN), false);
        for (int i = 0; i < found.size(); i++) {
            BlockPos p = found.get(i);
            int dist = (int) Math.round(Math.sqrt(StructureScanner.horizontalDistSqr(p, origin)));
            player.displayClientMessage(Component.translatable("megaradar.msg.found_line",
                    i + 1, p.getX(), p.getZ(), dist).withStyle(i == 0 ? ChatFormatting.AQUA : ChatFormatting.GRAY), false);
        }
        player.displayClientMessage(type.hint().copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        level.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private void skipCurrent(ServerPlayer player, ServerLevel level, ItemStack stack) {
        Optional<RadarTargets.Data> data = RadarTargets.read(stack);
        if (data.isEmpty() || !data.get().hasCurrent()) {
            player.displayClientMessage(Component.translatable("megaradar.msg.no_current")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        RadarTargets.Data d = data.get();
        VisitedData.get(level.getServer()).markVisited(player.getUUID(), type, d.dimension(), d.current());
        advance(player, stack, d, "megaradar.msg.skipped");
    }

    private void advance(ServerPlayer player, ItemStack stack, RadarTargets.Data d, String messageKey) {
        int next = d.index() + 1;
        RadarTargets.setIndex(stack, next);
        player.displayClientMessage(Component.translatable(messageKey, type.displayName())
                .withStyle(ChatFormatting.GREEN), false);
        if (next < d.targets().length) {
            BlockPos p = BlockPos.of(d.targets()[next]);
            player.displayClientMessage(Component.translatable("megaradar.msg.next_target",
                    p.getX(), p.getZ(), d.targets().length - next).withStyle(ChatFormatting.AQUA), false);
        } else {
            player.displayClientMessage(Component.translatable("megaradar.msg.list_done")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
    }

    /** Ogni secondo, lato server: se il giocatore è arrivato al bersaglio, lo segna e passa al prossimo. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || level.getGameTime() % 20 != 0) return;
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;

        Optional<RadarTargets.Data> data = RadarTargets.read(stack);
        if (data.isEmpty() || !data.get().hasCurrent()) return;
        RadarTargets.Data d = data.get();
        if (!d.dimension().equals(serverLevel.dimension().location())) return;

        int r = RadarConfig.get().arrivalRadius;
        if (StructureScanner.horizontalDistSqr(d.current(), player.blockPosition()) <= (double) r * r) {
            VisitedData.get(serverLevel.getServer()).markVisited(player.getUUID(), type, d.dimension(), d.current());
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.4f);
            player.displayClientMessage(type.hint().copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            advance(player, stack, d, "megaradar.msg.arrived");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RadarTargets.read(stack).filter(RadarTargets.Data::hasCurrent).ifPresent(d -> {
            BlockPos p = d.current();
            tooltip.add(Component.translatable("megaradar.tooltip.current", p.getX(), p.getZ(), d.remaining())
                    .withStyle(ChatFormatting.AQUA));
        });
        tooltip.add(Component.translatable("megaradar.tooltip.use", type.displayName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("megaradar.tooltip.skip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("megaradar.tooltip.fuel").withStyle(ChatFormatting.GOLD));
        for (FuelManager.FuelEntry e : FuelManager.displayFuel()) {
            tooltip.add(Component.translatable("megaradar.tooltip.fuel_line",
                    e.item().getDescription(), e.count()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
