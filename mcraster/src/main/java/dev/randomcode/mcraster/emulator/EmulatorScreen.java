package dev.randomcode.mcraster.emulator;

import java.util.Arrays;

public class EmulatorScreen {
    public byte[] framebuffer = new byte[WIDTH * HEIGHT];
    // TODO: gamerules for these?
    public static final int WIDTH = 160;
    public static final int HEIGHT = 120;

    public void clear(byte color) {
        Arrays.fill(framebuffer, color);
    }
    public byte getPixel(int x, int y) {
        return framebuffer[y * HEIGHT + x];
    }
    public void setPixel(int x, int y, byte color) {
        framebuffer[y * HEIGHT + x] = color;
    }
}
