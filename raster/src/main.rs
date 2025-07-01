#[link(wasm_import_module = "mcraster")]
unsafe extern "C" {
    fn running() -> i32;
    fn getWidth() -> i32;
    fn getHeight() -> i32;
    fn clearScreen(color: i32);
    fn presentFrame(buffer: *const u8, size: i32);
    fn getPixel(x: i32, y: i32) -> i32;
    fn setPixel(x: i32, y: i32, color: i32);
}

struct Doom;

impl doomgeneric::game::DoomGeneric for Doom {
    fn draw_frame(&mut self, screen_buffer: &[u8], xres: usize, yres: usize) {
        unsafe { presentFrame(screen_buffer.as_ptr() as *const u8, screen_buffer.len() as i32); }
    }

    fn get_key(&mut self) -> Option<doomgeneric::input::KeyData> {
        None
    }

    fn set_window_title(&mut self, title: &str) {
        
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
