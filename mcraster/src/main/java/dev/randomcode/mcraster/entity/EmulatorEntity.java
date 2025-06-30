package dev.randomcode.mcraster.entity;

import dev.randomcode.mcraster.MCRaster;
import dev.randomcode.mcraster.emulator.EmulatorScreen;
import dev.randomcode.mcraster.emulator.EmulatorThread;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EmulatorEntity extends MarkerEntity {
    private static EmulatorThread emulator = null;

    public EmulatorEntity(net.minecraft.entity.EntityType<?> type, World world) {
        super(type, world);

        if (!world.isClient()) {
            var serverWorld = (ServerWorld)world;

            // stop multiple emulators from existing
            var emulators = serverWorld.getEntitiesByType(EntityType.EMULATOR, null);
            emulators.forEach(emulator -> {
                if (emulator != this) {
                    emulator.remove(RemovalReason.DISCARDED);
                }
            });

            if (emulator == null) {
                emulator = new EmulatorThread();
                emulator.start();
            }
        }
    }

    @Override
    public void tick() {
        if (!getWorld().isClient()) {
            var serverWorld = (ServerWorld)getWorld();
            BlockPos topLeft = getBlockPos().up(EmulatorScreen.HEIGHT);
            for (int y = 0; y < EmulatorScreen.HEIGHT; y++) {
                for (int x = 0; x < EmulatorScreen.WIDTH; x++) {
                    BlockPos pos = topLeft.add(x, -y, 0);
                    BlockState block = MCRaster.PALETTE.get(emulator.getScreen().getPixel(x, y)).getDefaultState();
                    serverWorld.setBlockState(pos, block);
                }
            }
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        if (emulator == null) {
            emulator = new EmulatorThread();
            emulator.start();
        }
    }
}
