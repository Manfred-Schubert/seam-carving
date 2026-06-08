package seamcarving

import java.awt.image.BufferedImage

/**
 * Resizes a [BufferedImage] to the specified dimensions using the seam carving algorithm.
 * * This method dynamically reduces the image size by iteratively identifying and removing
 * the lowest-energy paths (seams) of pixels, preserving visually important features
 * better than standard scaling. It prioritizes width reduction before height reduction.
 * * @param image The source [BufferedImage] to be resized.
 * @param newWidth The target width in pixels. Must be less than or equal to the current width.
 * @param newHeight The target height in pixels. Must be less than or equal to the current height.
 * @return A new [BufferedImage] downscaled to the requested [newWidth] and [newHeight].
 * * @throws IllegalArgumentException If the target dimensions are larger than the original image's
 * dimensions, or if the image does not have exactly 3 color components (e.g., RGB).
 */
fun resize(
    image: BufferedImage,
    newWidth: Int,
    newHeight: Int
): BufferedImage {
    require(image.width >= newWidth && image.height >= newHeight) {
        "Images can only be scaled down."
    }
    require(image.colorModel.numColorComponents == 3) {
        "Only images with 3 color components are supported."
    }

    val workingBuffer = image.copy()
    val imageView = BufferedImageView(workingBuffer)

    while (imageView.width > newWidth) {
        val seam = imageView
            .toEnergyMap(isSquared = false)
            .convertedToCostMap()
            .findSeam()

        imageView.removeSeam(seam)
    }

    val transposedImageView = TransposedImageView(imageView)

    while (transposedImageView.width > newHeight) {
        val seam = transposedImageView
            .toEnergyMap(isSquared = false)
            .convertedToCostMap()
            .findSeam()

        transposedImageView.removeSeam(seam)
    }

    return transposedImageView.toBufferedImage()
}

/**
 * Generates a copy of the [image] with its lowest-energy vertical seam
 * highlighted in red.
 */
fun visualizeSeam(
    image: BufferedImage
): BufferedImage {
    require(image.colorModel.numColorComponents == 3) {
        "Only images with 3 color components are supported."
    }

    val workingBuffer = image.copy()
    val imageView = BufferedImageView(workingBuffer)
    val seam = imageView
        .toEnergyMap(isSquared = false)
        .convertedToCostMap()
        .findSeam()
    imageView.drawSeam(seam)

    return imageView.toBufferedImage()
}