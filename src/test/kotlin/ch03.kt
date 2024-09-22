package ch03

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

sealed class List<out A> {
    companion object {
        fun <A> of(vararg xs: A): List<A> {
            val tail = xs.sliceArray(1 ..< xs.size)
            return if (xs.isEmpty()) Nil else Cons(xs[0], of(*tail))
        }
    }
}

object Nil : List<Nothing>()

data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()

fun <A> tail(xs: List<A>): List<A> =
        when (xs) {
            is Nil -> Nil
            is Cons -> xs.tail
        }

fun <A> setHead(xs: List<A>, x: A): List<A> =
        when (xs) {
            is Nil -> Cons(x, Nil)
            is Cons -> Cons(x, xs.tail)
        }

fun <A> drop(l: List<A>, n: Int): List<A> =
        when (l) {
            is Nil -> Nil
            is Cons -> if (n <= 0) l else drop(l.tail, n - 1)
        }

fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
        when (l) {
            is Nil -> Nil
            is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
        }

fun <A> init(l: List<A>): List<A> =
        when (l) {
            is Nil -> Nil
            is Cons -> if (l.tail == Nil) Nil else Cons(l.head, init(l.tail))
        }

fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
        when (xs) {
            is Nil -> z
            is Cons -> f(xs.head, foldRight(xs.tail, z, f))
        }

fun <A> empty(): List<A> = Nil

fun <A> length(l: List<A>): Int = foldRight(l, 0, { _, acc -> acc + 1 })

tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (A, B) -> B): B =
        when (xs) {
            is Nil -> z
            is Cons -> foldLeft(xs.tail, f(xs.head, z), f)
        }

fun <A> appendL(xs: List<A>, x: A): List<A> =
        foldLeft(
                foldLeft(xs, Cons(x, Nil), { v, a -> Cons(a.head, Cons(v, a.tail)) }),
                empty(),
                { v, a -> Cons(v, a) }
        )

fun <A> append(xs: List<A>, x: A): List<A> = foldRight(xs, Cons(x, Nil), { v, a -> Cons(v, a) })

fun <A> concat(xs: List<List<A>>): List<A> =
        foldLeft(xs, empty(), { v1, a1 -> foldLeft(v1, a1, { v, a -> append(a, v) }) })

fun incAll(xs: List<Int>): List<Int> = foldRight(xs, empty(), { v, a -> Cons(v + 1, a) })

fun doublesToStrings(xs: List<Double>): List<String> =
        foldRight(xs, empty(), { v, a -> Cons(v.toString(), a) })

fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
        foldRight(xs, empty(), { v, a -> Cons(f(v), a) })

fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
        foldRight(xs, empty(), { v, a -> if (f(v)) Cons(v, a) else a })

fun <A> flatMap(xs: List<A>, f: (A) -> List<A>): List<A> = concat(map(xs, f))

fun <A> filterViaFlatMap(xs: List<A>, f: (A) -> Boolean): List<A> =
        flatMap(xs, { v -> if (f(v)) List.of(v) else empty() })

fun <A> zipWith(xs: List<A>, ys: List<A>, f: (A, A) -> A): List<A> =
        when (xs) {
            is Nil -> Nil
            is Cons ->
                    when (ys) {
                        is Nil -> Nil
                        is Cons -> Cons(f(xs.head, ys.head), zipWith(xs.tail, ys.tail, f))
                    }
        }

tailrec fun <A> hasSubsequence(l: List<A>, sub: List<A>): Boolean =
        when (l) {
            is Nil -> sub == Nil
            is Cons ->
                    when (sub) {
                        is Nil -> true
                        is Cons ->
                                if (l.head == sub.head) hasSubsequence(l.tail, sub.tail)
                                else hasSubsequence(l.tail, sub)
                    }
        }

sealed class Tree<out A>

data class Leaf<A>(val value: A) : Tree<A>()

data class Branch<A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()

fun <A> sizeT(tree: Tree<A>): Int =
        when (tree) {
            is Leaf -> 1
            is Branch -> 1 + sizeT(tree.left) + sizeT(tree.right)
        }

fun maxT(tree: Tree<Int>): Int =
        when (tree) {
            is Leaf -> tree.value
            is Branch -> maxOf(maxT(tree.left), maxT(tree.right))
        }

fun <A> maxDepth(tree: Tree<A>): Int =
        when (tree) {
            is Leaf -> 1
            is Branch -> 1 + maxOf(maxDepth(tree.left), maxDepth(tree.right))
        }

fun <A> mapT(tree: Tree<A>, f: (A) -> A): Tree<A> =
        when (tree) {
            is Leaf -> Leaf(f(tree.value))
            is Branch -> Branch(mapT(tree.left, f), mapT(tree.right, f))
        }

