package com.gvm.routebuilder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gvm.routebuilder.database.entities.Road
import com.gvm.routebuilder.viewmodel.MainViewModel
import kotlin.random.Random

/**
 * MainActivity class makes the main activity of the application work
 */
class MainActivity : AppCompatActivity() {

    /**
     * Start point of android application
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.selectCountrySpinner.isEnabled = false

        this.mainVM.ldCountries.observe(this, this.countriesObserver)
        this.mainVM.ldTownsAndRoads.observe(this, this.townAndRoadsObserver)

        this.mainVM.loadCountries() // getting countries from db
    }

    /**
     * Actions that occur when the focus of the window is changed
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            hideSystemUI()
    }

    /**
     * Hide navigation bar
     */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    /**
     * This is the action that is called when the "previous step" button is pressed
     */
    fun onClickPreviousStepButton(view: View) {
//        TODO
    }

    /**
     * This is the action that is called when the "next step" button is pressed
     */
    fun onClickNextStepButton(view: View) {
//        TODO
    }

    /**
     * This is the action that is called when the "Start/Stop" button is pressed
     */
    fun onClickStartButton(view: View) {
        this.switchStopStartButton(!this.mainVM.isStopped)
        this.mainVM.isStopped = !this.mainVM.isStopped
        this.selectCountrySpinner.isEnabled = this.mainVM.isStopped
        if (this.mainVM.isStopped) {
            this.centerTextView.text = resources.getString(R.string.selectCountryText)
        }
    }

    /**
     * Configures the "Select country" spinner and it's listener
     */
    private fun configureSpinner() {
        val context = this
        val spinner = this.selectCountrySpinner

        val countries = listOf(resources.getString(R.string.noneText)) +
                this.mainVM.ldCountries.value!!.values.toList()
        val newAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, countries)
        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = newAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (spinner.selectedItem != resources.getString(R.string.noneText)) {
                    val countryId = mainVM.getCountryId(spinner.selectedItem as String)
                    mainVM.loadTownsAndRoads(countryId) // getting towns and roads from db
                } else {
                    startButton.isEnabled = false
                    mainVM.isStopped = true
                    switchStopStartButton(true)
                    centerTextView.text = resources.getString(R.string.selectCountryText) // "Select a country"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                centerTextView.text = resources.getString(R.string.notSelectedText)
            }
        }
        spinner.isEnabled = true
    }

    /**
     * Switches the state of "next" and "prev" buttons. (Enables or disables both buttons)
     * @param[newState] new state of button
     */
    private fun changeStepButtonsState(newState: Boolean) {
        this.prevStepButton.isEnabled = newState
        this.nextStepButton.isEnabled = newState
    }

    /**
     * Switches the state of start/stop button ("Start" or "Stop").
     * @param[isStart] new state of button
     */
    private fun switchStopStartButton(isStart: Boolean) {
        startButton.text = when (isStart) {
            true -> resources.getString(R.string.startText)
            else -> resources.getString(R.string.stopText)
        }
        this.changeStepButtonsState(!isStart)
    }

    private val mapIdToImageButton = HashMap<Short, ImageButton>()

    private fun createNodes() {
        val setOfPairXY = HashSet<Pair<Float, Float>>()
        mainVM.ldTownsAndRoads.value!!.second.forEach {

            val setAttrsButton = fun(button: ImageButton, coordinates: Pair<Float, Float>) {
                button.apply {
                    visibility = Button.VISIBLE
                    x = coordinates.first
                    y = coordinates.second
                    setBackgroundResource(R.drawable.roundedbutton)
                }
            }
            var imageView = findViewById<ImageView>(R.id.image)
            val createButton = fun(node: Short) {
                var coordinates = Pair<Float, Float>(0f, 0f)
                if (!mapIdToImageButton.contains(node)) {
                    do {
                        coordinates = Pair(Random.nextFloat() * imageView.width, Random.nextFloat() * imageView.height)
                    } while (setOfPairXY.contains(coordinates))
                    setOfPairXY.add(coordinates)
                    val button = ImageButton(this)
                    setAttrsButton(button, coordinates)
                    mapIdToImageButton[node] = button
                }
            }
            createButton(it.nodeA)
            createButton(it.nodeB)
        }
    }

    fun deleteNodes(buttons: Collection<ImageButton>) {
        val layout = findViewById<ConstraintLayout>(R.id.field)
        for (button in mapIdToImageButton.values) {
            layout.removeView(button)
        }
    }

    private fun renderNodes() {
        val layout = findViewById<ConstraintLayout>(R.id.field)
        val lp = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        for (button in mapIdToImageButton.values) {
            layout.addView(button, lp)
        }
    }

    private fun renderEdges() {
        var imageView = findViewById<ImageView>(R.id.image)
        val paint = Paint()
        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        paint.apply {
            color = Color.WHITE
            strokeWidth = 10f
        }

        mainVM.ldTownsAndRoads.value!!.second.forEach {
            val buttonA = mapIdToImageButton[it.nodeA]
            val buttonB = mapIdToImageButton[it.nodeB]
            canvas.drawLine(buttonA!!.x, buttonA.y, buttonB!!.x, buttonB.y, paint)
        }

        imageView.setImageBitmap(bitmap)
    }

    // MainViewModel declaration
    private val mainVM by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    private val townAndRoadsObserver = Observer<Pair<Map<Short, String>, Collection<Road>>> {
        createNodes()
        renderEdges()
        renderNodes()
        switchStopStartButton(true)
        startButton.isEnabled = true
    }

    private val countriesObserver = Observer<Map<Short, String>> {
        this.configureSpinner()
    }

    private val centerTextView by lazy { findViewById<TextView>(R.id.centerTextView) }
    private val nextStepButton by lazy { findViewById<Button>(R.id.nextStepButton) }
    private val prevStepButton by lazy { findViewById<Button>(R.id.previousStepButton) }
    private val startButton by lazy { findViewById<Button>(R.id.startButton) }
    private val selectCountrySpinner by lazy { findViewById<Spinner>(R.id.selectCountrySpinner) }
}
