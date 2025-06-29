package dev.randomcode.mcraster.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;

public class Palette {
    private final ArrayList<Identifier> palette = new ArrayList<>();

    public Palette(String paletteData) {
        var rawPalette = JsonHelper.deserializeArray(paletteData);
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
}
