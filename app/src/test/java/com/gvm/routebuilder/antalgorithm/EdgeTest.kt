package com.gvm.routebuilder.antalgorithm

import junit.framework.TestCase
import org.junit.Test

class EdgeTest : TestCase() {
    @Test
    fun testEquals() {
        val a = Edge(0, 1, 2)
        val b = Edge(1, 0, 2)
        assertTrue(a == b)
        val c = Edge(0, 1, 3)
        assertFalse(a == c)
        val d = Edge(1, 0, 3)
        assertFalse(a == d)
        val e = Edge(0, 2, 2)
        assertFalse(a == e)
    }

    @Test
    fun testHashCode() {
        val a = Edge(0, 1, 2)
        val b = Edge(1, 0, 2)
        assertTrue(a.hashCode() == b.hashCode())
        val c = Edge(0, 1, 3)
        assertFalse(a.hashCode() == c.hashCode())
        val d = Edge(1, 0, 3)
        assertFalse(a.hashCode() == d.hashCode())
        val e = Edge(0, 2, 2)
        assertFalse(a.hashCode() == e.hashCode())
    }
}