use fast_image_resize::{ImageView, ImageViewMut, Resizer, ResizeAlg, ResizeOptions, images::{TypedImage, TypedImageRef}, pixels};
    
#[link(wasm_import_module = "mcraster")]
unsafe extern "C" {
    fn running() -> i32;
    fn getWidth() -> i32;
    fn getHeight() -> i32;
    fn clearScreen(color: i32);
    fn presentFrame(buffer: *const u8, size: i32);
    fn getPixel(x: i32, y: i32) -> i32;
    fn setPixel(x: i32, y: i32, color: i32);
    fn getPaletteCount() -> i32;
    fn setPalette(index: i32);
}

struct Doom;

impl doomgeneric::game::DoomGeneric for Doom {
    fn draw_frame(&mut self, screen_buffer: &[u8], xres: usize, yres: usize) {
        let screen_image: TypedImageRef<'_, pixels::U8> = TypedImageRef::from_buffer(xres as u32, yres as u32, screen_buffer).expect("failed to make image reference");
        let mut emulator_image = TypedImage::new(unsafe { getWidth() } as u32, unsafe { getHeight() } as u32);
        let mut resizer = Resizer::new();
        let options = &ResizeOptions::new().resize_alg(ResizeAlg::Nearest).use_alpha(false);
        resizer.resize_typed(&screen_image, &mut emulator_image, Some(options));

        let pixels = &emulator_image.pixels();
        unsafe { presentFrame(pixels.as_ptr() as *const u8, pixels.len() as i32); }
    }

    fn get_key(&mut self) -> Option<doomgeneric::input::KeyData> {
        // TODO
        None
    }

    fn set_window_title(&mut self, title: &str) {
        
    }

    fn set_palette_index(&mut self, palette: i32) {
        unsafe { setPalette(palette) };
    }
}

fn main() {
    println!("starting doom");

    let doom = Doom;
    doomgeneric::game::init(doom);
    while unsafe { running() } != 0 {
        doomgeneric::game::tick();
    }
}
