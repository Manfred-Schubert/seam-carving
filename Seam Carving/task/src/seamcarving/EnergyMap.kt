package seamcarving

import java.awt.image.BufferedImage
import kotlin.math.min
import kotlin.math.sqrt

/**
* A 2D grid storing energy or cost values for each pixel coordinate in an image.
*/
data class EnergyMap(val width: Int, val height: Int) {
    private val map: Array<DoubleArray>

    init {
        require(width > 0 && height > 0) {
            "Invalid width and height"
        }

        map = Array(height) { DoubleArray(width) }
    }


    val maxEnergy: Double
        get() = map.maxOf { it.max() }

    fun row(y: Int): DoubleArray = map[y]

    operator fun get(x: Int, y: Int): Double = map[y][x]
    operator fun set(x: Int, y: Int, value: Double) { map[y][x] = value }
}

fun dSquared(c1: PackedPixel, c2: PackedPixel): Double {
    val dr = c1.red - c2.red
    val dg = c1.green - c2.green
    val db = c1.blue - c2.blue

    return (dr * dr + dg * dg + db * db).toDouble()
}

/**
 * Computes the pixel-by-pixel [EnergyMap] of this view using a dual-gradient contrast operator.
 * @param isSquared If true, skips the square root step for raw gradient accumulation.
 */
inline fun <reified T : ImageView> T.toEnergyMap(
    isSquared: Boolean
): EnergyMap {
    require(colorModel.numColorComponents == 3) { "Invalid color model" }

    val width = this.width
    val height = this.height

    val xRange = 0 ..< width
    val yRange = 0..< height
    val xRangeInset = if (width > 2) 1..(width - 2) else 0..0
    val yRangeInset = if (height > 2) 1..(height - 2) else 0..0

    val energyMap = EnergyMap(width, height)

    for (y in 0..<height) {
        val yClamped = y.coerceIn(yRangeInset)
        val yUp = (yClamped - 1).coerceIn(yRange)
        val yDown = (yClamped + 1).coerceIn(yRange)

        for (x in 0..<width) {
            val xClamped = x.coerceIn(xRangeInset)
            val xLeft = (xClamped - 1).coerceIn(xRange)
            val xRight = (xClamped + 1).coerceIn(xRange)

            val pLeft = this.getRGB(xLeft, yClamped)
            val pRight = this.getRGB(xRight, yClamped)
            val pUp = this.getRGB(xClamped, yUp)
            val pDown = this.getRGB(xClamped, yDown)

            val dxSquared = dSquared(pLeft, pRight)
            val dySquared = dSquared(pUp, pDown)
            val e = if (isSquared) {
                dxSquared + dySquared
            } else {
                sqrt(dxSquared + dySquared)
            }
            energyMap[x, y] = e
        }
    }

    return energyMap
}

/**
 * Generates a dynamic programming cost map where each pixel represents the minimum cumulative
 * energy required to reach it from the top row.
 */
fun EnergyMap.convertedToCostMap(): EnergyMap {
    val height = this.height
    val width = this.width

    val energyMap = this
    val costMap = EnergyMap(width, height)

    // Initialize the first row with the first row of this energy map.
    for (x in 0 ..< width) {
        costMap[x, 0] = energyMap[x, 0]
    }

    // For each row take the min of the previous row and add to this pixel's energy.
    for (y in 1 ..< height) {
        for (x in 0 ..< width) {
            val e = energyMap[x, y]
            val yUp = y - 1
            var energyUpMin = costMap[x, yUp]
            for (direction in -1..1) {
                val xLeft = x + direction
                if (xLeft !in 0..<width) continue
                energyUpMin = min(costMap[xLeft, yUp], energyUpMin)
            }
            costMap[x, y] = e + energyUpMin
        }
    }

    return costMap
}

/**
 * Backtracks through a cost-based [EnergyMap] from bottom to top to identify the vertical path (seam)
 * with the absolute lowest total energy.
 * @return An array of x-coordinates corresponding to each row index from top to bottom.
 */
fun EnergyMap.findSeam(): IntArray {
    val costMap = this
    val height = costMap.height
    val seam = IntArray(height)

    // Start at the bottom with the lowest cost x coordinate.
    val lastRowIndex = height - 1
    val lastRow = costMap.row(lastRowIndex)
    var currentX = lastRow.indices
        .minBy { lastRow[it] }
    seam[lastRowIndex] = currentX

    // Work upwards to find the seam with the lowest energy.
    for (y in lastRowIndex downTo 1) {
        val yUp = y - 1
        var bestX = currentX
        var minCostPrev = costMap[currentX, yUp]

        if (currentX - 1 >= 0) {
            val leftCost = costMap[currentX - 1, yUp]
            if (leftCost < minCostPrev) {
                minCostPrev = leftCost
                bestX = currentX - 1
            }
        }

        if (currentX + 1 < width) {
            val rightCost = costMap[currentX + 1, yUp]
            if (rightCost < minCostPrev) {
                bestX = currentX + 1
            }
        }

        currentX = bestX
        seam[yUp] = currentX
    }

    return seam
}

/**
 * Visualizes this [EnergyMap] as a grayscale [BufferedImage], normalizing values against
 * the maximum energy present.
 */
@Suppress("unused")
fun EnergyMap.toBufferedImage(): BufferedImage {
    val height = this.height
    val width = this.width

    // Find the maximal energy for normalization.
    val maxEnergy = this.maxEnergy

    // Write normalized energy into the output buffer as luminosity.
    val output = BufferedImage(
        width,
        height,
        BufferedImage.TYPE_INT_RGB
    )

    if (maxEnergy == 0.0) return output

    for (y in 0..<height) {
        for (x in 0..<width) {
            val e = this[x, y]
            val intensity = (255.0 * e / maxEnergy).toInt()
            val gray = PackedPixel.fromGray(intensity)
            output.setRGB(x, y, gray)
        }
    }

    return output
}