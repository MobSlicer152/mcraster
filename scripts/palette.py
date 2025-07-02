import argparse
import json
import numpy as np
import os

from os import path
from PIL import Image


def rgb2hsv(r, g, b):
    r /= 255
    g /= 255
    b /= 255
    maxc = max(r, g, b)
    minc = min(r, g, b)
    v = maxc
    if minc == maxc:
        return 0.0, 0.0, v
    s = (maxc - minc) / maxc
    rc = (maxc - r) / (maxc - minc)
    gc = (maxc - g) / (maxc - minc)
    bc = (maxc - b) / (maxc - minc)
    if r == maxc:
        h = 0.0 + bc - gc
    elif g == maxc:
        h = 2.0 + rc - bc
    else:
        h = 4.0 + gc - rc
    h = (h / 6.0) % 1.0
    return [h * 360, s * 100, v * 100]


def hex2hsv(x):
    x = int(x, 16)
    (r, g, b) = ((x >> 16) & 0xFF, (x >> 8) & 0xFF, (x >> 0) & 0xFF)
    return np.array(rgb2hsv(r, g, b))


def closest(colors, color):
    dists = np.sqrt(np.sum((colors - color) ** 2, axis=1))
    return np.argmin(dists)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--input", "-i", default="data/palette_99.json", nargs='+', help="The list of color values to map"
    )
    parser.add_argument(
        "--colors", "-c", default="data/colors.json", help="The color map JSON to use"
    )
    parser.add_argument("--first", "-f", default=99, type=int, help="The starting number for the palette(s)")
    parser.add_argument(
        "--output", "-o", default="mcraster/src/main/resources/data/mcraster/palettes", help="The output JSON file"
    )

    args = parser.parse_args()
    palette_jsons = args.input if type(args.input) == list else [args.input]
    print(palette_jsons)
    colors_json = args.colors
    n = args.first
    output = args.output

    color_map = None
    with open(colors_json, "r") as f:
        color_map = json.load(f)
    colors = np.array([hex2hsv(x) for x in color_map.values()])

    for palette_json in palette_jsons:
        palette = None
        with open(palette_json, "r") as f:
            palette = json.load(f)
        palette = np.array([hex2hsv(x) for x in palette])

        mc_palette = []
        names = list(color_map.keys())
        for color in palette:
            i = closest(colors, color)
            print(f"{color} -> {names[i]} {colors[i]}")
            mc_palette.append(names[i])

        with open(path.join(output, f"palette_{n}.json"), "w") as f:
            json.dump(mc_palette, f, indent=4)

        n += 1


if __name__ == "__main__":
    main()
