package seamcarving

/**
 * Represents a color encoded as a single 32-bit integer in ARGB format.
 */
typealias PackedPixel = Int
typealias PackedPixelCompanion = Int.Companion

val PackedPixel.alpha: Int
    get() = (this shr 24) and 0xFF

fun PackedPixel.withAlpha(alpha: Int): PackedPixel {
    return (this and 0x00FFFFFF) or ((alpha and 0xFF) shl 24)
}

val PackedPixel.red: Int
    get() = (this shr 16) and 0xFF

fun PackedPixel.withRed(red: Int): PackedPixel {
    return (this and 0xFF00FFFF.toInt()) or ((red and 0xFF) shl 16)
}

val PackedPixel.green: Int
    get() = (this shr 8) and 0xFF

fun PackedPixel.withGreen(green: Int): PackedPixel {
    return (this and 0xFFFF00FF.toInt()) or ((green and 0xFF) shl 8)
}

val PackedPixel.blue: Int
    get() = this and 0xFF

fun PackedPixel.withBlue(blue: Int): PackedPixel {
    return (this and 0xFFFFFF00.toInt()) or (blue and 0xFF)
}

/**
 * Packs individual ARGB channels into a single [PackedPixel].
 */
fun PackedPixelCompanion.fromRGB(red: Int, green: Int, blue: Int, alpha: Int = 255): PackedPixel {
    return ((alpha and 0xFF) shl 24) or
            ((red and 0xFF) shl 16) or
            ((green and 0xFF) shl 8) or
            (blue and 0xFF)
}

/**
 * Creates an opaque grayscale [PackedPixel] from a single intensity value.
 */
fun PackedPixelCompanion.fromGray(gray: Int): PackedPixel {
    return fromRGB(red = gray, green = gray, blue = gray, alpha = 255)
}

