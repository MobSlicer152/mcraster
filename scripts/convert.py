import argparse
import json
import numpy as np
import os

from os import path
from PIL import Image

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", "-i", help="The image to convert")
    parser.add_argument("--output", "-o", help="Write the converted image to the given file")
    parser.add_argument("--palette", "-p", default="mc_palette.json", help="The block palette")
    parser.add_argument("--minecraft-assets", "-a", help="The path to the assets folder of an extracted Minecraft client JAR")
    
    args = parser.parse_args()
    input = args.input
    output = args.output
    palette_json = args.palette
    assets = args.minecraft_assets
    
    palette = None
    with open(palette_json, "r") as f:
        palette = json.load(f)
    
    blocks = path.join(assets, "minecraft", "textures", "block")
    
    img = Image.open(input)
    out = Image.new("RGB", (img.width * 16, img.height * 16))
    for y in range(0, img.height):
        for x in range(0, img.width):
            i = img.getpixel((x, y))
            name = path.join(blocks, f"{palette[i]}.png")
            if not path.exists(name):
                for block in os.listdir(blocks):
                    if block.find(palette[i]) != -1:
                        name = path.join(blocks, block)
                        break
            if path.exists(name):
                block = Image.open(name)
                out.paste(block, (x * 16, y * 16))
                
    out.save(output)

if __name__ == "__main__":
    main()
