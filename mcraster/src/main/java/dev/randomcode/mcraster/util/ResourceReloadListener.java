package dev.randomcode.mcraster.util;

import dev.randomcode.mcraster.MCRaster;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ResourceReloadListener implements SimpleResourceReloadListener {
    @Override
    public CompletableFuture<String> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var paletteResource = manager.getResource(Identifier.of(MCRaster.MOD_ID, "palette.json")).orElseThrow();
                var reader = paletteResource.getReader();
                var data = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }

                return data.toString();
            } catch (Exception e) {
                MCRaster.LOGGER.error("error: failed to read palette.json: {e}");
                return "";
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Object data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            MCRaster.PALETTE = new Palette((String)data);
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return null;
    }
}
