package com.megaradar.registry;

import com.megaradar.MegaRadar;
import com.megaradar.menu.RadarMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModMenus {

    private ModMenus() {}

    public static final ExtendedScreenHandlerType<RadarMenu, RadarMenu.OpenData> RADAR = Registry.register(
            BuiltInRegistries.MENU, MegaRadar.id("radar"),
            new ExtendedScreenHandlerType<>(RadarMenu::new, RadarMenu.OpenData.CODEC));

    /** Chiamato dall'entrypoint per forzare la registrazione. */
    public static void register() {}
}
