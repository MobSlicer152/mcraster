package dev.randomcode.mcraster.entity;

import dev.randomcode.mcraster.MCRaster;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class EntityType {
    public static final net.minecraft.entity.EntityType<EmulatorEntity> EMULATOR = register(keyOf("emulator"),
            net.minecraft.entity.EntityType.Builder.create(EmulatorEntity::new, SpawnGroup.MISC).dropsNothing().dimensions(0, 0).maxTrackingRange(0));

    public static void ensureInit() {
    }

    private static RegistryKey<net.minecraft.entity.EntityType<?>> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, MCRaster.identifier(id));
    }

    private static <T extends Entity> net.minecraft.entity.EntityType<T> register(RegistryKey<net.minecraft.entity.EntityType<?>> key, net.minecraft.entity.EntityType.Builder<T> type) {
        MCRaster.LOGGER.info("Registering entity {}", key.toString());
        return Registry.register(Registries.ENTITY_TYPE, key, type.build(key));
    }
}
