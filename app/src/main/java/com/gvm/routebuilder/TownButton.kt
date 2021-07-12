package com.gvm.routebuilder

import android.content.Context
import androidx.appcompat.widget.AppCompatImageButton

class TownButton : AppCompatImageButton {
    constructor(townId: Short, context: Context) : super(context) {
        this.townId = townId
    }

    constructor(context: Context) : super(context) {
        this.townId = 0
    }

    var isStart = false
    var isDestination = false
    val townId: Short
}