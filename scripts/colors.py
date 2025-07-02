import argparse
import cv2
import json
import numpy as np
import os
import re

from os import path

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--minecraft-assets", "-a", required=True, help="The path to the assets folder of an extracted Minecraft client JAR")
    parser.add_argument("--output", "-o", default="data/colors.json", help="Output JSON file")
    
    args = parser.parse_args()
    assets = args.minecraft_assets
    output = args.output
    
    block_models = path.join(assets, "minecraft", "models", "block")
    
    blocks = []
    for block in os.listdir(block_models):
        #if re.match(r".*(wool|concrete|terracotta)", block) is not None:
        model = None
        with open(path.join(block_models, block), "r") as f:
            model: dict = json.load(f)
            f.close()

            if len(model) > 0 and model.get("parent") is not None and model["parent"] == "minecraft:block/cube_all":
                block = model["textures"]["all"].split(':')
                namespace = block[0]
                name = block[1]
                blocks.append(path.join(namespace, "textures", name + ".png"))

    means = {}
    for block in blocks:
        texture = cv2.imread(path.join(assets, block))
        (b, g, r) = texture.mean(axis=(0, 1))
        color = (int(r) << 16) | (int(g) << 8) | (int(b) << 0)

        # guess the "id" of the block
        basename = path.splitext(path.basename(block))[0]
        if not re.fullmatch(r"(lit|powered)", basename): # if it's "lit", it's probably way different than the normal state
            name = re.sub(r"_(data|[0-9]|lit|powered)", "", basename)
            means[name] = f"{color:06X}"
        
    with open(output, "w") as f:
        json.dump(means, f, indent=4)

if __name__ == "__main__":
    main()
