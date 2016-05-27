package com.ichipsea.kotlin.matrix

import java.util.ArrayList

interface Matrix<out T> {
    val cols: Int
    val rows: Int

    operator fun get(x: Int, y: Int): T
}

val <T> Matrix<T>.size: Int
    get() = this.cols * this.rows

interface MutableMatrix<T>: Matrix<T> {
    operator fun set(x: Int, y: Int, value: T)
}

abstract class AbstractMatrix<out T>: Matrix<T> {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        forEach { x, y, value ->
            if (x === 0)
                sb.append('[')
            sb.append(value.toString())
            if (x===cols-1) {
                sb.append(']')
                if (y < rows-1)
                    sb.append(", ")
            } else {
                sb.append(", ")
            }
        }
        sb.append(']')
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Matrix<*>) return false
        if (rows !== other.rows || cols !== other.cols) return false

        var eq = true
        forEach { x, y, value ->
            if (value === null) {
                if (other[x, y] !== null) {
                    eq = false
                    return@forEach
                }
            } else {
                if (!value.equals(other[x, y])) {
                    eq = false
                    return@forEach
                }
            }
        }
        return eq
    }

    override fun hashCode(): Int {
        var h = 17
        forEach { value -> h = h * 37 + (value?.hashCode() ?: 1)}
        return h
    }
}

internal open class TransposedMatrix<out T>(private val original: Matrix<T>): AbstractMatrix<T>() {
    override val cols: Int
        get() = original.rows

    override val rows: Int
        get() = original.cols

    override fun get(x: Int, y: Int): T = original[y, x]
}

internal class TransposedMutableMatrix<T>(private val original: MutableMatrix<T>) :
        TransposedMatrix<T>(original), MutableMatrix<T> {
    override fun set(x: Int, y: Int, value: T) {
        original[y, x] = value
    }
}

fun <T> Matrix<T>.transposedView() : Matrix<T> = TransposedMatrix(this)

internal open class ListMatrix<out T>(override val cols: Int, override val rows: Int,
                                      open protected val list: List<T>) :
        AbstractMatrix<T>() {
    override operator fun get(x: Int, y: Int): T = list[y*cols+x]
}

internal class MutableListMatrix<T>(override val cols: Int, override val rows: Int,
                                    list: MutableList<T>):
        ListMatrix<T>(cols, rows, list), MutableMatrix<T> {
    override val list: MutableList<T>
        get() = super.list as MutableList<T>

    override fun set(x: Int, y: Int, value: T) {
        list[y*cols+x] = value
    }
}

fun <T> matrixOf(cols: Int, rows: Int, vararg elements: T): Matrix<T> {
    return ListMatrix(cols, rows, elements.asList())
}

fun <T> mutableMatrixOf(cols: Int, rows: Int, vararg elements: T): Matrix<T> {
    return MutableListMatrix(cols, rows, elements.toMutableList())
}

inline private fun <T> prepareListForMatrix(cols: Int, rows: Int, init: (Int, Int) -> T): ArrayList<T> {
    val list = ArrayList<T>(cols * rows)
    for (y in 0..rows - 1) {
        for (x in 0..cols - 1) {
            list.add(init(x, y))
        }
    }
    return list
}

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <T> createMatrix(cols: Int, rows: Int, init: (Int, Int) -> T): Matrix<T> {
    return ListMatrix(cols, rows, prepareListForMatrix(cols, rows, init))
}

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <T> createMutableMatrix(cols: Int, rows: Int, init: (Int, Int) -> T): MutableMatrix<T> {
    return MutableListMatrix(cols, rows, prepareListForMatrix(cols, rows, init))
}

inline fun <T, U> Matrix<T>.map(transform: (T) -> U): Matrix<U> {
    return createMatrix(cols, rows) { x, y -> transform(this[x, y]) }
}

inline fun <T, U> Matrix<T>.map(transform: (Int, Int, T) -> U): Matrix<U> {
    return createMatrix(cols, rows) { x, y -> transform(x, y, this[x, y]) }
}

inline fun <T> Matrix<T>.forEach(action: (T) -> Unit): Unit {
    for (y in 0..rows-1) {
        for (x in 0..cols-1) {
            action(this[x, y])
        }
    }
}

inline fun <T> Matrix<T>.forEach(action: (Int, Int, T) -> Unit): Unit {
    for (y in 0..rows-1) {
        for (x in 0..cols-1) {
            action(x, y, this[x, y])
        }
    }
}
