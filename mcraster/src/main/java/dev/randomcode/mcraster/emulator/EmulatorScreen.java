package dev.randomcode.mcraster.emulator;

import dev.randomcode.mcraster.MCRaster;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

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
        return framebuffer[y * HEIGHT + x];
    }
    public void setPixel(int x, int y, byte color) {
        framebuffer[y * HEIGHT + x] = color;
    }

    public void render(ServerWorld world, BlockPos emulatorPos) {
        var topLeft = emulatorPos.up(EmulatorScreen.HEIGHT);
		var palette = MCRaster.palettes.get(this.palette);
        for (int y = 0; y < EmulatorScreen.HEIGHT; y++) {
            for (int x = 0; x < EmulatorScreen.WIDTH; x++) {
                BlockPos pos = topLeft.add(x, -y, 0);
                BlockState block = palette.get(getPixel(x, y)).getDefaultState();
                world.setBlockState(pos, block);
            }
        }
    }

	public void setPalette(int palette) {
		if (palette > 0 && palette < MCRaster.palettes.size()) {
			this.palette = palette;
		}
	}
}
