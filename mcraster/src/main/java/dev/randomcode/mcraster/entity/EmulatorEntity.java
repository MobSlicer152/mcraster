package dev.randomcode.mcraster.entity;

import dev.randomcode.mcraster.emulator.EmulatorScreen;
import dev.randomcode.mcraster.emulator.EmulatorThread;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class EmulatorEntity extends Entity {
    private EmulatorThread emulator = null;

    public EmulatorEntity(EntityType<?> type, World world) {
        super(type, world);

        // stop multiple emulators from existing
        if (world.getEntitiesByType(Entities.EMULATOR_ENTITY_TYPE, null, null).size() > 1) {
            getServer().sendMessage(Text.of("this mod is jank, only one emulator at a time is supported"));
            remove(RemovalReason.DISCARDED);
        }

        emulator = new EmulatorThread();
        emulator.run();
    }

    @Override
    public void tick() {
        for (int y = 0; y < EmulatorScreen.HEIGHT; y++) {
            for (int x = 0; x < EmulatorScreen.WIDTH; x++) {

            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {

    }

    @Override
    protected void writeCustomData(WriteView view) {

    }
}
