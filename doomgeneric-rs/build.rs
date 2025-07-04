// Example custom build script.
fn main() -> Result<(), Box<dyn std::error::Error>> {
    let ref dg_src_dir = std::path::PathBuf::from("doomgeneric/doomgeneric");
    let mut dg_c_paths = vec![];
    let mut dg_h_paths = vec![];

    // Find most c and h files
    for entry in std::fs::read_dir(dg_src_dir)? {
        let entry = entry?;
        if let Some(filename) = entry.file_name().to_str() {
            if filename.starts_with("doomgeneric")
                || filename.contains("_allegro")
                || filename.contains("_sdl")
                || filename == "i_main.c" {
                continue;
            }

            if filename.ends_with(".h") {
                dg_h_paths.push(dg_src_dir.join(filename));
            } else if filename.ends_with(".c") {
                dg_c_paths.push(dg_src_dir.join(filename));
            }
        }
    }
    dg_c_paths
        .iter()
        .chain(dg_h_paths.iter())
        .for_each(|path| println!("cargo:rerun-if-changed={}", path.to_str().unwrap()));

    cc::Build::new()
        .flag("-w") // Disable warnings
        .define("DOOMGENERIC_RESX", "320")
        .define("DOOMGENERIC_RESY", "200")
        .define("CMAP256", None)
        .files(dg_c_paths)
        .compile("doomgeneric");

    //println!("cargo:rustc-link-lib=static=doomgeneric");
    Ok(())
}
