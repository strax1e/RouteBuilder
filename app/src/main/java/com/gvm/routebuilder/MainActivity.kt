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

    // MainViewModel declaration
    private val mainVM by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    private val townAndRoadsObserver = Observer<Pair<Map<Short, String>, Collection<Road>>> {
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
