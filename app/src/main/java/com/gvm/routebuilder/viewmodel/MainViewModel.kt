package com.gvm.routebuilder.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.Button
import android.widget.ImageButton
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gvm.routebuilder.R
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
        busyXY: MutableSet<Pair<Float, Float>>,
        minXY: Pair<Int, Int>,
        maxXY: Pair<Int, Int>
    ): ImageButton {
        var coordinates: Pair<Float, Float>
        do {
            coordinates =
                Pair(
                    minXY.first + Random.nextFloat() * (maxXY.first - minXY.first - 24),
                    minXY.second + Random.nextFloat() * (maxXY.second - minXY.second - 24)
                )
        } while (busyXY.contains(coordinates))
        busyXY.add(coordinates)
        val button = ImageButton(getApplication())
        button.apply {
            visibility = Button.VISIBLE
            x = coordinates.first
            y = coordinates.second
            setBackgroundResource(R.drawable.roundedbutton)
        }
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
            color = Color.WHITE
            strokeWidth = 6f
            isAntiAlias = true
        }

        townsAndRoads.value!!.second.forEach {
            val buttonA = nodes[it.nodeA]
            val buttonB = nodes[it.nodeB]
            val offset = 15f
            Canvas(bitmap).drawLine(
                buttonA!!.x + offset, buttonA.y + offset,
                buttonB!!.x + offset, buttonB.y + offset,
                paint
            )
        }
        return bitmap
    }

    var isStopped = true

    private val ldGraph = MutableLiveData<Pair<Map<Short, ImageButton>, Bitmap>>()
    val graph: LiveData<Pair<Map<Short, ImageButton>, Bitmap>> = this.ldGraph

    private val ldTownsAndRoads = MutableLiveData<Pair<Map<Short, String>, Collection<Road>>>()
    val townsAndRoads: LiveData<Pair<Map<Short, String>, Collection<Road>>> = this.ldTownsAndRoads

    private val ldCountries = MutableLiveData<Map<Short, String>>()
    val countries: LiveData<Map<Short, String>> = this.ldCountries

    private val hostDb = "188.225.75.231"
    private val portDb = 8888
}
