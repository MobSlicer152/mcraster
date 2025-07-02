# 8-bit WASM block screen

This is an experimental but mostly functional project that adds a controllable screen.

## Features

- 320x200 256 color screen at close to 20Hz (depends on your computer, but that's the limit)
- Includes VGA mode 13h and Doom color palettes, and scripts to generate your own
- Script to make versions of 8-bit images out of Minecraft block textures (`scripts/convert.py`)
  [img/doom.png]()
- Full WebAssembly runtime using [Chicory](https://chicory.dev), with WASI and access to a virtualized filesystem backed by a zip file
- Palettes can be selected at runtime by the program
- Palettes and program are controlled with a datapack
- Generally doesn't crash the game when the data is invalid

## Problems

- The code is kinda messy in places, and mainly oriented towards Doom working
- Sometimes the emulator doesn't cleanly shut down
- Unloading the running datapack kinda screws it up
- Screen size isn't configurable yet, and 320x200 requires a high render distance to fully see
- I don't think block updates are prevented for the screen, I'm sure that increases lag
- There are probably glaring thread safety issues
- In general, this mod is a proof of concept and not very fleshed out

## Palette scripts

The block colors for 1.21.6 are precomputed in `data/colors.json`. You can update them like this:

```shell
python scripts/colors.py -a <extracted Minecraft client JAR>
```

The block palettes are in `data/palette_*.json`. You can make a color list like this, using HTML color codes:

```json
[
    "000000", // black
    "ff0000", // red
    "00ff00", // green
    "0000ff", // blue
    "ffffff", // white
    ...       // etc
    ...
    ...
]
```

Then, you can convert it to a block palette like this:

```shell
python scripts/palette.py
```

All the scripts use argparse and have their flags documented (except maybe the Doom one).

## Datapack layout

- `<datapack root folder>`
  - `data/mcraster`
    - `palettes`
      - The default VGA mode 13h palette is named `palette_99.json`
      - `palette_<n>.json`
    - `program`
      - The data zip is optional and can have any internal layout you want. The program
        should be compiled for `wasm32-unknown-unknown` or `wasm32-wasip1`.
      - `data.zip`
      - `program.wasm`
  - `pack.mcmeta`

## WASM API

The emulator exposes several functions to the guest:

```c
// all in the "mcraster" module, use __attribute__((import_module("mcraster"))) or equivalent

// check if the emulator is running
bool running(void);

// get the screen size. it's hardcoded, but only in emulator.EmulatorScreen.
int getWidth(void);
int getHeight(void);

// clear the screen
void clearScreen(uint8_t color);

// manipulate individual pixels
void setPixel(int x, int y, uint8_t color);
uint8_t getPixel(int x, int y);

// set the palette, starts at 0 and can be any loaded by the mod. the load order is displayed in the log.
void setPalette(int index);

// memcpy a guest framebuffer to the host
void presentFrame(uint8_t* buffer, int size);

// set the data to be used for callbacks once they're implemented
void setCallbackData(void* data);
```

Once I implement event callbacks, there will be optional functions like this that the guest can export:

```c
void keyCallback(void* userData, bool pressed, int key);
```

