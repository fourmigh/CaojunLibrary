package org.caojun.map

import android.graphics.Bitmap
import android.graphics.Matrix

object MatrixUtils {

    private val values = FloatArray(9)

    private fun getValues(matrix: Matrix) {
        matrix.getValues(values)
    }

    /**
     * Bitmap中的坐标转Canvas坐标
     */
    private fun transferCanvasCoordinateFromBitmap(bitmapCoordinate: BitmapCoordinate): CanvasCoordinate {
        val canvasCoordinate = CanvasCoordinate()
        canvasCoordinate.x = values[Matrix.MSCALE_X] * bitmapCoordinate.x + values[Matrix.MSKEW_X] * bitmapCoordinate.y + values[Matrix.MTRANS_X]
        canvasCoordinate.y = values[Matrix.MSKEW_Y] * bitmapCoordinate.x + values[Matrix.MSCALE_Y] * bitmapCoordinate.y + values[Matrix.MTRANS_Y]
        return canvasCoordinate
    }

    /**
     * 地图坐标转Bitmap坐标
     */
    private fun transferBitmapCoordinateFromMap(mapCoordinate: MapCoordinate, mapMin: MapCoordinate, mapMax: MapCoordinate, mapBitmap: Bitmap): BitmapCoordinate {
        val bitmapCoordinate = BitmapCoordinate()

        val w = mapBitmap.width
        val h = mapBitmap.height

        bitmapCoordinate.x = (w * (mapCoordinate.x - mapMin.x) / (mapMax.x - mapMin.x)).toFloat()
        bitmapCoordinate.y = (h * (mapMax.y - mapCoordinate.y) / (mapMax.y - mapMin.y)).toFloat()
        return bitmapCoordinate
    }

    /**
     * 地图坐标转Canvas坐标
     */
    fun transferCanvasCoordinateFromMap(matrix: Matrix, mapCoordinate: MapCoordinate, mapMin: MapCoordinate, mapMax: MapCoordinate, mapBitmap: Bitmap): CanvasCoordinate {
        getValues(matrix)
        val bitmapCoordinate = transferBitmapCoordinateFromMap(mapCoordinate, mapMin, mapMax, mapBitmap)
        return transferCanvasCoordinateFromBitmap(bitmapCoordinate)
    }
}