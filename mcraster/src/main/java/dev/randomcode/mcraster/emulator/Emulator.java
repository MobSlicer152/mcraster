package dev.randomcode.mcraster.emulator;

import com.dylibso.chicory.runtime.*;

import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.types.Export;
import com.dylibso.chicory.wasm.types.FunctionType;
import com.dylibso.chicory.wasm.types.ValType;
import dev.randomcode.mcraster.MCRaster;
import dev.randomcode.mcraster.entity.EmulatorEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class Emulator extends Thread {
    private final EmulatorEntity parent;
    private final AtomicBoolean running;
    private int callbackPtr = 0;
    private ExportFunction keyCallback;
    private EmulatorScreen screen;

    public Emulator(EmulatorEntity parent) {
        super();

        this.parent = parent;
        this.running = new AtomicBoolean(false);
        setName("Emulator thread");
    }

    private static class EmulatorOutputStream extends OutputStream {
        private final Function<String, Void> output;
        private final StringBuilder buffer;

        public EmulatorOutputStream(Function<String, Void> write) {
            output = write;
            buffer = new StringBuilder();
        }

        @Override
        public void write(int b) throws IOException {
            if (b == '\n') {
                output.apply(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append((char) b);
            }
        }
    }

    @Override
    public void run() {
        MCRaster.LOGGER.debug("Emulator started");

        screen = new EmulatorScreen();
        var wasi = initWasi();
        var store = initHostFunctions(wasi);

        running.set(true);
        while (running()) {
            screen.clear((byte) 0);

            try {
                var instance = store.instantiate("program", MCRaster.module);
                //initCallbacks(instance);
            } catch (Exception e) {
                MCRaster.LOGGER.error("Failed to run emulator", e);
            }

            MCRaster.LOGGER.info("Restarting emulator in 500ms");
            try {
                sleep(500);
            } catch (InterruptedException e) {
                // doesn't need to be precise at all, just an arbitrary wait
            }
        }

        running.set(false);
    }

    public boolean running() {
        return parent.isAlive() && running.get();
    }

    public void shutdown() {
        MCRaster.LOGGER.debug("Emulator shutting down");
        running.set(false);
    }

    private WasiPreview1 initWasi() {
        var stdout = new EmulatorOutputStream(s -> {
            MCRaster.LOGGER.info(s);
            return null;
        });
        var stderr = new EmulatorOutputStream(s -> {
            MCRaster.LOGGER.error(s);
            return null;
        });
        var optionBuilder = WasiOptions.builder().withStdout(stdout).withStderr(stderr);
        if (MCRaster.dataFs != null) {
            optionBuilder.withDirectory(".", MCRaster.dataFs.getPath("/"));
        }
        return WasiPreview1.builder().withOptions(optionBuilder.build()).build();
    }

    private void initCallbacks(Instance instance) {
        keyCallback = instance.export("keyCallback");
    }

    private Store initHostFunctions(WasiPreview1 wasi) {
        var store = new Store();
        store.addFunction(wasi.toHostFunctions());
        store.addFunction(
                new ImportFunction("env", "system",
                        FunctionType.of(List.of(ValType.I32), List.of(ValType.I32)),
                        (Instance instance, long... args) -> {
                            return new long[]{1};
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "running",
                        FunctionType.of(List.of(), List.of(ValType.I32)),
                        (Instance instance, long... args) -> new long[]{running() ? 1 : 0}
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "getWidth",
                        FunctionType.of(List.of(), List.of(ValType.I32)),
                        (Instance instance, long... args) -> new long[]{EmulatorScreen.WIDTH}
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "getHeight",
                        FunctionType.of(List.of(), List.of(ValType.I32)),
                        (Instance instance, long... args) -> new long[]{EmulatorScreen.HEIGHT}
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
                new ImportFunction(MCRaster.MOD_ID, "setPalette",
                        FunctionType.of(List.of(ValType.I32), List.of()),
                        (Instance instance, long... args) -> {
                            var palette = (int) args[0];
							screen.setPalette(palette);
                            return null;
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "presentFrame",
                        FunctionType.of(List.of(ValType.I32, ValType.I32), List.of()),
                        (Instance instance, long... args) -> {
                            var base = (int) args[0];
                            var size = (int) args[1];

                            // no buffer overflow pls
                            if (size > screen.framebuffer.length) {
                                size = screen.framebuffer.length;
                            }

                            System.arraycopy(instance.memory().readBytes(base, size), 0, screen.framebuffer, 0, size);
                            return null;
                        }
                ));
        store.addFunction(
                new ImportFunction(MCRaster.MOD_ID, "setCallbackData",
                        FunctionType.of(List.of(ValType.I32), List.of()),
                        (Instance instance, long... args) -> {
                            callbackPtr = (int) args[0];
                            return null;
                        }
                ));

        return store;
    }

    public EmulatorScreen getScreen() {
        return screen;
    }
}
