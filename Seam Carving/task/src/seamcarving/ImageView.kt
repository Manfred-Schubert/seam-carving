package seamcarving

import java.awt.image.BufferedImage
import java.awt.image.ColorModel

/**
 * A unified abstraction layer over pixel-based images, decoupling seam-carving
 * algorithms from concrete image storage and memory layouts.
 */
interface ImageView {
    val width: Int
    val height: Int
    val colorModel: ColorModel
    fun getRGB(x: Int, y: Int): Int
    fun setRGB(x: Int, y: Int, rgb: Int)
    fun cropTo(width: Int, height: Int)
    fun toBufferedImage(): BufferedImage
}

/**
 * A concrete [ImageView] adapter that forwards operations directly to an
 * underlying [BufferedImage] without coordinates transformation.
 */
class BufferedImageView(private val image: BufferedImage): ImageView {
    private var cropWidth: Int = image.width
    private var cropHeight: Int = image.height

    override val width: Int
        get() = cropWidth
    override val height: Int
        get() = cropHeight
    override val colorModel: ColorModel
        get() = image.colorModel

    override fun getRGB(x: Int, y: Int): Int = image.getRGB(x, y)
    override fun setRGB(x: Int, y: Int, rgb: Int) { image.setRGB(x, y, rgb) }

    override fun cropTo(width: Int, height: Int) {
        require(width > 0 && height > 0
                && width <= cropWidth && height <= cropHeight) {
            "Internal inconsistency: image can only be cropped in."
        }

        cropWidth = width
        cropHeight = height
    }
    override fun toBufferedImage(): BufferedImage = image
        .getSubimage(0, 0, cropWidth, cropHeight)
}

/**
 * A zero-allocation decorator that transposes an [ImageView] by swapping axes
 * and flipping coordinates `(x, y) -> (y, x)`.
 * * Used to reuse vertical seam-carving algorithms for horizontal operations in $O(1)$ time.
 */
class TransposedImageView(private val image: ImageView): ImageView {
    override val width: Int
        get() = image.height
    override val height: Int
        get() = image.width
    override val colorModel: ColorModel
        get() = image.colorModel

    override fun getRGB(x: Int, y: Int): Int = image.getRGB(y, x)
    override fun setRGB(x: Int, y: Int, rgb: Int) { image.setRGB(y, x, rgb) }

    override fun cropTo(width: Int, height: Int) {
        image.cropTo(height, width)
    }
    override fun toBufferedImage(): BufferedImage = image.toBufferedImage()
}

/**
 * Shifts pixels leftward to overwrite the specified vertical [seam] path,
 * effectively shrinking the view's width by 1 pixel.
 */
fun ImageView.removeSeam(seam: IntArray) {
    require(seam.size == height) { "Internal inconsistency: invalid seam." }

    for (y in 0 ..< height) {
        for (x in seam[y] ..< width - 1) {
            val pixel = getRGB(x + 1, y)
            setRGB(x, y, pixel)
        }
    }

    cropTo(this.width - 1, this.height)
}

/**
 * Visualizes a vertical [seam] by painting its path across the image in solid red.
 */
fun ImageView.drawSeam(seam: IntArray) {
    require(seam.size == height) { "Invalid seam" }

    val height = this.height
    val width = this.width

    val red = PackedPixel.fromRGB(255, 0, 0)

    for (y in 0..<height) {
        val seamX = seam[y]

        if (seamX in 0..<width) {
            this.setRGB(seamX, y, red)
        }
    }
}