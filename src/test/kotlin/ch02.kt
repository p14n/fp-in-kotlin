package ch02

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

fun fib(n: Int): Int {
    if (n <= 1) return n
    return fib(n - 1) + fib(n - 2)
}

val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun loop(remaining: List<A>): Boolean {
        if (remaining.size <= 1) return true
        if (!order(remaining.head, remaining.tail.head)) return false
        return loop(remaining.tail)
    }
    return loop(aa)
}

fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C {
    return { a -> { b -> f(a, b) } }
}

fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C {
    return { a, b -> f(a)(b) }
}

fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { a -> f(g(a)) }
}

class ch02Test {
    @Test
    fun testCompose() {
        val result = compose({ a: Int -> a + 1 }, { a: Int -> a * 2 })(3)
        assertEquals(7, result, "Compose of 3 should be 7")
    }

    @Test
    fun testUncurry() {
        val result = uncurry(curry { a: Int, b: Int -> a + b })(2, 3)
        assertEquals(5, result, "Uncurry of 2 and 3 should be 5")
    }

    @Test
    fun testCurry() {
        val result = curry { a: Int, b: Int -> a + b }(2)(3)
        assertEquals(5, result, "Curry of 2 and 3 should be 5")
    }

    @Test
    fun testIsSorted() {
        val array = listOf(1, 2, 3, 4, 5)
        val result = isSorted(array, { a, b -> a < b })
        assertEquals(true, result, "Array should be sorted in ascending order")
    }
    @Test
    fun testIsNotSorted() {
        val array = listOf(1, 2, 3, 5, 4)
        val result = isSorted(array, { a, b -> a < b })
        assertEquals(false, result, "Array should be sorted in ascending order")
    }

    @Test
    fun testTail() {
        val list = listOf(1, 2, 3, 4, 5)
        val tail = list.tail
        assertEquals(listOf(2, 3, 4, 5), tail, "Tail of the list should be [2, 3, 4, 5]")
    }

    @Test
    fun testHead() {
        val list = listOf(1, 2, 3, 4, 5)
        val head = list.head
        assertEquals(1, head, "Head of the list should be 1")
    }

    @Test
    fun testFibonacci() {
        val result = fib(5)
        assertEquals(5, result, "Fibonacci of 5 should be 5")
    }
}
