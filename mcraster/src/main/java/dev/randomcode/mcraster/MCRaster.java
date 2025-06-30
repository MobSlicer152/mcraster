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

public class MCRaster implements ModInitializer {
	public static final String MOD_ID = "mcraster";
	public static final String MOD_VERSION = "v0";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("MCRaster");

	public static Palette PALETTE;
	public static WasmModule MODULE;

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
                    PALETTE = new Palette(palette.getReader());
					LOGGER.info("Palette: {}", PALETTE);

					LOGGER.info("Loading WASM module");
					var module = manager.getResource(identifier("program.wasm")).orElseThrow();
					MODULE = Parser.parse(module.getInputStream());
					LOGGER.info("Loaded WASM module {}", MODULE.toString());
                } catch (Exception e) {
                    LOGGER.error("Failed to load palette: {}", e.toString());
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
