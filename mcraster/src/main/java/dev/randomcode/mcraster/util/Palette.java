package dev.randomcode.mcraster.util;

import dev.randomcode.mcraster.MCRaster;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.Reader;
import java.util.ArrayList;

public class Palette {
    private final ArrayList<Identifier> palette = new ArrayList<>();

    public Palette(Reader reader) {
        var rawPalette = JsonHelper.deserializeArray(reader);
        rawPalette.forEach((name) -> {
            palette.add(Identifier.tryParse(name.getAsString()));
        });
    }

    public Block get(byte color) {
		// ensure the color is unsigned. i get discouraging unsigned types, but not implementing them is crazy.
		int realColor = color & 0xFF;
        if (realColor < palette.size()) {
            return Registries.BLOCK.get(palette.get(realColor));
        } else {
            // leave holes in stuff with unknown colors
            MCRaster.LOGGER.warn("bad color {}", realColor);
            return Blocks.AIR;
        }
    }

    public String toString() {
        return palette.toString();
    }
}
