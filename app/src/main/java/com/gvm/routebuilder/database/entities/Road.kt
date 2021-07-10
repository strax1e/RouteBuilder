package com.gvm.routebuilder.database.entities

import com.gvm.routebuilder.antalgorithm.Edge

/**
 * Base class of road.
 * @param[townA] first node, that connected by this edge
 * @param[townB] second node, that connected by this edge
 * @param[distance] the value this edge contains
 * @property[country] Country of road
 */
class Road(val country: Short, townA: Short, townB: Short, distance: Short) : Edge(townA, townB, distance)