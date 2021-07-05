package com.gvm.routebuilder.antalgorithm

/**
 * Base class of edge.
 * @param[nodes] nodes that connected by this edge
 * @param[cost] the value that this edge contains
 */
open class Edge(val nodes: Pair<Short, Short>, val cost: Short) {

    /**
     * Сompares, taking into account the equality of paths A -> B and B -> A
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) return false

        val otherAsEdge = other as Edge
        val isNodesEquals = this.nodes == otherAsEdge.nodes ||
                (this.nodes.first == otherAsEdge.nodes.second && this.nodes.second == otherAsEdge.nodes.first)

        return isNodesEquals && this.cost == otherAsEdge.cost
    }

    /**
     * Generates hash code, taking into account the equality of paths A -> B and B -> A
     */
    override fun hashCode(): Int {
        var result = nodes.first * nodes.second
        result = 31 * result + cost
        return result
    }
}