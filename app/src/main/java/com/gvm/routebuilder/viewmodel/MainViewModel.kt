package com.gvm.routebuilder.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gvm.routebuilder.R
import com.gvm.routebuilder.TownButton
import com.gvm.routebuilder.antalgorithm.getPathAndStates
import com.gvm.routebuilder.database.DbConnection
import com.gvm.routebuilder.database.entities.Road
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
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

    fun getJsonTowns(): String {
        return jsonMapper.writeValueAsString(this.townsAndRoads.value?.first)
    }

    /**
     * Starts routing
     */
    fun route(minXY: Pair<Int, Int>, maxXY: Pair<Int, Int>) {
        CoroutineScope(Dispatchers.Default).launch {
            val res = getPathAndStates(
                startAndDestination.value!!.first,
                startAndDestination.value!!.second,
                townsAndRoads.value!!.second
            )
            val width = maxXY.first - minXY.first
            val height = maxXY.second - minXY.second

            val states = ArrayList<Bitmap>()
            res.second.forEach {
                states.add(painter.createPheromonesEdges(it, townButtons, width, height))
            }
            ldPathAndStates.postValue(
                Pair(
                    painter.createEdgesWithPath(
                        res.first, townsAndRoads.value!!.second,
                        townButtons, width, height
                    ), states
                )
            )
        }
    }

    /**
     * Creates a graph for loaded roads. Invoke only when the last one was loaded
     * @param[minXY] minimal coordinates
     * @param[maxXY] maximum coordinates
     */
    fun createGraph(minXY: Pair<Int, Int>, maxXY: Pair<Int, Int>) {
        CoroutineScope(Dispatchers.Default).launch {
            townButtons = createNodes(minXY, maxXY)
            ldStartAndDestination.postValue(Pair(0, 0))
            val width = maxXY.first - minXY.first
            val height = maxXY.second - minXY.second
            val edges = EdgesPainter.createRawEdges(townsAndRoads.value!!.second, townButtons, width, height)
            ldEdgesToUpdate.postValue(edges)
            ldGraph.postValue(Pair(townButtons, edges))
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
        val radiusButton = convertDpToPx(10.5f)
        var regenerate: Boolean
        do {
            regenerate = false
            coordinates =
                Pair(
                    minXY.first + Random.nextFloat() * (maxXY.first - minXY.first - convertDpToPx(23f)),
                    minXY.second + Random.nextFloat() * (maxXY.second - minXY.second - convertDpToPx(23f))
                )
            busyXY.forEach {
                val d = (abs(it.first - coordinates.first)
                    .pow(2.0f) + abs(it.second - coordinates.second)
                    .pow(2.0f)).pow(0.5f)
                if (d < 6 * radiusButton) {
                    regenerate = true
                }
            }
        } while (regenerate)
        busyXY.add(coordinates)
        val button = TownButton(townId, viewModelOwner).apply {
            text = townId.toString()
            visibility = Button.VISIBLE
            x = coordinates.first
            y = coordinates.second
            setBackgroundResource(R.drawable.town_button)
            gravity = Gravity.CENTER_HORIZONTAL
            textSize = convertDpToPx(4.75f)
        }
        button.setOnTouchListener(this.onTouchListener)
        button.setOnClickListener(this.townButtonListener)
        return button
    }

    /**
     * Creates a nodes for loaded roads.
     * @param[minXY] minimal coordinates
     * @param[maxXY] maximum coordinates
     * @return nodes
     */
    private fun createNodes(minXY: Pair<Int, Int>, maxXY: Pair<Int, Int>): Map<Short, TownButton> {
        val newNodes = HashMap<Short, TownButton>()
        val coordinatesSet = HashSet<Pair<Float, Float>>()
        townsAndRoads.value!!.second.forEach {
            if (!newNodes.contains(it.nodeA)) {
                newNodes[it.nodeA] = createButton(it.nodeA, coordinatesSet, minXY, maxXY)
            }
            if (!newNodes.contains(it.nodeB)) {
                newNodes[it.nodeB] = createButton(it.nodeB, coordinatesSet, minXY, maxXY)
            }
        }
        return newNodes
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
        if (this.isStopped) {
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
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { view: View, event: MotionEvent ->
        if (event.action == MotionEvent.ACTION_MOVE) {
            val barrier = convertDpToPx(23f)
            if (this.isStopped) {
                if (event.rawX <= (this.imageWidth - barrier)) {
                    view.x = event.rawX
                }
                if (event.rawY <= (this.imageHeight - barrier)) {
                    view.y = event.rawY
                }
                val bitmap = this.painter.createRawEdges(
                    this.townsAndRoads.value!!.second,
                    this.townButtons,
                    this.imageWidth,
                    this.imageHeight
                )
                this.ldEdgesToUpdate.value = bitmap
            }
        }
        false
    }

    var indexOfStates = 0

    private val ldStartAndDestination = MutableLiveData(Pair<Short, Short>(0, 0))
    val startAndDestination: LiveData<Pair<Short, Short>> = ldStartAndDestination

    private val ldGraph = MutableLiveData<Pair<Map<Short, TownButton>, Bitmap>>()
    val graph: LiveData<Pair<Map<Short, TownButton>, Bitmap>> = this.ldGraph

    private val ldEdgesToUpdate = MutableLiveData<Bitmap>()
    val edgesToUpdate: LiveData<Bitmap> = this.ldEdgesToUpdate

    private val ldTownsAndRoads = MutableLiveData<Pair<Map<Short, String>, Collection<Road>>>()
    val townsAndRoads: LiveData<Pair<Map<Short, String>, Collection<Road>>> = this.ldTownsAndRoads

    private val ldPathAndStates = MutableLiveData<Pair<Bitmap, Collection<Bitmap>>>()
    val pathAndEdgesState: LiveData<Pair<Bitmap, Collection<Bitmap>>> = this.ldPathAndStates

    private val ldCountries = MutableLiveData<Map<Short, String>>()
    val countries: LiveData<Map<Short, String>> = this.ldCountries

    private lateinit var townButtons: Map<Short, TownButton>
    private val painter = EdgesPainter
    private val jsonMapper = jacksonObjectMapper()
    private val hostDb = "188.225.75.231"
    private val portDb = 8888

    var imageWidth: Int = 1
    var imageHeight: Int = 1

    init {
        this.painter.convertDpToPx = ::convertDpToPx
    }
}
