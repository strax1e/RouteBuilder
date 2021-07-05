package com.gvm.routebuilder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.ArrayAdapter

/**
 * MainActivity class makes the main activity of the application work
 * @property[isStopped] is a private property for controlling the state of the Start/Stop button
 */
class MainActivity : AppCompatActivity() {

    /**
     * Start point of android application
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.makeSpinnerListener()
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
        this.changeStepButtonsState(true)
        findViewById<Button>(R.id.startButton).text = if (this.isStopped) "Start" else "Stop"
        this.isStopped = !this.isStopped
        this.changeStepButtonsState(this.isStopped)
    }

    /**
     * Configures the "Select country" spinner listener
     */
    private fun makeSpinnerListener() {
        val spinner = findViewById<Spinner>(R.id.selectCountrySpinner)
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            arrayOf("SWITZERLAND", "KOSOVO", "LUXEMBOURG")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                findViewById<TextView>(R.id.centerTextView)
                    .text = findViewById<Spinner>(R.id.selectCountrySpinner).selectedItem as String
                changeStepButtonsState(false)
                findViewById<Button>(R.id.startButton).text = "Start"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                findViewById<TextView>(R.id.centerTextView).text = "Not selected"
            }
        }
    }

    /**
     * Switches the state of "next" and "prev" buttons. (Enables or disables both buttons)
     */
    private fun changeStepButtonsState(newState: Boolean) {
        findViewById<Button>(R.id.previousStepButton).isEnabled = newState
        findViewById<Button>(R.id.nextStepButton).isEnabled = newState
    }

    private var isStopped = false
}