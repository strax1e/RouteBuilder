import binarytree.BinaryTree

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(
            """
            Too small arguments. Аn algebraic expression was expected.
            example: java -jar Main.jar -s "- * + a 2 c - 3 + e * f g";
        """.trimIndent()
        )
    } else {
        for (elem in args) {
            val expr = BinaryTree(elem)
            expr.simplify()
            printResult(expr)
        }
    }
}

fun printTraverse(vector: java.util.Vector<Char>) {
    for (elem in vector) {
        print(
            when {
                elem == '+' || elem == '-' || elem == '*' ||
                        elem.isDigit() || elem.isLetter() -> elem
                else -> '?'
            }
        )
        print(' ')
    }
    println()
}

fun printResult(tree: BinaryTree) {
    if (tree.isCorrectBinaryTree()) {
        printTraverse(tree.postfixTraverse())
        printTraverse(tree.prefixTraverse())
        printTraverse(tree.infixTraverse())
    }
}