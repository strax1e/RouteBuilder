package com.gvm.routebuilder.antalgorithm

import java.util.HashMap
import java.util.HashSet
import java.util.Vector
import kotlin.math.pow
import kotlin.random.Random

//constants
private object Constants {
    const val startingPheromones = 0.1f
    const val pheromonesResidue = 0.64f
    const val pheromonesInference = 1
    const val distanceInfluence = 1
    const val iterationAmount = 10
    const val antAmount = 50
    const val pheromonesDistCf = 2f
    const val antRankRaise = 0.8f
}

/**
 * @param[start] starting point
 * @param[destination] ending point
 * @param[edges] Collection of Edges in the graph
 * @return Path and pheromones quantity in 10 even iterations
 */
fun getPathAndStates(
    start: Short,
    destination: Short,
    edges: Collection<Edge>
): Pair<Collection<Pair<Short, Short>>?, Collection<Collection<Triple<Short, Short, Float>>>> {
    val verticesAmount = getVerticesAmount(edges)

    // adjacencyList[nodeA][nodeB] = Pair<pheromones, 1/dist>
    val adjacencyList = adjacencyListOfEdges(edges, verticesAmount)
    val addingPheromones = Array<HashMap<Short, Float>>(verticesAmount) { hashMapOf() }
    val states = mutableListOf(getState(adjacencyList))
    for (iterationNumber in 0 until Constants.iterationAmount) {
        antsLoop@ for (antNumber in 0 until Constants.antAmount) {
            val visited = hashSetOf(start) // nodes, visited by the ant
            val curAntPath = mutableListOf(start) // current ant's path.
            var distance = 0f // summary distance of the way
            var curPos = start
            while (curAntPath.last() != destination) {
                val nextPos = getNextStep(curPos, visited, adjacencyList) ?: continue@antsLoop
                distance += 1 / adjacencyList[curPos.toInt()][nextPos]!!.second
                visited.add(nextPos)
                curAntPath.add(nextPos)
                curPos = nextPos
            }
            addPheromones(addingPheromones, curAntPath, distance)
        }
        updatePheromones(adjacencyList, addingPheromones)
        states.add(getState(adjacencyList))
    }
    val bestPath = getBestPath(adjacencyList, start, destination)
    return Pair(edgeListOfPath(bestPath), states)
}

/**
 * Creates adjacencyList of given collection of Edges and verticesAmount
 * @param[edges] Collection of unoriented edges
 * @param[verticesAmount] Amount of vertices in Graph
 * @return adjacencyList, which represents the Graph, given in Collection<Edge>
 */
private fun adjacencyListOfEdges(
    edges: Collection<Edge>,
    verticesAmount: Int
): Array<HashMap<Short, Pair<Float, Float>>> {
    val adjacencyList = Array<HashMap<Short, Pair<Float, Float>>>(verticesAmount) { hashMapOf() }
    edges.forEach {
        val dist = 1f / it.cost
        adjacencyList[it.nodeA.toInt()][it.nodeB] = Pair(Constants.startingPheromones, dist)
        adjacencyList[it.nodeB.toInt()][it.nodeA] = Pair(Constants.startingPheromones, dist)
    }
    return adjacencyList
}

/**
 * Used to get ant's next move based on nearness, pheromones amount and random
 * @param[curPos] current ant position
 * @param[visited] nodes, visited by the ant
 * @return node, which the ant wanted to go
 */
private fun getNextStep(
    curPos: Short,
    visited: HashSet<Short>,
    adjacencyList: Array<HashMap<Short, Pair<Float, Float>>>
): Short? {
    val possibleWays = getPossibleWays(adjacencyList[curPos.toInt()], visited)
    if (possibleWays.size == 0) return null
    var delimiter = 0f // sum of ant's desire to go all possible ways
    possibleWays.forEach {
        delimiter += it.second.pow(Constants.pheromonesInference) * it.third.pow(Constants.distanceInfluence)
    }
    if (delimiter == 0f) return null
    val multiplier = 1 / delimiter // using multiplier instead of delimiter, because it's faster
    var curChance = 0f
    val chances = Vector<Pair<Short, Float>>(possibleWays.size) // Vector<Pair<NodeB, desire(chance)>
    possibleWays.forEach {
        curChance += it.second.pow(Constants.pheromonesInference) * it.third.pow(Constants.distanceInfluence)
        chances.addElement(Pair(it.first, curChance * multiplier))
    }
    chances[chances.size - 1] = Pair(chances[chances.size - 1].first, 1.1f) // 1.1f - to get rid of inaccuracies
    val randomFloat = Random.nextFloat()
    for (i in chances)
        if (randomFloat < i.second)
            return i.first
    return null
}

/**
 * Used to get vertices number based on given Collection of Edges
 * @param[edges] Collection of Edges in given graph
 * @return amount of Vertices in Graph
 */
private fun getVerticesAmount(edges: Collection<Edge>): Int {
    var number: Short = -1
    edges.forEach {
        number = maxOf(number, it.nodeA, it.nodeB)
    }
    return number + 1
}

