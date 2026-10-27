package com.megaradar.registry;

import com.megaradar.MegaRadar;
import com.megaradar.item.RadarItem;
import com.megaradar.radar.RadarType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class ModItems {

    private ModItems() {}

    public static final RadarItem MEGAROID_RADAR = register("megaroid_radar",
            new RadarItem(RadarType.MEGAROID, radarProps()));

    public static final RadarItem MEGA_CITY_RADAR = register("mega_city_radar",
            new RadarItem(RadarType.MEGA_CITY, radarProps()));

    /** Tab creativo dedicato. JEI/EMI/REI mostrano comunque tutti gli item registrati e le loro ricette. */
    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            MegaRadar.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(MEGAROID_RADAR))
                    .title(Component.translatable("itemGroup.megaradar.main"))
                    .displayItems((params, output) -> {
                        output.accept(MEGAROID_RADAR);
                        output.accept(MEGA_CITY_RADAR);
                    })
                    .build());

    private static Item.Properties radarProps() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant();
    }

    private static <T extends Item> T register(String path, T item) {
        return Registry.register(BuiltInRegistries.ITEM, MegaRadar.id(path), item);
    }

    /** Chiamato dall'entrypoint: forza il caricamento della classe e aggiunge gli item anche a "Strumenti". */
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(MEGAROID_RADAR);
            entries.accept(MEGA_CITY_RADAR);
        });
    }
}
