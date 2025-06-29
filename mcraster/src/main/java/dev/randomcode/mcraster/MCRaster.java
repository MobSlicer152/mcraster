package dev.randomcode.mcraster;

import dev.randomcode.mcraster.util.Palette;
import dev.randomcode.mcraster.util.ResourceReloadListener;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class MCRaster implements ModInitializer {
	public static final String MOD_ID = "mcraster";
	public static final String MOD_VERSION = "v0";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static String PALETTE_JSON;
	public static Palette PALETTE;

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ResourceReloadListener());
	}
}
