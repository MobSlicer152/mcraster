package dev.randomcode.mcraster.emulator;

public class EmulatorScreen {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 320;
    public byte[] framebuffer = new byte[WIDTH * HEIGHT];

    public void setPixel(int x, int y, byte color) {
        framebuffer[y * HEIGHT + x] = color;
    }
}
