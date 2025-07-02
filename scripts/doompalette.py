import argparse
import json
import os
import struct

from os import path

class WAD:
    MAGIC1 = b'IWAD'
    MAGIC2 = b'PWAD'

    class FileLump:
        def __init__(self, f):
            self.filepos = struct.unpack("<i", f.read(4))[0]
            self.size = struct.unpack("<i", f.read(4))[0]
            self.name = f.read(8)
            last = f.tell()
            f.seek(self.filepos, os.SEEK_SET)
            self.data = f.read(self.size)
            f.seek(last, os.SEEK_SET)

    def __init__(self, name):
        with open(name, "rb") as f:
            size = f.seek(0, os.SEEK_END)
            f.seek(0, os.SEEK_SET)

            self.magic = f.read(4)
            if self.magic != self.MAGIC1 and self.magic != self.MAGIC2:
                raise ValueError(f"invalid magic, should be {self.MAGIC1} or {self.MAGIC2}, got {self.magic}")

            self.numlumps = struct.unpack("<i", f.read(4))[0]
            self.infotableofs = struct.unpack("<i", f.read(4))[0]

            if size < self.infotableofs:
                raise ValueError(f"info table is at {self.infotableofs}, but file is only {size} bytes")

            f.seek(self.infotableofs, os.SEEK_SET)
            self.dirs = []
            for i in range(0, self.numlumps):
                self.dirs.append(self.FileLump(f))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--wad", "-w", default="doom1.wad")
    parser.add_argument("--palette-basename", "-p", "-o", default="data/palette_.json")

    args = parser.parse_args()

    wad = WAD(args.wad)
    i = 0
    while i < wad.numlumps and wad.dirs[i].name != b"PLAYPAL\0":
        i += 1

    playpal = wad.dirs[i]
    palsize = 3 * 256
    numpals = playpal.size // palsize # 3 bytes per color, 256 colors per palette
    print(f"got {numpals} palettes from lump {i} in WAD {args.wad}");
    (name, ext) = path.splitext(args.palette_basename)
    for i in range(0, numpals):
        pal = []
        for j in range(0, 256):
            r = playpal.data[i * palsize + j * 3 + 0]
            g = playpal.data[i * palsize + j * 3 + 1]
            b = playpal.data[i * palsize + j * 3 + 2]
            rgb = (r << 16) | (g << 8) | (b << 0)
            pal.append(f"{rgb:06X}")

        out = f"{name}{i}{ext}"
        print(f"writing palette {out}")
        with open(f"{out}", "w") as f:
            json.dump(pal, f, indent=4)


if __name__ == "__main__":
    main()

