package binarytree

import java.lang.RuntimeException
import java.util.Vector

fun Char.isOperation(): Boolean = this == '+' || this == '-' || this == '*'

class BinaryTree() {
    private var head_: Node? = null
    private var isCorrectBinaryTree_ = false

    constructor(str: String) : this() {
        try {
            isCorrectBinaryTree_ = true
            createBinaryTree(str)
        } catch (err: RuntimeException) {
            isCorrectBinaryTree_ = false
            println("Error: ${err.message}")
        }
    }

    private fun createBinaryTree(str: String) {
        var quantityOfTrees = 0
        var isNextWord = true
        var curr: Node? = head_
        for (index in str.indices) {
            when {
                str[index].isWhitespace() -> {
                    isNextWord = true
                    continue
                }
                !isNextWord -> throw RuntimeException("invalid construction")
                !(str[index].isDigit() || str[index].isLetter() || str[index].isOperation()) -> throw RuntimeException("invalid symbol")
                str[index].isOperation() && quantityOfTrees < 2 -> throw RuntimeException("not enough arguments")
            }
            val isOperation = str[index].isOperation()

            when {
                !isOperation && quantityOfTrees == 0 -> {
                    head_ = Node(str[index])
                    curr = head_
                    ++quantityOfTrees
                }
                !isOperation -> {
                    val temp = Node('?')
                    temp.left_ = curr
                    temp.right_ = Node(str[index])

                    if (curr?.parent_ == null) {
                        head_ = temp
                    } else {
                        curr.parent_!!.right_ = temp
                    }
                    temp.parent_ = curr?.parent_
                    temp.left_!!.parent_ = temp
                    temp.right_!!.parent_ = temp
                    curr = temp.right_
                    ++quantityOfTrees
                }
                else -> {
                    curr!!.parent_?.data_ = str[index]
                    curr = curr.parent_
                    --quantityOfTrees
                }
            }

            isNextWord = false
        }
        if (quantityOfTrees != 1)
            throw RuntimeException("invalid syntax")
    }

    fun isCorrectBinaryTree(): Boolean = isCorrectBinaryTree_

    fun postfixTraverse(): Vector<Char> {
        val vec = Vector<Char>()
        recursivePostfixTraverse(vec, head_)
        return vec
    }

    fun prefixTraverse(): Vector<Char> {
        val vec = Vector<Char>()
        recursivePrefixTraverse(vec, head_)
        return vec
    }

    fun infixTraverse(): Vector<Char> {
        val vec = Vector<Char>()
        recursiveInfixTraverse(vec, head_)
        return vec
    }

    private fun recursivePostfixTraverse(vec: Vector<Char>, obj: Node?) {
        if (obj != null) {
            recursivePostfixTraverse(vec, obj.left_)
            recursivePostfixTraverse(vec, obj.right_)
            vec.add(obj.data_)
        }
    }

    private fun recursivePrefixTraverse(vec: Vector<Char>, obj: Node?) {
        if (obj != null) {
            vec.add(obj.data_)
            recursivePrefixTraverse(vec, obj.left_)
            recursivePrefixTraverse(vec, obj.right_)
        }
    }

    private fun recursiveInfixTraverse(vec: Vector<Char>, obj: Node?) {
        if (obj != null) {
            recursiveInfixTraverse(vec, obj.left_)
            vec.add(obj.data_)
            recursiveInfixTraverse(vec, obj.right_)
        }
    }

    fun simplify() {
        if (isCorrectBinaryTree_)
            recursiveSimplify(head_)
    }

    private fun recursiveSimplify(obj: Node?) {
        if (obj != null) {
            tryToSimplify(obj)
            recursiveSimplify(obj.left_)
            tryToSimplify(obj)
            recursiveSimplify(obj.right_)
            tryToSimplify(obj)
        }
    }

    private fun tryToSimplify(obj: Node?) {
        if (obj!!.isOperation() && obj.getOperation() == '-' &&
            obj.left_!!.isDigit() && obj.right_!!.isDigit()
        ) {
            val diff = obj.left_!!.getDigit() - obj.right_!!.getDigit()
            if (diff < 0) {
                obj.left_!!.data_ = '0'
                obj.right_!!.data_ = (-1 * diff + 48).toChar()
            } else {
                obj.data_ = (diff + 48).toChar()
                obj.left_ = null
                obj.right_ = null
            }
        }
    }
}

internal class Node(
    var data_: Char = '?',
    var parent_: Node? = null,
    var left_: Node? = null,
    var right_: Node? = null
) {
    fun isDigit(): Boolean = data_.isDigit()

    fun isLetter(): Boolean = data_.isLetter()

    fun isOperation(): Boolean = when (data_) {
        '+', '-', '*' -> true
        else -> false
    }

    fun getDigit(): Int {
        if (!isDigit())
            throw NoSuchElementException("trying to get a different type")
        return data_.digitToInt()
    }

    fun getLetter(): Char {
        if (!isLetter())
            throw NoSuchElementException("trying to get a different type")
        return data_
    }

    fun getOperation(): Char {
        if (!isOperation())
            throw NoSuchElementException("trying to get a different type")
        return when (data_) {
            '+', '-', '*' -> data_
            else -> '?'
        }
    }
}