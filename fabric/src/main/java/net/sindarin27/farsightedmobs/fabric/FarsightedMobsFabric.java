package net.sindarin27.farsightedmobs.fabric;

import net.fabricmc.api.ModInitializer;

import net.sindarin27.farsightedmobs.FarsightedMobs;

public final class FarsightedMobsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        FarsightedMobs.init();
    }
}