fun <A, B> fold(tree: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
        when (tree) {
            is Leaf -> l(tree.value)
            is Branch -> b(fold(tree.left, l, b), fold(tree.right, l, b))
        }

fun <A> size(tree: Tree<A>): Int = fold(tree, { 1 }, { a, b -> a + b + 1 })

fun max(tree: Tree<Int>): Int = fold(tree, { it }, { a, b -> maxOf(a, b) })

fun <A> depth(tree: Tree<A>): Int = fold(tree, { 1 }, { a, b -> 1 + maxOf(a, b) })

fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
        fold(tree, { Leaf(f(it)) as Tree<B> }, { a, b -> Branch(a, b) })

class ch03Test {

    @Test
    fun testMapFold() {
        val tree = Branch(Leaf(1), Branch(Leaf(2), Branch(Leaf(11), Leaf(12))))
        val expected = Branch(Leaf(11), Branch(Leaf(12), Branch(Leaf(21), Leaf(22))))
        val result = map(tree, { it + 10 })
        assertEquals(expected, result, "Map should apply function to all elements")
    }

    @Test
    fun testMaxDepthFold() {
        val tree = Branch(Leaf(1), Branch(Leaf(2), Branch(Leaf(11), Leaf(12))))
        assertEquals(4, depth(tree), "Max depth of tree with 4 leaves should be 4")
    }

    @Test
    fun testMaxFold() {
        val tree = Branch(Leaf(1), Branch(Leaf(2), Branch(Leaf(11), Leaf(12))))
        assertEquals(12, max(tree), "Max of tree with 4 leaves should be 12")
    }

    @Test
    fun testSizeFold() {
        val tree = Branch(Leaf(1), Leaf(2))
        val biggerTree = Branch(tree, Branch(Leaf(3), tree))
        assertEquals(3, size(tree), "Size of tree with 2 leaves should be 3")
        assertEquals(9, size(biggerTree), "Size of bigger tree should be 9")
    }

    @Test
    fun testMap() {
        val tree = Branch(Leaf(1), Branch(Leaf(2), Branch(Leaf(11), Leaf(12))))
        val expected = Branch(Leaf(11), Branch(Leaf(12), Branch(Leaf(21), Leaf(22))))
        val result = mapT(tree, { it + 10 })
        assertEquals(expected, result, "Map should apply function to all elements")
    }

    @Test
    fun testMaxDepth() {
        val tree = Branch(Leaf(1), Branch(Leaf(2), Branch(Leaf(11), Leaf(12))))
        assertEquals(4, maxDepth(tree), "Max depth of tree with 4 leaves should be 4")
    }

    @Test
    fun testMax() {
        val tree = Branch(Leaf(1), Branch(Leaf(2), Branch(Leaf(11), Leaf(12))))
        assertEquals(12, maxT(tree), "Max of tree with 4 leaves should be 12")
    }

    @Test
    fun testSize() {
        val tree = Branch(Leaf(1), Leaf(2))
        val biggerTree = Branch(tree, Branch(Leaf(3), tree))
        assertEquals(3, sizeT(tree), "Size of tree with 2 leaves should be 3")
        assertEquals(9, sizeT(biggerTree), "Size of bigger tree should be 9")
    }

    @Test
    fun testHasSubsequence() {
        val l1 = List.of(1, 2, 3, 4, 5)
        val l2 = List.of(1, 2, 3)
        val l3 = List.of(2, 3, 4)
        val l4 = List.of(5, 6, 7)
        val l5 = List.of(3, 4, 5)
        val l6 = List.of(4, 5, 6)
        assertTrue(hasSubsequence(l1, l2))
        assertTrue(hasSubsequence(l1, l3))
        assertFalse(hasSubsequence(l1, l4))
        assertTrue(hasSubsequence(l1, l5))
        assertFalse(hasSubsequence(l1, l6))
    }

    @Test
    fun testZipWith() {
        val l1 = List.of(1, 2, 3)
        val l2 = List.of(4, 5, 6)
        val result = zipWith(l1, l2, { a, b -> a + b })
        assertEquals(
                List.of(5, 7, 9),
                result,
                "Zip with sum of [1, 2, 3] and [4, 5, 6] should be [5, 7, 9]"
        )
    }

    @Test
    fun testFilterViaFlatMap() {
        val l1 = List.of(1, 2, 3, 4)
        val result = filterViaFlatMap(l1, { it % 2 == 0 })
        assertEquals(
                Cons(2, Cons(4, Nil)),
                result,
                "Filter even numbers from [1, 2, 3, 4] should be [2, 4]"
        )
    }
    @Test
    fun testFlatMap() {
        val l1 = List.of(1, 2, 3)
        val result = flatMap(l1, { List.of(it, it) })
        assertEquals(
                List.of(1, 1, 2, 2, 3, 3),
                result,
                "Flat map of [1, 2, 3] with function { it, it } should be [1, 1, 2, 2, 3, 3]"
        )
    }
    @Test
    fun testFilterEvenNumbers() {
        val l1 = List.of(1, 2, 3, 4)
        val result = filter(l1, { it % 2 == 0 })
        assertEquals(
                Cons(2, Cons(4, Nil)),
                result,
                "Filter even numbers from [1, 2, 3, 4] should be [2, 4]"
        )
    }

    @Test
    fun testMapInc() {
        val result = map(Cons(1, Cons(2, Cons(3, Nil))), { it + 1 })
        assertEquals(
                Cons(2, Cons(3, Cons(4, Nil))),
                result,
                "Map increment of [1, 2, 3] should be [2, 3, 4]"
        )
    }

    @Test
    fun testMapDoubleToString() {
        val result = map(Cons(1.0, Cons(2.0, Cons(3.0, Nil))), { it.toString() })
        val expected: List<String> = Cons("1.0", Cons("2.0", Cons("3.0", Nil)))
        assertEquals(expected, result, "Convert [1.0, 2.0, 3.0] to [\"1.0\", \"2.0\", \"3.0\"]")
    }

    @Test
    fun testDoublesToStrings() {
        val result = doublesToStrings(Cons(1.0, Cons(2.0, Cons(3.0, Nil))))
        val expected: List<String> = Cons("1.0", Cons("2.0", Cons("3.0", Nil)))
        assertEquals(expected, result, "Convert [1.0, 2.0, 3.0] to [\"1.0\", \"2.0\", \"3.0\"]")
    }

    @Test
    fun testIncAll() {
        val result = incAll(Cons(1, Cons(2, Cons(3, Nil))))
        assertEquals(
                Cons(2, Cons(3, Cons(4, Nil))),
                result,
                "Increment all elements of [1, 2, 3] should be [2, 3, 4]"
        )
    }

    @Test
    fun testConcat() {
        val l1 = Cons(1, Cons(2, Nil))
        val l2 = Cons(3, Cons(4, Nil))
        val result = concat(Cons(l1, Cons(l2, Nil)))
        assertEquals(
                Cons(1, Cons(2, Cons(3, Cons(4, Nil)))),
                result,
                "Concat of [[1, 2], [3, 4]] should be [1, 2, 3, 4]"
        )
    }

    @Test
    fun testAppendL() {
        val result = appendL(Cons(1, Cons(2, Cons(3, Nil))), 4)
        assertEquals(
                Cons(1, Cons(2, Cons(3, Cons(4, Nil)))),
                result,
                "Append 4 to [1, 2, 3] should be [1, 2, 3, 4]"
        )
    }

    @Test
    fun testAppend() {
        val result = append(Cons(1, Cons(2, Cons(3, Nil))), 4)
        assertEquals(
                Cons(1, Cons(2, Cons(3, Cons(4, Nil)))),
                result,
                "Append 4 to [1, 2, 3] should be [1, 2, 3, 4]"
        )
    }

    @Test
    fun testLength() {
        val result = length(Cons(1, Cons(2, Cons(3, Nil))))
        assertEquals(3, result, "Length of [1, 2, 3] should be 3")
    }

    @Test
    fun testInit() {
        val result = init(Cons(1, Cons(2, Cons(3, Nil))))
        assertEquals(Cons(1, Cons(2, Nil)), result, "Init of [1, 2, 3] should be [1, 2]")
    }

    @Test
    fun testDropWhile() {
        val result = dropWhile(Cons(1, Cons(2, Cons(3, Nil))), { it < 3 })
        assertEquals(Cons(3, Nil), result, "Drop while < 3 from [1, 2, 3] should be [3]")
    }

    @Test
    fun testDrop() {
        val result = drop(Cons(1, Cons(2, Cons(3, Nil))), 2)
        assertEquals(Cons(3, Nil), result, "Drop 2 from [1, 2, 3] should be [3]")
    }

    @Test
    fun testSetHead() {
        val result = setHead(Cons(1, Cons(2, Cons(3, Nil))), 0)
        assertEquals(
                Cons(0, Cons(2, Cons(3, Nil))),
                result,
                "Set head of [1, 2, 3] to 0 should be [0, 2, 3]"
        )
    }

    @Test
    fun testTail() {
        val result = tail(Cons(1, Cons(2, Cons(3, Nil))))
        assertEquals(Cons(2, Cons(3, Nil)), result, "Tail of [1, 2, 3] should be [2, 3]")
    }
}
