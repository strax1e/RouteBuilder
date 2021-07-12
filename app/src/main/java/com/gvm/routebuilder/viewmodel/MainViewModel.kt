package com.gvm.routebuilder.viewmodel

import android.app.Application
import android.graphics.*
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gvm.routebuilder.R
import com.gvm.routebuilder.TownButton
import com.gvm.routebuilder.antalgorithm.getPathAndStates
import com.gvm.routebuilder.database.DbConnection
import com.gvm.routebuilder.database.entities.Road
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel class for MainActivity
 * @param[application] application
 * @property[isStopped] is a private property for controlling the state of the Start/Stop button
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Loads towns and roads from db server
     * @param[countryId] ID of country
     */
    fun loadTownsAndRoads(countryId: Short) {
        CoroutineScope(Dispatchers.IO).launch {
            DbConnection(hostDb, portDb).use {
                ldTownsAndRoads.postValue(Pair(it.getTowns(countryId), it.getRoads(countryId)))
            }
        }
    }

    /**
     * Loads towns and roads from db server
     */
    fun loadCountries() {
        CoroutineScope(Dispatchers.IO).launch {
            DbConnection(hostDb, portDb).use {
                ldCountries.postValue(it.getCountries())
            }
        }
    }

    /**
     * Gets country ID by its name from country map
     * @param[countryName] name of country
     * @return country ID
     */
    fun getCountryId(countryName: String): Short {
        return this.countries.value!!.toList().find { it.second == countryName }!!.first
    }

    /**
     * Creates a graph for loaded roads. Invoke only when the last one was loaded
     * @param[minXY] minimal coordinates
     * @param[maxXY] maximum coordinates
     */
    fun createGraph(minXY: Pair<Int, Int>, maxXY: Pair<Int, Int>) {
        CoroutineScope(Dispatchers.Default).launch {
            val newNodes = createNodes(minXY, maxXY)
            ldGraph.postValue(Pair(newNodes, createEdges(newNodes, minXY, maxXY)))
        }
    }

    /**
     * Creates a button for node
     * @param[busyXY] set of busy coordinates
     * @param[minXY] minimal coordinates
     * @param[maxXY] maximum coordinates
     * @return a button for node
     */
    private fun createButton(
        townId: Short,
        busyXY: MutableSet<Pair<Float, Float>>,
        minXY: Pair<Int, Int>,
        maxXY: Pair<Int, Int>
    ): TownButton {
        var coordinates: Pair<Float, Float>
        do {
            coordinates =
                Pair(
                    minXY.first + Random.nextFloat() * (maxXY.first - minXY.first - convertDpToPx(23f)),
                    minXY.second + Random.nextFloat() * (maxXY.second - minXY.second - convertDpToPx(23f))
                )
        } while (busyXY.contains(coordinates))
        busyXY.add(coordinates)
        val button = TownButton(townId, viewModelOwner)
        button.apply {
            visibility = Button.VISIBLE
            x = coordinates.first
            y = coordinates.second
            setBackgroundResource(R.drawable.town_button)
        }
        button.setOnClickListener(this.townButtonListener)
        return button
    }

    /**
     * Creates a nodes for loaded roads.
     * @param[minXY] minimal coordinates
     * @param[maxXY] maximum coordinates
     * @return nodes
     */
    private fun createNodes(minXY: Pair<Int, Int>, maxXY: Pair<Int, Int>): Map<Short, ImageButton> {
        val newNodes = HashMap<Short, ImageButton>()
        val coordinatesSet = HashSet<Pair<Float, Float>>()
        townsAndRoads.value!!.second.forEach {
            if (!newNodes.contains(it.nodeA)) {
                newNodes[it.nodeA] = createButton(coordinatesSet, minXY, maxXY)
            }
            if (!newNodes.contains(it.nodeB)) {
                newNodes[it.nodeB] = createButton(coordinatesSet, minXY, maxXY)
            }
        }
        return newNodes
    }

    /**
     * Creates a edges for loaded roads.
     * @param[minXY] minimal coordinates
     * @param[maxXY] maximum coordinates
     * @return nodes
     */
    private fun createEdges(nodes: Map<Short, ImageButton>, minXY: Pair<Int, Int>, maxXY: Pair<Int, Int>): Bitmap {
        val bitmap =
            Bitmap.createBitmap(maxXY.first - minXY.first, maxXY.second - minXY.second, Bitmap.Config.ARGB_8888)
        val paint = Paint()
        paint.apply {
            color = viewModelOwner.getColor(R.color.gray_light)
            strokeWidth = convertDpToPx(2.5f)
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        val paths = ArrayList<Pair<Path, Short>>()
        townsAndRoads.value!!.second.forEach {
            val buttonA = nodes[it.nodeA]
            val buttonB = nodes[it.nodeB]
            val offset = convertDpToPx(21f / 2)

            var coordStart = Pair(buttonA!!.x + offset, buttonA.y + offset)
            var coordDestination = Pair(buttonB!!.x + offset, buttonB.y + offset)
            if (coordDestination.first < coordStart.first) {
                coordStart = coordDestination.also { coordDestination = coordStart }
            }

            val path = Path()
            path.moveTo(coordStart.first, coordStart.second)
            path.lineTo(coordDestination.first, coordDestination.second)
            Canvas(bitmap).drawPath(path, paint)

            paths.add(Pair(path, it.cost))
        }
        this.setEdgesText(paths, paint, bitmap)

        return bitmap
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

    /**
     * Converting dp to px
     * @param[dpValue] value in dp
     * @return value in px
     */
    private fun convertDpToPx(dpValue: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, viewModelOwner.resources.displayMetrics)
    }

    private val viewModelOwner = application

    var isStopped = true

    private val townButtonListener = View.OnClickListener {
        val pair = ldStartAndDestination
        when {
            (it as TownButton).isStart -> {
                it.isStart = false
                pair.value = Pair(0, pair.value!!.second)
                it.setBackgroundResource(R.drawable.town_button)
            }
            it.isDestination -> {
                it.isDestination = false
                pair.value = Pair(pair.value!!.first, 0)
                it.setBackgroundResource(R.drawable.town_button)
            }
            pair.value!!.first == 0.toShort() -> {
                it.isStart = true
                pair.value = Pair(it.townId, pair.value!!.second)
                it.setBackgroundResource(R.drawable.start_town_button)
            }
            pair.value!!.second == 0.toShort() -> {
                it.isDestination = true
                pair.value = Pair(pair.value!!.first, it.townId)
                it.setBackgroundResource(R.drawable.destination_town_button)
            }
        }
    }

    private val ldStartAndDestination = MutableLiveData(Pair<Short, Short>(0, 0))
    val startAndDestination: LiveData<Pair<Short, Short>> = ldStartAndDestination

    private val ldGraph = MutableLiveData<Pair<Map<Short, TownButton>, Bitmap>>()
    val graph: LiveData<Pair<Map<Short, TownButton>, Bitmap>> = this.ldGraph

    private val ldTownsAndRoads = MutableLiveData<Pair<Map<Short, String>, Collection<Road>>>()
    val townsAndRoads: LiveData<Pair<Map<Short, String>, Collection<Road>>> = this.ldTownsAndRoads

    private val ldCountries = MutableLiveData<Map<Short, String>>()
    val countries: LiveData<Map<Short, String>> = this.ldCountries

    private val hostDb = "188.225.75.231"
    private val portDb = 8888
}
