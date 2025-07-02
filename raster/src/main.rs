use doomgeneric::{game::{self, DoomGeneric}, input::KeyData};
use fast_image_resize::{
    ResizeAlg, ResizeOptions, Resizer,
    images::{TypedImage, TypedImageRef},
    pixels,
};

#[link(wasm_import_module = "mcraster")]
unsafe extern "C" {
    fn running() -> i32;
    fn getWidth() -> i32;
    fn getHeight() -> i32;
    //fn clearScreen(color: i32);
    fn presentFrame(buffer: *const u8, size: i32);
    //fn getPixel(x: i32, y: i32) -> i32;
    //fn setPixel(x: i32, y: i32, color: i32);
    fn setPalette(index: i32);
    fn setCallbackData(base: i32);
}

struct RingBuf<T, const N: usize> where T: Copy {
    buffer: [T; N],
    read_head: usize,
    write_head: usize
}

impl<T: Copy, const N: usize> RingBuf<T, N> {
    pub fn new(value: T) -> Self {
        Self {
            buffer: [value; N],
            read_head: 0,
            write_head: 0,
        }
    }

    pub fn write(&mut self, value: T) {
        self.buffer[self.write_head] = value;
        self.write_head = (self.write_head) % self.buffer.len();
    }

    pub fn read(&mut self) -> Option<T> {
        if self.read_head == self.write_head {
            None
        } else {
            let value = self.buffer[self.read_head];
            self.read_head = (self.read_head) % self.buffer.len();
            Some(value)
        }
    }
}

struct Doom {
    pub keys: RingBuf<KeyData, 16>
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
fn keyCallback(doom: *mut Doom, pressed: i32, key: i32) {
    if !doom.is_null() {
        let doom = unsafe { &mut *doom };
        doom.keys.write(KeyData { pressed: pressed != 0, key: key as u8 });
    }
}

impl DoomGeneric for Doom {
    fn draw_frame(&mut self, screen_buffer: &[u8], xres: usize, yres: usize) {
        let screen_image: TypedImageRef<'_, pixels::U8> =
            TypedImageRef::from_buffer(xres as u32, yres as u32, screen_buffer)
                .expect("failed to make image reference");
        let mut emulator_image =
            TypedImage::new(unsafe { getWidth() } as u32, unsafe { getHeight() } as u32);
        let mut resizer = Resizer::new();
        let options = &ResizeOptions::new()
            .resize_alg(ResizeAlg::Nearest)
            .use_alpha(false);
        let _ = resizer.resize_typed(&screen_image, &mut emulator_image, Some(options));

        let pixels = emulator_image.pixels();
        unsafe {
            presentFrame(pixels.as_ptr() as *const u8, pixels.len() as i32);
        }
    }

    fn get_key(&mut self) -> Option<KeyData> {
        self.keys.read()
    }

    fn set_window_title(&mut self, _title: &str) {}

    fn set_palette_index(&mut self, palette: i32) {
        unsafe { setPalette(palette) };
    }
}

fn main() {
    println!(
        "Starting doom, emulator screen is {}x{}\n",
        unsafe { getWidth() },
        unsafe { getHeight() }
    );

    let mut doom = Doom {
        keys: RingBuf::new(KeyData { pressed: false, key: 0 })
    };

    unsafe { setCallbackData(&raw mut doom as i32); }

    game::init(doom);
    while unsafe { running() } != 0 {
        game::tick();
    }
}