/**
 * Used to get admissible ways out of all cur node's edges (without getting in visited nodes)
 * @param[ways] piece of adjacencyList. HashMap of cur node's edges.
 * @param[visited] HashSet of nodes, visited by the ant in this sally
 * @return Vector<Triple<NodeB, pheromones, 1/dist>> of available ways from nodeA
 */
private fun getPossibleWays(ways: HashMap<Short, Pair<Float, Float>>, visited: HashSet<Short>)
        : Vector<Triple<Short, Float, Float>> {
    val possibleWays = Vector<Triple<Short, Float, Float>>()
    ways.forEach {
        if (it.key !in visited)
            possibleWays.addElement(Triple(it.key, it.value.first, it.value.second))
    }
    return possibleWays
}

/**
 * Used to update pheromones on ways between iterations.
 * @param[adjacencyList] contains way data in form: adjList［nodeA］［nodeB］ = <pheromones, 1/dist>
 * @param[addingPheromones]
 */
private fun updatePheromones(
    adjacencyList: Array<HashMap<Short, Pair<Float, Float>>>,
    addingPheromones: Array<HashMap<Short, Float>>
): Unit {
    for (i in adjacencyList.indices) {
        for (j in adjacencyList[i].keys) {
            val oldV = adjacencyList[i][j]
            val newPheromones = Constants.pheromonesDistCf * (addingPheromones[i][j] ?: 0f)
            adjacencyList[i][j] = oldV!!.copy(oldV.first * Constants.pheromonesResidue + newPheromones)
        }
    }
}


/**
 * Adds current ant's pheromones to Array in iteration
 * @param[addingPheromones] delayed pheromones, which will be added after iteration
 * @param[path] Mutable List of nodes, used in found way
 * @param[distance] summary distance of found way
 */
private fun addPheromones(
    addingPheromones: Array<HashMap<Short, Float>>,
    path: MutableList<Short>,
    distance: Float
): Unit {
    val edgeList = edgeListOfPath(path) ?: return
    for (i in edgeList) {
        val addVal = (1 / distance).pow(Constants.antRankRaise)
        addingPheromones[i.first.toInt()][i.second] = addVal + (addingPheromones[i.first.toInt()][i.second] ?: 0f)
    }
}

/**
 * Translates MutableList of visited nodes to MutableList of visited edges (<nodeA, nodeB>)
 * @param[path] visited nodes
 * @return visited edges (<nodeA, nodeB>)
 */
private fun edgeListOfPath(path: MutableList<Short>?): MutableList<Pair<Short, Short>>? {
    path ?: return null
    val edgeList = mutableListOf<Pair<Short, Short>>()
    var curNode = path.first()
    for (i in 1 until path.size) {
        edgeList.add(Pair(curNode, path[i]))
        curNode = path[i]
    }
    return edgeList
}

/**
 * Dumps current iteration's information about pheromones on edges amount
 * @param[adjacencyList] contains way data in form: adjList［nodeA］［nodeB］ = <pheromones, 1/dist>
 * @return List<Triple<nodeA, nodeB, pheromones>> of current iteration
 */
private fun getState(adjacencyList: Array<HashMap<Short, Pair<Float, Float>>>): MutableList<Triple<Short, Short, Float>> {
    val state = mutableListOf<Triple<Short, Short, Float>>()
    for (i in adjacencyList.indices) {
        for (j in i + 1 until adjacencyList.size) {
            val pheromonesStraight = adjacencyList[i][j.toShort()]?.first ?: 0f
            val pheromonesInverted = adjacencyList[j][i.toShort()]?.first ?: 0f
            if (pheromonesInverted > pheromonesStraight && pheromonesInverted > 0f)
                state.add(Triple(j.toShort(), i.toShort(), pheromonesInverted))
            else if (pheromonesStraight > 0f)
                state.add(Triple(i.toShort(), j.toShort(), pheromonesStraight))

        }
    }
    return state
}

/**
 * Tries to build current iteration best path based on adjacencyList without random
 * @param[adjacencyList] contains way data in form: adjList［nodeA］［nodeB］ = <pheromones, 1/dist>
 * @param[start] starting point
 * @param[destination] ending point
 * @return list of used vertices if the path found, else null
 */
private fun getBestPath(
    adjacencyList: Array<HashMap<Short, Pair<Float, Float>>>,
    start: Short,
    destination: Short
): MutableList<Short>? {
    val visited = hashSetOf(start)
    var curPos = start
    val path = mutableListOf(start)
    while (path.last() != destination) {
        val possibleWays = getPossibleWays(adjacencyList[curPos.toInt()], visited)
        if (possibleWays.size == 0) return null
        var maxDesire = 0f
        var desiredNode: Short = 0
        for (i in possibleWays) {
            val curDesire = i.second.pow(Constants.pheromonesInference) * i.third.pow(Constants.distanceInfluence)
            if (curDesire > maxDesire) {
                maxDesire = curDesire
                desiredNode = i.first
            }
        }
        visited.add(desiredNode)
        path.add(desiredNode)
        curPos = desiredNode
    }
    return path
}