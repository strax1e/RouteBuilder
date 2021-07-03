import binarysearchtree.BinarySearchTree
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    var action: Int
    var bst1 = BinarySearchTree<Int>()
    var bst2 = BinarySearchTree<Int>()
    lateinit var currentBst: BinarySearchTree<Int>
    do {
        println("1. Добавить элемент\n" +
                "2. Удалить элемент\n" +
                "3. Проверить наличие элемента\n" +
                "4. Склонировать дерево в другое\n" +
                "5. Выход")
        println("Дерево 1: $bst1\n" +
                "Дерево 2: $bst2")
        print("Выберите дерево (1 (по умолчанию) или 2): ")
        currentBst = when(scanner.nextInt()) {
            2 -> bst2
            else -> bst1
        }

        print("Выберите действие: ")
        action = scanner.nextInt()
        when (action) {
            1 -> {
                print("Введите число: ")
                currentBst.add(scanner.nextInt())
            }
            2 -> {
                print("Введите число: ")
                currentBst.remove(scanner.nextInt())
            }
            3 -> {
                print("Введите число: ")
                println(currentBst.contains(scanner.nextInt()))
            }
            4 -> {
                if (currentBst == bst1) {
                    bst2 = bst1.clone() as BinarySearchTree<Int>
                } else {
                    bst1 = bst2.clone() as BinarySearchTree<Int>
                }
            }
        }
        println()
    } while (action != 5)
}