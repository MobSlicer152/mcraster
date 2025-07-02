package dev.randomcode.mcraster.emulator;

import dev.randomcode.mcraster.MCRaster;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;

public class EmulatorScreen {
    public byte[] framebuffer = new byte[WIDTH * HEIGHT];
	private int palette = 0;

    // TODO: gamerules for these?
    public static final int WIDTH = 320;
    public static final int HEIGHT = 200;

    public void clear(byte color) {
        Arrays.fill(framebuffer, color);
    }
    public byte getPixel(int x, int y) {
        return framebuffer[y * WIDTH + x];
    }
    public void setPixel(int x, int y, byte color) {
        framebuffer[y * WIDTH + x] = color;
    }

    public void render(ServerWorld world, BlockPos emulatorPos) {
        var topLeft = emulatorPos.up(HEIGHT);
        var first = new Vec3i(topLeft.getX() + WIDTH, topLeft.getY() + HEIGHT, topLeft.getZ());
        var second = new Vec3i(topLeft.getX(), topLeft.getY(), topLeft.getZ() - 1);
        var box = BlockBox.create(first, second);

		var palette = MCRaster.palettes.get(this.palette);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                var pos = topLeft.add(x, -y, 0);
                var block = palette.get(getPixel(x, y)).getDefaultState();
                world.setBlockState(pos, block);
                var back = pos.add(0, 0, -1);
                world.setBlockState(back, Blocks.BLACK_CONCRETE.getDefaultState());
            }
        }

        // prevent water and coral and stuff from updating hopefully
        world.clearUpdatesInArea(box);
    }

	public void setPalette(int palette) {
		if (palette >= 0 && palette < MCRaster.palettes.size()) {
			this.palette = palette;
		} else {
            MCRaster.LOGGER.warn("palette {} out of range, only {} defined", palette, MCRaster.palettes.size());
            // default to the last one, which should be VGA
            //this.palette = MCRaster.palettes.size() - 1;
        }
	}
}
