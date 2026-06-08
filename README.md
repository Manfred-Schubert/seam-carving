# Seam Carving Image Resizer (Kotlin)

A high-performance, content-aware command-line image resizing tool built utilizing modern structural design patterns and low-level bitwise operations.

This application was developed as a milestone project within the **Hyperskill / JetBrains Academy** Kotlin Core curriculum.

- **Course Project Page:** https://hyperskill.org/projects/100
- **My Hyperskill Profile:** https://hyperskill.org/profile/629496713

---

## Key Features
- **Content-Aware Resizing:** Implemented the Seam Carving algorithm to dynamically reduce image dimensions by identifying and removing paths of lowest energy, preserving important visual features.
- **Zero-Allocation Transposition:** Created a virtual `TransposedImageView` decorator that flips structural coordinate access $O(1)$ in time and space, enabling total reuse of the vertical seam-finding algorithm along the horizontal axis.
- **Dynamic Programming Cost Routing:** Modeled directional matrix accumulations to track optimal paths across an image, utilizing a backtracking technique to isolate the absolute lowest-energy seam.
- **High-Performance Bit Manipulation:** Formulated a zero-overhead `PackedPixel` typealias bound directly to native 32-bit integers, executing raw bitwise masks and shifts for ultra-fast color-channel extractions without garbage collection pressure.
- **Seam Visualization Pathing:** Integrated a diagnostic rendering pipeline capable of computing, mapping, and drawing isolated lowest-energy pixel seams in solid red onto a copied image buffer for algorithmic debugging.

## Tech Stack
- Kotlin (JVM)
- Java AWT Framework (`BufferedImage`, `ImageIO`)

## How to Run
1. Open the project root folder in IntelliJ IDEA.
2. Build the project or run the `main()` function inside `Main.kt`.
3. Execute via CLI using the required flags:
   ```bash
   -in <input_image.png> -out <output_image.png> -width <pixels_to_crop> -height <pixels_to_crop> [-visualize]
