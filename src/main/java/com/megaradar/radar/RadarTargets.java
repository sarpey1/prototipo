package com.megaradar.radar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Optional;

/**
 * I bersagli trovati sono salvati DENTRO l'item (component custom_data).
 * Il server li scrive, Minecraft sincronizza l'item al client da solo,
 * e l'HUD li legge: niente pacchetti extra e la freccia sopravvive a relog e riavvii.
 */
public final class RadarTargets {

    private static final String ROOT = "megaradar";

    private RadarTargets() {}

    public record Data(ResourceLocation dimension, long[] targets, int index) {
        public boolean hasCurrent() {
            return index >= 0 && index < targets.length;
        }

        public BlockPos current() {
            return BlockPos.of(targets[index]);
        }

        public int remaining() {
            return Math.max(0, targets.length - index);
        }
    }

    public static Optional<Data> read(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return Optional.empty();
        CompoundTag tag = custom.copyTag();
        if (!tag.contains(ROOT)) return Optional.empty();
        CompoundTag t = tag.getCompound(ROOT);
        ResourceLocation dim = ResourceLocation.tryParse(t.getString("dim"));
        if (dim == null) return Optional.empty();
        return Optional.of(new Data(dim, t.getLongArray("targets"), t.getInt("idx")));
    }

    public static void write(ItemStack stack, ResourceLocation dimension, List<BlockPos> targets, int index) {
        CompoundTag t = new CompoundTag();
        t.putString("dim", dimension.toString());
        t.putLongArray("targets", targets.stream().mapToLong(BlockPos::asLong).toArray());
        t.putInt("idx", index);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(ROOT, t));
    }

    public static void setIndex(ItemStack stack, int index) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (tag.contains(ROOT)) tag.getCompound(ROOT).putInt("idx", index);
        });
    }
}
