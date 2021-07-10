package com.gvm.routebuilder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gvm.routebuilder.database.DbConnection
import com.gvm.routebuilder.database.entities.Road
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                _ldTownsAndRoads.postValue(Pair(it.getTowns(countryId), it.getRoads(countryId)))
            }
        }
    }

    /**
     * Loads towns and roads from db server
     */
    fun loadCountries() {
        CoroutineScope(Dispatchers.IO).launch {
            DbConnection(hostDb, portDb).use {
                _ldCountries.postValue(it.getCountries())
            }
        }
    }

    /**
     * Gets country ID by its name from country map
     * @param[countryName] name of country
     * @return country ID
     */
    fun getCountryId(countryName: String): Short {
        if (countryName != this.selectedCountry.second) {
            this.selectedCountry = this.ldCountries.value!!.toList().find { it.second == countryName }!!
        }
        return this.selectedCountry.first
    }

    var selectedCountry: Pair<Short, String> = Pair(0, "None")
    var isStopped = true

    private val _ldTownsAndRoads = MutableLiveData<Pair<Map<Short, String>, Collection<Road>>>()
    val ldTownsAndRoads: LiveData<Pair<Map<Short, String>, Collection<Road>>> = this._ldTownsAndRoads

    private val _ldCountries = MutableLiveData<Map<Short, String>>()
    val ldCountries: LiveData<Map<Short, String>> = this._ldCountries

    private val hostDb = "188.225.75.231"
    private val portDb = 8888
}
