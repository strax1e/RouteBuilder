package com.gvm.routebuilder.database

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gvm.routebuilder.database.entities.Road
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/**
 * Class of database server connection
 * @param[host] server ip address
 * @param[port] server port
 */
class DbConnection(host: String, port: Int) : AutoCloseable {

    /**
     * Gets map (id:name) of countries from db server
     * @return required data: map of id and name of countries
     */
    fun getCountries(): Map<Short, String> {
        writer.println("get countries")
        return this.jsonMapper.readValue<HashMap<Short, String>>(reader.readLine())
    }

    /**
     * Gets the roads of the selected country from db server
     * @param[countryId] id of country whose roads are required
     * @return required data: collection of roads
     */
    fun getRoads(countryId: Short): Collection<Road> {
        writer.println("get roads $countryId")
        return this.jsonMapper.readValue<ArrayList<Road>>(reader.readLine())
    }

    /**
     * Gets the towns of the selected country from db server
     * @param[countryId] id of country whose towns are required
     * @return required data: map of id and name of towns
     */
    fun getTowns(countryId: Short): Map<Short, String> {
        writer.println("get towns $countryId")
        return this.jsonMapper.readValue<HashMap<Short, String>>(reader.readLine())
    }

    /**
     * Closes connection
     */
    override fun close() {
        writer.use { it.println("finish") }
        this.writer.close()
        this.reader.close()
        this.socket.close()
    }

    private val socket by lazy { Socket(host, port) }
    private val writer by lazy { PrintWriter(socket.getOutputStream(), true) }
    private val reader by lazy { BufferedReader(InputStreamReader(socket.getInputStream())) }
    private val jsonMapper = jacksonObjectMapper()
}
