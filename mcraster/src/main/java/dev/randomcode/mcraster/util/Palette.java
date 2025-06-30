package dev.randomcode.mcraster.util;

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
        if (color < palette.size()) {
            return Registries.BLOCK.get(palette.get(color));
        } else {
            // leave holes in stuff with unknown colors
            return Blocks.AIR;
        }
    }

    public String toString() {
        return palette.toString();
    }
}
