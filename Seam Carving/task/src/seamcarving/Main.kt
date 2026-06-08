package seamcarving

import java.io.File
import javax.imageio.ImageIO

/**
 * Holds commandline parsing logic and operational constraints for the seam carving runner.
 */
data class Configuration(
    val inputPath: String,
    val outputPath: String,
    val horizontalCrop: Int,
    val verticalCrop: Int,
    val visualizeSeam: Boolean
) {
    companion object {
        fun from(args: Array<String>): Configuration? {
            var inputPath: String? = null
            var outputPath: String? = null
            var horizontal: Int? = null
            var vertical: Int? = null
            var visualizeSeam = false
            var index = 0

            while (index < args.size) {
                when (args[index]) {
                    "-in" -> {
                        if (index + 1 >= args.size) return null
                        inputPath = args[index + 1]
                        index += 2
                    }
                    "-out" -> {
                        if (index + 1 >= args.size) return null
                        outputPath = args[index + 1]
                        index += 2
                    }
                    "-width" -> {
                        if (index + 1 >= args.size) return null
                        horizontal = args[index + 1].toIntOrNull()
                        index += 2
                    }
                    "-height" -> {
                        if (index + 1 >= args.size) return null
                        vertical = args[index + 1].toIntOrNull()
                        index += 2
                    }
                    "-visualize" -> {
                        visualizeSeam = true
                        index += 1
                    }
                    else -> return null
                }
            }

            if (inputPath == null || outputPath == null) return null

            return Configuration(
                inputPath,
                outputPath,
                horizontal ?: 0,
                vertical ?: 0,
                visualizeSeam
            )
        }
    }
}

fun main(args: Array<String>) {
    val configuration = Configuration.from(args) ?: run {
        println("Invalid arguments. Usage: -in <input_file> -out <output_file>")
        return
    }

    val inputFile = File(configuration.inputPath)
    val outputFile = File(configuration.outputPath)

    if (!inputFile.exists()) {
        println("The file ${configuration.inputPath} doesn't exist.")
        return
    }

    val image = ImageIO.read(inputFile) ?: run {
        println("Input file $inputFile could not be read.")
        return
    }

    if (configuration.visualizeSeam) {
        // Just create an image with the visualization of the lowest
        // energy vertical seam.
        try {
            val result = visualizeSeam(image)

            ImageIO.write(
                result,
                outputFile.extension.takeIf { it.isNotEmpty() } ?: "png",
                outputFile)
        } catch (e: Exception) {
            println(e.message)
        }
        return
    }

    val horizontalCrop = configuration.horizontalCrop
    if (horizontalCrop < 0 || horizontalCrop > (image.width - 3)) {
        println("Invalid width.")
        return
    }
    val verticalCrop = configuration.verticalCrop
    if (verticalCrop < 0 || verticalCrop > (image.height - 3)) {
        println("Invalid height.")
        return
    }

    val targetWidth = image.width - horizontalCrop
    val targetHeight = image.height - verticalCrop

    try {
        val result = resize(
            image,
            newWidth = targetWidth,
            newHeight = targetHeight
        )

        ImageIO.write(
            result,
            outputFile.extension.takeIf { it.isNotEmpty() } ?: "png",
            outputFile)
    } catch (e: Exception) {
        println(e.message)
    }
}
