package com.gvm.routebuilder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
    }

    override fun onResume() {
        super.onResume()
        val townsJson = intent.extras!!.getString("towns")!!
        if (townsJson != getString(R.string.noneText)) {
            val newTowns = this.jsonMapper.readValue<HashMap<Short, String>?>(townsJson)
            this.listHeadTextView.visibility = TextView.VISIBLE
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            for (town in newTowns!!.toList()) {
                val textString = town.first.toString() + " - " + town.second
                val textView = TextView(this).apply {
                    text = textString
                    textSize = convertSpToPx(8f)
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                this.list.addView(textView, lp)
            }
        } else {
            this.listHeadTextView.visibility = TextView.INVISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        this.list.removeAllViews()
    }

    /**
     * Converting sp to px
     * @param[spValue] value in sp
     * @return value in px
     */
    private fun convertSpToPx(spValue: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, this.resources.displayMetrics)
    }

    private val list by lazy { findViewById<LinearLayout>(R.id.listOfTowns) }
    private val listHeadTextView by lazy { findViewById<TextView>(R.id.listHeadTextView) }
    private val jsonMapper = jacksonObjectMapper()
}