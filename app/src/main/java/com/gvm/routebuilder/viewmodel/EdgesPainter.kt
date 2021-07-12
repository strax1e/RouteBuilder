package com.gvm.routebuilder.viewmodel

import android.graphics.*
import android.view.View
import com.gvm.routebuilder.antalgorithm.Edge

object EdgesPainter {

    /**
     * Creates bitmap edges with pheromones
     * @param[width] width of bitmap
     * @param[height] height of bitmap
     * @return bitmap with edges
     */
    fun createPheromonesEdges(
        edges: Collection<Pair<Edge, Float>>,
        nodes: Map<Short, View>, width: Int, height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            strokeWidth = convertDpToPx(2.5f)
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        val paths = ArrayList<Pair<Path, Short>>()
        edges.forEach {
            val nodeA = nodes[it.first.nodeA]
            val nodeB = nodes[it.first.nodeB]

            val offset = convertDpToPx(21f / 2)
            var startXY = Pair(nodeA!!.x + offset, nodeA.y + offset)
            var destinationXY = Pair(nodeB!!.x + offset, nodeB.y + offset)
            if (destinationXY.first < startXY.first) {
                startXY = destinationXY.also { destinationXY = startXY }
            }

            val maxPheromones = edges.maxOf { edge -> edge.second }
            paint.color = Color.rgb(1 - it.second / maxPheromones, 1f, 1f)
            paths.add(Pair(this.createEdge(startXY, destinationXY, bitmap, paint), it.first.cost))
        }
        this.setEdgesText(paths, paint, bitmap)

        return bitmap
    }

    /**
     * Creates bitmap edges with path
     * @param[width] width of bitmap
     * @param[height] height of bitmap
     * @return bitmap with edges
     */
    fun createEdgesWithPath(
        path: Collection<Edge>?, edges: Collection<Edge>,
        nodes: Map<Short, View>, width: Int, height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            strokeWidth = convertDpToPx(2.5f)
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        val paths = ArrayList<Pair<Path, Short>>()
        edges.forEach {
            val nodeA = nodes[it.nodeA]
            val nodeB = nodes[it.nodeB]

            val offset = convertDpToPx(21f / 2)
            var startXY = Pair(nodeA!!.x + offset, nodeA.y + offset)
            var destinationXY = Pair(nodeB!!.x + offset, nodeB.y + offset)
            if (destinationXY.first < startXY.first) {
                startXY = destinationXY.also { destinationXY = startXY }
            }

            paint.color = if (path != null && path.contains(it)) Color.YELLOW else Color.rgb(0x9F, 0x9F, 0x9F)
            paths.add(Pair(this.createEdge(startXY, destinationXY, bitmap, paint), it.cost))
        }
        this.setEdgesText(paths, paint, bitmap)

        return bitmap
    }

    /**
     * Creates a edges for loaded roads.
     * @param[width] width of bitmap
     * @param[height] height of bitmap
     * @return bitmap with edges
     */
    fun createRawEdges(edges: Collection<Edge>, nodes: Map<Short, View>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            color = Color.rgb(0x9F, 0x9F, 0x9F)
            strokeWidth = convertDpToPx(2.5f)
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        val paths = ArrayList<Pair<Path, Short>>()
        edges.forEach {
            val buttonA = nodes[it.nodeA]
            val buttonB = nodes[it.nodeB]

            val offset = convertDpToPx(21f / 2)
            var startXY = Pair(buttonA!!.x + offset, buttonA.y + offset)
            var destinationXY = Pair(buttonB!!.x + offset, buttonB.y + offset)
            if (destinationXY.first < startXY.first) {
                startXY = destinationXY.also { destinationXY = startXY }
            }

            paths.add(Pair(this.createEdge(startXY, destinationXY, bitmap, paint), it.cost))
        }
        this.setEdgesText(paths, paint, bitmap)

        return bitmap
    }

    /**
     * Creates edge on bitmap
     * @return edge
     */
    private fun createEdge(
        startXY: Pair<Float, Float>,
        destinationXY: Pair<Float, Float>, bitmap: Bitmap, paint: Paint
    ): Path {
        val path = Path().apply {
            moveTo(startXY.first, startXY.second)
            lineTo(destinationXY.first, destinationXY.second)
        }
        Canvas(bitmap).drawPath(path, paint)
        return path
    }

    /**
     * Set the text for edges.
     * @param[paths] the paths along which the text draw
     * @param[paint] the paint used for the text
     * @param[bitmap] the bitmap used for stored
     * @return bitmap
     */
    private fun setEdgesText(paths: ArrayList<Pair<Path, Short>>, paint: Paint, bitmap: Bitmap): Bitmap {
        paint.apply {
            textAlign = Paint.Align.CENTER
            textSize = convertDpToPx(12f)
        }
        paths.forEach {
            setPaintAttrs(paint, Paint.Style.STROKE, Color.DKGRAY)
            Canvas(bitmap).drawTextOnPath(it.second.toString(), it.first, 0f, 16f, paint)

            setPaintAttrs(paint, Paint.Style.FILL, Color.WHITE)
            Canvas(bitmap).drawTextOnPath(it.second.toString(), it.first, 0f, 16f, paint)
        }
        return bitmap
    }

    /**
     * Set the attributes for the paint.
     * @param[paint] the paint which is changing
     * @param[style] style attribute
     * @param[color] color attribute
     * @return paint
     */
    private fun setPaintAttrs(paint: Paint, style: Paint.Style, color: Int): Paint {
        paint.style = style
        paint.color = color
        return paint
    }

    lateinit var convertDpToPx: (Float) -> Float
}