package com.gvm.routebuilder

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gvm.routebuilder.database.entities.Road
import com.gvm.routebuilder.viewmodel.MainViewModel

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

        this.mainVM.countries.observe(this, this.countriesObserver)
        this.mainVM.townsAndRoads.observe(this, this.townsRoadsObserver)
        this.mainVM.graph.observe(this, this.graphObserver)

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
                this.mainVM.countries.value!!.values.toList()
        val newAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, countries)
        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = newAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (spinner.selectedItem != resources.getString(R.string.noneText)) {
                    val countryId = mainVM.getCountryId(spinner.selectedItem as String)
                    centerTextView.visibility = TextView.INVISIBLE
                    progressBar.visibility = ProgressBar.VISIBLE
                    mainVM.loadTownsAndRoads(countryId) // getting towns and roads from db
                } else {
                    if (!mainVM.graph.value?.first.isNullOrEmpty()) {
                        removeGraph()
                    }
                    centerTextView.visibility = TextView.VISIBLE
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

    /**
     * Removes graph from display
     */
    private fun removeGraph() {
        for (button in mainVM.graph.value!!.first.values) {
            this.constraintLayout.removeView(button)
        }
        this.imageView.setImageBitmap(
            Bitmap.createBitmap(
                this.imageView.width,
                this.imageView.height,
                Bitmap.Config.ARGB_8888
            )
        )
    }

    /**
     * Displays graph
     */
    private fun renderGraph() {
        this.imageView.setImageBitmap(mainVM.graph.value!!.second)

        val lp = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        for (button in mainVM.graph.value!!.first.values) {
            this.constraintLayout.addView(button, lp)
        }
    }

    // MainViewModel declaration
    private val mainVM by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    private val townsRoadsObserver = Observer<Pair<Map<Short, String>, Collection<Road>>> {
        val offset = this.mainVM.convertDpToPx(22f).toInt()
        this.mainVM.createGraph(
            Pair(0, 0),
            Pair(this.imageView.width, this.imageView.height)
        )
    }

    private val countriesObserver = Observer<Map<Short, String>> {
        this.progressBar.visibility = ProgressBar.INVISIBLE
        this.centerTextView.visibility = TextView.VISIBLE
        this.configureSpinner()
    }

    private val graphObserver = Observer<Pair<Map<Short, ImageButton>, Bitmap>> {
        this.progressBar.visibility = ProgressBar.INVISIBLE
        this.renderGraph()
    }

    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progressBar) }
    private val constraintLayout by lazy { findViewById<ConstraintLayout>(R.id.field) }
    private val imageView by lazy { findViewById<ImageView>(R.id.image) }
    private val centerTextView by lazy { findViewById<TextView>(R.id.centerTextView) }
    private val nextStepButton by lazy { findViewById<Button>(R.id.nextStepButton) }
    private val prevStepButton by lazy { findViewById<Button>(R.id.previousStepButton) }
    private val startButton by lazy { findViewById<Button>(R.id.startButton) }
    private val selectCountrySpinner by lazy { findViewById<Spinner>(R.id.selectCountrySpinner) }
}
