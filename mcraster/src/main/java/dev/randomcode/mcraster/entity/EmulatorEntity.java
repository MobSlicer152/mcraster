package dev.randomcode.mcraster.entity;

import dev.randomcode.mcraster.MCRaster;
import dev.randomcode.mcraster.emulator.EmulatorScreen;
import dev.randomcode.mcraster.emulator.Emulator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class EmulatorEntity extends MarkerEntity {
    private static Emulator emulator = null;

    public EmulatorEntity(net.minecraft.entity.EntityType<?> type, World world) {
        super(type, world);

        if (!world.isClient()) {
            var serverWorld = (ServerWorld)world;

            // stop multiple emulators from existing
            var emulators = serverWorld.getEntitiesByType(EntityType.EMULATOR, new Predicate<EmulatorEntity>() {
                @Override
                public boolean test(EmulatorEntity emulatorEntity) {
                    return true;
                }
            });

            emulators.forEach(emulator -> {
                if (emulator != this) {
                    emulator.kill(serverWorld);
                }
            });

			checkEmulator();
        }
    }

    @Override
    public void tick() {
        if (!getWorld().isClient()) {
			// make sure an emulator thread is running
			checkEmulator();
            emulator.getScreen().render((ServerWorld)getWorld(), getBlockPos());
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        checkEmulator();
    }

	private void checkEmulator() {
		if (emulator == null || !emulator.isAlive() && MCRaster.module != null) {
			emulator = new Emulator(this);
		    emulator.start();
		}
	}
}
