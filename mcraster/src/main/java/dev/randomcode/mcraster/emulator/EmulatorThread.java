package dev.randomcode.mcraster.emulator;

import com.dylibso.chicory.runtime.*;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.types.DataSegment;
import com.dylibso.chicory.wasm.types.FunctionType;
import com.dylibso.chicory.wasm.types.ValType;
import dev.randomcode.mcraster.MCRaster;
import dev.randomcode.mcraster.entity.EmulatorEntity;

import java.util.Arrays;
import java.util.List;

public class EmulatorThread extends Thread {
    private EmulatorEntity parent;
    private EmulatorScreen screen;

    public EmulatorThread(EmulatorEntity parent) {
        super();

        this.parent = parent;
        setName("Emulator");
    }

    @Override
    public void run() {
        MCRaster.LOGGER.debug("Emulator started");

        screen = new EmulatorScreen();

        var store = initHostFunctions();
        var instance = store.instantiate("program", MCRaster.MODULE);
        var main = instance.export("main");

        while (parent.isAlive()) {
            screen.clear((byte) 0);

            main.apply();

            MCRaster.LOGGER.info("Restarting emulator in 500ms");
            try {
                sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }

    private Store initHostFunctions() {
        var store = new Store();
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "running",
                        FunctionType.of(List.of(), List.of(ValType.I32)),
                        (Instance instance, long... args) -> {
                            return new long[]{parent.isAlive() ? 1 : 0};
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "getWidth",
                        FunctionType.of(List.of(), List.of(ValType.I32)),
                        (Instance instance, long... args) -> {
                            return new long[]{EmulatorScreen.WIDTH};
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "getHeight",
                        FunctionType.of(List.of(), List.of(ValType.I32)),
                        (Instance instance, long... args) -> {
                            return new long[]{EmulatorScreen.HEIGHT};
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "clearScreen",
                        FunctionType.of(List.of(ValType.I32), List.of()),
                        (Instance instance, long... args) -> {
                            var color = (int) args[0];
                            screen.clear((byte) color);
                            return null;
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "setPixel",
                        FunctionType.of(List.of(ValType.I32, ValType.I32, ValType.I32), List.of()),
                        (Instance instance, long... args) -> {
                            var x = (int) args[0];
                            var y = (int) args[1];
                            var color = (int) args[2];
                            screen.setPixel(x, y, (byte) color);
                            return null;
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "getPixel",
                        FunctionType.of(List.of(ValType.I32, ValType.I32), List.of(ValType.I32)),
                        (Instance instance, long... args) -> {
                            var x = (int) args[0];
                            var y = (int) args[1];
                            return new long[]{screen.getPixel(x, y)};
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "presentFrame",
                        FunctionType.of(List.of(ValType.I32, ValType.I32), List.of()),
                        (Instance instance, long... args) -> {
                            var base = (int) args[0];
                            var size = (int) args[1];

                            // no buffer overflow
                            if (size > screen.framebuffer.length) {
                                size = screen.framebuffer.length;
                            }

                            System.arraycopy(instance.memory().readBytes(base, size), 0, screen.framebuffer, 0, size);
                            return null;
                        }
                ));

        return store;
    }

    public EmulatorScreen getScreen() {
        return screen;
    }
}
