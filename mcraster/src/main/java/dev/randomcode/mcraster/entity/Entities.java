package dev.randomcode.mcraster.entity;

import dev.randomcode.mcraster.MCRaster;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class Entities {
    public static final EntityType<Entity> EMULATOR_ENTITY_TYPE = registerEntity("emulator");

    private static EntityType<Entity> registerEntity(String name) {
        Identifier id = Identifier.of(MCRaster.MOD_ID, name);
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
        return Registry.register(Registries.ENTITY_TYPE, id, EntityType.Builder.create(SpawnGroup.MISC).build(key));
    }
}
