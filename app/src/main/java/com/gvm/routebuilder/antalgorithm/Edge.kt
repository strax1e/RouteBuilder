package com.gvm.routebuilder.antalgorithm

/**
 * Base class of edge.
 * @param[nodeA] first node, that connected by this edge
 * @param[nodeB] second node, that connected by this edge
 * @param[cost] the value this edge contains
 */
open class Edge(val nodeA: Short, val nodeB: Short, val cost: Short) {

    /**
     * Compares, taking into account the equality of paths A -> B and B -> A
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Edge) return false
        val isNodesEquals = (this.nodeA == other.nodeA && this.nodeB == other.nodeB) ||
                (this.nodeA == other.nodeB && this.nodeB == other.nodeA)
        return isNodesEquals && this.cost == other.cost
    }

    /**
     * Generates hash code, taking into account the equality of paths A -> B and B -> A
     */
    override fun hashCode(): Int {
        var result = (this.nodeA + 1) * (this.nodeB + 1)
        result = 31 * result + this.cost
        return result
    }
}