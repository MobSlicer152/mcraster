fn main() {
    println!("cargo:rustc-link-arg=-sERROR_ON_UNDEFINED_SYMBOLS=0");
    println!("cargo:rustc-link-arg=-sEXPORT_ALL");
    println!("cargo:rustc-link-arg=-sEXPORTED_FUNCTIONS=[\"mcraster.presentFrame\",\"mcraster.running\"]");
}
