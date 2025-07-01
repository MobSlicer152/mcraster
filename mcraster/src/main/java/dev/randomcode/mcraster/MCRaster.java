package dev.randomcode.mcraster;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import dev.randomcode.mcraster.entity.EntityType;
import dev.randomcode.mcraster.util.Palette;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class MCRaster implements ModInitializer {
    public static final String MOD_ID = "mcraster";
    public static final String MOD_VERSION = "v0";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("MCRaster");

    public static Palette palette;
    public static WasmModule module;
    public static Path tempZip;
    public static FileSystem dataFs;

    public static Identifier identifier(String name) {
        return Identifier.of(MCRaster.MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                try {
                    LOGGER.info("Loading palette");
                    var palette = manager.getResource(identifier("palette.json")).orElseThrow();
                    MCRaster.palette = new Palette(palette.getReader());
                    LOGGER.info("Palette: {}", MCRaster.palette);

                    LOGGER.info("Loading WASM module");
                    var module = manager.getResource(identifier("program.wasm")).orElseThrow();
                    MCRaster.module = Parser.parse(module.getInputStream());
                    LOGGER.info("Loaded WASM module {}", MCRaster.module);

                    LOGGER.info("Checking for data.zip");
                    var dataZip = manager.getResource(identifier("data.zip"));
                    dataZip.ifPresent(resource -> {
                        try {
                            var stream = resource.getInputStream();

                            if (tempZip != null) {
                                Files.deleteIfExists(tempZip);
                            }

                            tempZip = Files.createTempFile("data", ".zip");
                            LOGGER.info("Storing data.zip in {}", tempZip);
                            var outStream = new FileOutputStream(tempZip.toFile());
                            outStream.write(stream.readAllBytes());
                            dataFs = FileSystems.newFileSystem(tempZip);
                        } catch (Exception e) {
                            LOGGER.error("Failed to store data.zip", e);
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("Failed to load resource", e);
                }
            }

            @Override
            public Identifier getFabricId() {
                return identifier("palette_resource");
            }
        });

        EntityType.ensureInit();
    }
}
