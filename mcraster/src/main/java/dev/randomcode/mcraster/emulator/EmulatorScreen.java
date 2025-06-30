package dev.randomcode.mcraster.emulator;

public class EmulatorScreen {
    public byte[] framebuffer = new byte[WIDTH * HEIGHT];
    // TODO: gamerules for these?
    public static final int WIDTH = 80;
    public static final int HEIGHT = 60;

    public byte getPixel(int x, int y) {
        return framebuffer[y * HEIGHT + x];
    }
    public void setPixel(int x, int y, byte color) {
        framebuffer[y * HEIGHT + x] = color;
    }
}
