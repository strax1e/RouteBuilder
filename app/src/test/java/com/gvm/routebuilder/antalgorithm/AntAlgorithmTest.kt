package com.gvm.routebuilder.antalgorithm

import junit.framework.TestCase
import org.junit.Test


class AntAlgorithmTest : TestCase() {
    private val edges = listOf(
        Edge(1, 2, 5),
        Edge(1, 3, 2),
        Edge(2, 3, 4),
        Edge(2, 4, 3),
        Edge(3, 4, 6),
        Edge(0, 3, 20),
        Edge(0, 4, 10),
        Edge(5, 6, 1),
        Edge(6, 7, 2)
    )

    @Test
    fun testStartingPoint() {
        var path = getPathAndStates(1, 0, edges).first
        assertNotNull(path)
        var startPoint = path!!.first().first
        assertEquals(1, startPoint)

        path = getPathAndStates(2, 3, edges).first
        assertNotNull(path)
        startPoint = path!!.first().first
        assertEquals(2, startPoint)

        path = getPathAndStates(1, 0, edges).first
        assertNotNull(path)
        startPoint = path!!.first().first
        assertEquals(1, startPoint)
    }

    @Test
    fun testEndingPoint() {
        var path = getPathAndStates(1, 0, edges).first
        assertNotNull(path)
        var endPoint = path!!.last().second
        assertEquals(0, endPoint)

        path = getPathAndStates(2, 3, edges).first
        assertNotNull(path)
        endPoint = path!!.last().second
        assertEquals(3, endPoint)

        path = getPathAndStates(1, 0, edges).first
        assertNotNull(path)
        endPoint = path!!.last().second
        assertEquals(0, endPoint)
    }

    @Test
    fun testWayNotExists() {
        var path = getPathAndStates(1, 5, edges).first
        assertNull(path)

        path = getPathAndStates(3, 7, edges).first
        assertNull(path)

        path = getPathAndStates(6, 4, edges).first
        assertNull(path)
    }

    @Test
    fun testPathCorrect() {
        val adjList = Array<HashMap<Short, Short>>(8) { hashMapOf() }
        edges.forEach {
            adjList[it.nodeA.toInt()][it.nodeB] = it.cost
            adjList[it.nodeB.toInt()][it.nodeA] = it.cost
        }

        fun checkPathCorrect(path: Collection<Pair<Short, Short>>?) {
            requireNotNull(path)
            var curPos = path.first().first
            for (i in path) {
                assert(i.first == curPos)
                assert(i.first < adjList.size)
                assertNotNull(adjList[i.first.toInt()][i.second])
                curPos = i.second
            }
        }

        var path = getPathAndStates(0, 2, edges).first
        checkPathCorrect(path)

        path = getPathAndStates(1, 4, edges).first
        checkPathCorrect(path)

        path = getPathAndStates(5, 7, edges).first
        checkPathCorrect(path)
    }
}