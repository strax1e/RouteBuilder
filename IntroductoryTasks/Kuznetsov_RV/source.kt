import java.util.Scanner
import kotlin.system.exitProcess

class Node(var next: Node? = null, var chValue: Char? = null, var nodeValue: Node? = null)

class NodeVector(capacity: Int) {
    private var arr = Array<Node?>(capacity) { null }

    fun resize() {
        val newArray = Array<Node?>(arr.size * 2) { null }
        for (i in arr.indices)
            newArray[i] = arr[i]
        arr = newArray
    }

    fun size() = arr.size
    operator fun get(index: Int) = arr[index]
    operator fun set(index: Int, value: Node?) = arr.set(index, value)
}

fun getList(str: String): Node {
    val node = Node()
    val levels = NodeVector(8)
    if (str.first() != '(') {
        println("ERROR: given string list is invalid (should start with '(')\nstring value = [$str]")
        exitProcess(1)
    }
    levels[0] = node
    var depth = 1
    var ind = 1
    fun connect(chValue: Char?) {
        val cur = Node(null, chValue)
        if (levels[depth] != null)
            levels[depth]!!.next = cur
        else
            levels[depth - 1]!!.nodeValue = cur
        levels[depth] = cur
    }

    while (ind < str.length) {
        if (depth <= 0) {
            println("ERROR: given string list is invalid (there are more ')' than '(' )\nstring value = [$str]")
            exitProcess(1)
        }
        when (str[ind]) {
            '(' -> {
                if (depth + 1 == levels.size())
                    levels.resize()
                connect(null)
                depth++
            }
            ')' -> depth--
            else -> {
                connect(str[ind])
            }
        }
        ind++
    }
    if (depth > 0) {
        println("ERROR: given string list is invalid (there are more '(' than ')' )\nstring value = [$str]")
        exitProcess(1)
    }
    return node
}

fun getRepr(list: Node): String {
    var res = ""
    fun dfs(node: Node?) {
        if (node == null) {
            res += ')'
            return
        }
        if (node.chValue != null)
            res += node.chValue
        else {
            res += '('
            dfs(node.nodeValue)
        }
        dfs(node.next)
    }
    dfs(list)
    return res.dropLast(1)
}

fun changeAllEntries(node: Node?, a: Char, toChar: Char? = null, toList: Node? = null) {
    node ?: return
    if (node.nodeValue != null) {
        changeAllEntries(node.nodeValue, a, toChar, toList)
    } else if (node.chValue == a) {
        if (toList != null) {
            node.nodeValue = toList.nodeValue
            node.chValue = toList.chValue
            node.next = toList.next
        } else
            node.chValue = toChar
    }
    changeAllEntries(node.next, a, toChar, toList)
}

fun main() {
    val scan = Scanner(System.`in`)
    val input = scan.next()
    val a = scan.next().first()
    val bStr = scan.next()
    val list = getList(input)
    if (bStr.length == 1 && bStr[0] != '(')
        changeAllEntries(list, a, toChar = bStr[0])
    else
        changeAllEntries(list, a, toList = getList(bStr))
    println(getRepr(list))
}