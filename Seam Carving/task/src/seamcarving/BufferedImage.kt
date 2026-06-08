package seamcarving

import java.awt.Graphics
import java.awt.image.BufferedImage

/**
 * Executes the given [block] on this graphics context and ensures it is safely
 * disposed of in a `finally` block, preventing resource leaks.
 */
inline fun <T: Graphics, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        this.dispose()
    }
}

/**
 * Creates and returns a deep copy of this [BufferedImage] with the same
 * dimensions and image type.
 */
fun BufferedImage.copy(): BufferedImage {
    val copy = BufferedImage(width, height, type)

    copy.createGraphics().use { graphics ->
        graphics.drawImage(this, 0, 0, null)
    }

    return copy
}