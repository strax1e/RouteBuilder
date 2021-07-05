package com.gvm.routebuilder.antalgorithm

/**
 * Base class of edge.
 * @param[nodes] nodes that connected by this edge
 * @param[cost] the value that this edge contains
 */
open class Edge(val nodes: Pair<Short, Short>, val cost: Short)