package com.megaradar.command;

import com.megaradar.config.RadarConfig;
import com.megaradar.data.VisitedData;
import com.megaradar.fuel.FuelManager;
import com.megaradar.network.ModNetworking;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.megaradar.radar.StructureScanner;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;

/**
 * /megaradar reload            (OP 2) ricarica la config e risincronizza i client
 * /megaradar reset             azzera le strutture "già visitate" di chi lo esegue
 * /megaradar reset <giocatore> (OP 2) azzera quelle di un altro giocatore
 */
public final class RadarCommand {

    private RadarCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("megaradar")
                .then(Commands.literal("reload")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            RadarConfig.load();
                            var fuel = FuelManager.serverFuel();
                            for (ServerPlayer p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                ModNetworking.sendFuelSync(p, fuel);
                            }
                            ctx.getSource().sendSuccess(() -> Component.translatable("megaradar.cmd.reloaded"), true);
                            return 1;
                        }))
                .then(Commands.literal("debugscan")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("structure", ResourceLocationArgument.id())
                                .executes(ctx -> {
                                    try {
                                        return debugScan(ctx.getSource(), ResourceLocationArgument.getId(ctx, "structure"));
                                    } catch (Exception e) {
                                        com.megaradar.MegaRadar.LOGGER.error("debugscan fallito", e);
                                        throw e;
                                    }
                                })))
                .then(Commands.literal("megas")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> com.megaradar.compat.UnlimitedMegas.statusMessage(ctx.getSource().getServer()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("File motore: "
                                    + com.megaradar.compat.ShowdownMegaPatch.ENGINE_FILE.toAbsolutePath()
                                    + " | stato: " + com.megaradar.compat.ShowdownMegaPatch.status()), false);
                            return 1;
                        }))
                .then(Commands.literal("reset")
                        .executes(ctx -> reset(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))));
    }

    /** Prova la scansione su una struttura qualsiasi (es. minecraft:village_plains), senza carburante. */
    private static int debugScan(CommandSourceStack source, ResourceLocation id) {
        ServerLevel level = source.getLevel() != null ? source.getLevel() : source.getServer().overworld();
        var holder = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getHolder(ResourceKey.create(Registries.STRUCTURE, id));
        if (holder.isEmpty()) {
            source.sendFailure(Component.literal("[megaradar] Struttura non registrata: " + id));
            return 0;
        }
        BlockPos origin = BlockPos.containing(source.getPosition());
        List<Holder<Structure>> targets = new ArrayList<>();
        targets.add(holder.get());
        long t0 = System.currentTimeMillis();
        List<BlockPos> found = StructureScanner.findNearest(level, targets, origin,
                RadarConfig.get().searchRadiusChunks, 3, pos -> false);
        long ms = System.currentTimeMillis() - t0;
        source.sendSuccess(() -> Component.literal("[megaradar] debugscan " + id + ": "
                + found.size() + " risultati in " + ms + " ms"), false);
        for (BlockPos p : found) {
            int d = (int) Math.sqrt(StructureScanner.horizontalDistSqr(p, origin));
            source.sendSuccess(() -> Component.literal("[megaradar]   X " + p.getX() + " Z " + p.getZ() + " (" + d + " blocchi)"), false);
        }
        return found.size();
    }

    private static int reset(CommandSourceStack source, ServerPlayer target) {
        VisitedData data = VisitedData.get(source.getServer());
        int n = data.count(target.getUUID());
        data.reset(target.getUUID());
        source.sendSuccess(() -> Component.translatable("megaradar.cmd.reset", n, target.getDisplayName()), true);
        return n;
    }
}
