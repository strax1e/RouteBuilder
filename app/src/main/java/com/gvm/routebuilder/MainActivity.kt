package com.gvm.routebuilder

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

        findViewById<Spinner>(R.id.selectCountrySpinner).isEnabled = false

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
        val textView = findViewById<TextView>(R.id.centerTextView)
        textView.text = "Previous step"
    }

    /**
     * This is the action that is called when the "next step" button is pressed
     */
    fun onClickNextStepButton(view: View) {
        val textView = findViewById<TextView>(R.id.centerTextView)
        textView.text = "Next step"
    }

    /**
     * This is the action that is called when the "Start/Stop" button is pressed
     */
    fun onClickStartButton(view: View) {
        this.switchStopStartButton(!this.mainVM.isStopped, view as Button)
        this.mainVM.isStopped = !this.mainVM.isStopped
        findViewById<Spinner>(R.id.selectCountrySpinner).isEnabled = this.mainVM.isStopped
        findViewById<TextView>(R.id.centerTextView).text = when (this.mainVM.isStopped) {
            true -> "Select a country"
            else -> "Process"
        }
    }

    /**
     * Configures the "Select country" spinner and it's listener
     */
    private fun configureSpinner() {
        val context = this
        val spinner = findViewById<Spinner>(R.id.selectCountrySpinner)

        val countries = listOf("None") + this.mainVM.ldCountries.value!!.values.toList()
        val newAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, countries)
        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = newAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (spinner.selectedItem != "None") {
                    val countryId = mainVM.getCountryId(spinner.selectedItem as String)
                    mainVM.loadTownsAndRoads(countryId) // getting towns and roads from db
                } else {
                    val startButton = findViewById<Button>(R.id.startButton)
                    startButton.isEnabled = false
                    mainVM.isStopped = true
                    switchStopStartButton(true, startButton)
                    findViewById<TextView>(R.id.centerTextView).text = "Select a country"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                findViewById<TextView>(R.id.centerTextView).text = "Not selected"
            }
        }
        spinner.isEnabled = true
    }

    /**
     * Switches the state of "next" and "prev" buttons. (Enables or disables both buttons)
     * @param[newState] new state of button
     */
    private fun changeStepButtonsState(newState: Boolean) {
        findViewById<Button>(R.id.previousStepButton).isEnabled = newState
        findViewById<Button>(R.id.nextStepButton).isEnabled = newState
    }

    /**
     * Switches the state of start/stop button ("Start" or "Stop").
     * @param[isStart] new state of button
     */
    private fun switchStopStartButton(isStart: Boolean, startButton: Button = findViewById(R.id.startButton)) {
        startButton.text = if (isStart) "Start" else "Stop"
        this.changeStepButtonsState(!isStart)
    }

    // MainViewModel declaration
    private val mainVM by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    private val townAndRoadsObserver = Observer<Pair<Map<Short, String>, Collection<Road>>> {
        switchStopStartButton(true)
        findViewById<Button>(R.id.startButton).isEnabled = true

        // TODO debug code
        findViewById<TextView>(R.id.centerTextView).text = this.mainVM.ldTownsAndRoads.value!!.first.toString()
    }

    private val countriesObserver = Observer<Map<Short, String>> {
        this.configureSpinner()
    }
}
