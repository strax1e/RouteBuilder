package binarysearchtree

import kotlin.collections.MutableCollection
import java.util.ArrayList
import java.util.Stack

class BinarySearchTree<T : Comparable<T>>() : MutableCollection<T>, Cloneable {

    constructor(elements: Collection<T>) : this() {
        this.addAll(elements)
    }

    fun get(element: T): Pair<Boolean, T?> {
        var node: Node<T>? = this.root
        while (node != null) {
            node = when (node.value.compareTo(element)) {
                1 -> node.left
                -1 -> node.right
                else -> return Pair(true, node.value)
            }
        }
        return Pair(false, null)
    }

    public override fun clone(): Any {
        val newTree = BinarySearchTree<T>()
        newTree.root = this.recursiveClone(this.root)
        return newTree
    }

    override fun add(element: T): Boolean {
        if (this.root == null) {
            this.root = Node(element, null)
        } else {
            var node: Node<T>? = this.root
            lateinit var parent: Node<T>
            var compareResult = this.root!!.value.compareTo(element)
            while (node != null) {
                compareResult = node.value.compareTo(element)
                parent = node
                node = when (compareResult) {
                    1 -> node.left
                    -1 -> node.right
                    else -> return true
                }
            }

            if (compareResult == 1) {
                parent.left = Node(element, parent)
            } else {
                parent.right = Node(element, parent)
            }
        }

        ++this.size
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach { if (!this.add(it)) return false }
        return true
    }

    override fun contains(element: T): Boolean {
        return this.get(element).first
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        elements.forEach { if (!this.contains(it)) return false }
        return true
    }

    override fun remove(element: T): Boolean {
        if (this.root == null) return false

        var node: Node<T>? = this.root
        while (node != null) {
            node = when (node.value.compareTo(element)) {
                1 -> node.left
                -1 -> node.right
                else -> return this.deleteNode(node)
            }
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        elements.forEach { if (!this.remove(it)) return false }
        return true
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val nodesToRemove = ArrayList<T>()
        this.forEach {
            if (!elements.contains(it)) {
                if (!nodesToRemove.add(it)) return false
            }
        }
        return this.removeAll(nodesToRemove)
    }

    override fun isEmpty(): Boolean {
        return this.root == null
    }

    override fun clear() {
        this.root = null
        this.size = 0
    }

    override fun iterator(): MutableIterator<T> {

        return object : MutableIterator<T> {
            override fun hasNext(): Boolean {
                return !this.dfsStack.isEmpty() ||
                        (this.isNewNodeAfterRemove && this.currentNode != null)
            }

            override fun remove() {
                val node = this.currentNode
                if (!this.isNextCalled || node == null) throw IllegalStateException()

                val parent = node.parent
                var isNodeIsLeftChild = false
                if (parent != null && node == parent.left) {
                    isNodeIsLeftChild = true
                }

                remove(node.value) // from BinarySearchTree
                this.currentNode = if (parent != null) {
                    if (isNodeIsLeftChild) parent.left else parent.right
                } else root

                if (this.currentNode != null) {
                    this.dfsStack.remove(this.currentNode)
                }

                this.isNextCalled = false
                this.isNewNodeAfterRemove = true
            }

            override fun next(): T {
                if (!this.hasNext()) throw NoSuchElementException()

                this.isNextCalled = true
                if (this.isNewNodeAfterRemove) {
                    return this.currentNode!!.value
                }

                val newCurrentNode = this.dfsStack.pop()
                if (newCurrentNode.right != null) {
                    this.dfsStack.push(newCurrentNode.right)
                }

                if (newCurrentNode.left != null) {
                    this.dfsStack.push(newCurrentNode.left)
                }

                this.currentNode = newCurrentNode
                return newCurrentNode.value
            }

            private var isNewNodeAfterRemove = false
            private var isNextCalled = false
            private var currentNode: Node<T>? = null
            private var dfsStack = Stack<Node<T>>()

            init {
                if (root != null) {
                    this.dfsStack.push(root)
                }
            }
        }
    }

    override fun toString(): String {
        if (this.root == null) {
            return "BST:empty"
        }
        var string = String()
        this.forEach { string += "$it " }
        return string
    }

    private fun deleteNode(node: Node<T>): Boolean {
        val parent = node.parent
        val newNode: Node<T>?
        val nodeRightChild = node.right
        val nodeLeftChild = node.left

        if (nodeRightChild != null) {
            newNode = findMin(nodeRightChild)
            val newNodeParent: Node<T> = newNode.parent!!
            when (newNodeParent.left) {
                newNode -> newNodeParent.left = null
                else -> newNodeParent.right = null
            }

            if (newNode != nodeRightChild) {
                newNode.parent!!.left = newNode.right
                newNode.right?.parent = newNode.parent
                newNode.right = node.right
                newNode.right?.parent = newNode
            }
            newNode.left = node.left
            newNode.left?.parent = newNode
        } else if (nodeLeftChild != null) {
            newNode = nodeLeftChild
        } else {
            newNode = null
        }

        if (parent != null) {
            if (parent.left == node) parent.left = newNode else parent.right = newNode
        } else {
            this.root = newNode
        }
        newNode?.parent = parent

        --this.size
        return true
    }

    private fun recursiveClone(node: Node<T>?): Node<T>? {
        var newNode: Node<T>? = null
        if (node != null) {
            newNode = Node(node.value, node.parent)
            newNode.left = recursiveClone(node.left)
            newNode.right = recursiveClone(node.right)
        }
        return newNode
    }

    companion object {
        private fun <C : Comparable<C>> findMin(node: Node<C>): Node<C> {
            var currentNode: Node<C> = node
            while (currentNode.left != null) {
                currentNode = currentNode.left!!
            }
            return currentNode
        }
    }

    private var root: Node<T>? = null
    override var size: Int = 0
        private set
}

internal class Node<T : Comparable<T>>(val value: T, var parent: Node<T>?) : Comparable<Node<T>> {

    override fun toString(): String {
        return this.value.toString()
    }

    override fun compareTo(other: Node<T>): Int {
        return this.value.compareTo(other.value)
    }

    var left: Node<T>? = null
    var right: Node<T>? = null
}