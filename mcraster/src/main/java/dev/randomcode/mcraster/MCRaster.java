package dev.randomcode.mcraster;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.randomcode.mcraster.entity.EntityType;
import dev.randomcode.mcraster.util.IReloadable;
import dev.randomcode.mcraster.util.NaturalOrderComparator;
import dev.randomcode.mcraster.util.Palette;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class MCRaster implements ModInitializer {
    public static final String MOD_ID = "mcraster";
    public static final String MOD_VERSION = "v0";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("MCRaster");

    public static ArrayList<Palette> palettes;
    public static WasmModule module;
    public static Path tempZip;
    public static FileSystem dataFs;
    public static IReloadable emulatorEntity;
    public static ServerPlayerEntity grabPlayer;

    public static Identifier identifier(String name) {
        return Identifier.of(MCRaster.MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                try {
                    loadResources(manager);
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

    private static class ResourceWithID {
        public Identifier id;
        public Resource resource;

        public ResourceWithID(Identifier id, Resource resource) {
            this.id = id;
            this.resource = resource;
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    public void loadResources(ResourceManager manager) throws IOException {
        LOGGER.info("Loading palette(s)");
        palettes = new ArrayList<>();
        var paletteResources = new ArrayList<ResourceWithID>();

        // this indentation is vile
        for (var id : manager.findResources("palettes",
                path -> {
                    var pathStr = path.toString();
                    LOGGER.info(pathStr);
                    return pathStr.startsWith("mcraster:palettes/palette_") && pathStr.endsWith(".json");
                }).keySet()) {
            var palette = manager.getResource(id).orElseThrow();
            paletteResources.add(new ResourceWithID(id, palette));
        }

        // sort the palettes more nicely
        paletteResources.sort(new NaturalOrderComparator<>());
        LOGGER.info("Got {} palette(s)", paletteResources.size());
        for (var palette : paletteResources) {
            LOGGER.info("\t{}", palette);
            palettes.add(new Palette(palette.resource.getReader()));
        }

        LOGGER.info("Loading WASM module");
        var module = manager.getResource(identifier("program/program.wasm")).orElseThrow();
        MCRaster.module = Parser.parse(module.getInputStream());
        LOGGER.info("Loaded WASM module {}", MCRaster.module);

        LOGGER.info("Checking for data.zip");
        var dataZip = manager.getResource(identifier("program/data.zip"));
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

        if (emulatorEntity != null) {
            emulatorEntity.onReload();
        }
    }

    public static boolean grabbed() {
        return grabPlayer != null;
    }
}
