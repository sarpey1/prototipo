package com.megaradar.radar;

import com.megaradar.MegaRadar;
import com.megaradar.config.RadarConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/** I due tipi di radar. L'ordinale viene usato nel payload di rete. */
public enum RadarType {
    MEGAROID("megaroid", 0xFFC86BFF),
    MEGA_CITY("mega_city", 0xFF5FE6FF);

    private final String id;
    private final int hudColor;
    private final TagKey<Structure> targetTag;

    RadarType(String id, int hudColor) {
        this.id = id;
        this.hudColor = hudColor;
        // data/megaradar/tags/worldgen/structure/<id>_targets.json
        this.targetTag = TagKey.create(Registries.STRUCTURE, MegaRadar.id(id + "_targets"));
    }

    public String id() { return id; }
    public int hudColor() { return hudColor; }
    public TagKey<Structure> targetTag() { return targetTag; }

    public Component displayName() {
        return Component.translatable("megaradar.target." + id);
    }

    public Component hint() {
        return Component.translatable("megaradar.hint." + id);
    }

    /** ID di strutture extra aggiunti dalla config (oltre al tag). */
    public List<String> extraStructuresFromConfig() {
        RadarConfig c = RadarConfig.get();
        return this == MEGAROID ? c.megaroidExtraStructures : c.megaCityExtraStructures;
    }

    public static RadarType byOrdinal(int ordinal) {
        RadarType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MEGAROID;
    }
}
