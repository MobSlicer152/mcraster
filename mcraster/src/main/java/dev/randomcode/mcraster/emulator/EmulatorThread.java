package dev.randomcode.mcraster.emulator;

import com.dylibso.chicory.runtime.Instance;

import com.dylibso.chicory.wasm.Parser;
import dev.randomcode.mcraster.MCRaster;

public class EmulatorThread extends Thread {
    private EmulatorScreen screen;

    public EmulatorThread() {
        super();

        setName("Emulator");
    }

    @Override
    public void run() {
        MCRaster.LOGGER.debug("Emulator started");

        screen = new EmulatorScreen();
        screen.setPixel(0, 0, (byte)12);

        

        while (true) {
        }
    }

    public EmulatorScreen getScreen() {
        return screen;
    }
}
