package io.github.shaksternano.borgar.core.collect

import io.github.shaksternano.borgar.core.io.useAll
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.Closeable
import java.util.function.Consumer

interface CloseableIterable<T> : Iterable<T>, Closeable {

    override fun iterator(): CloseableIterator<T>

    override fun forEach(action: Consumer<in T>) = iterator().use {
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun spliterator(): CloseableSpliterator<T>
}

interface SizedIterable<T> : Iterable<T> {
    val size: Long
}

suspend fun <T, R> Iterable<T>.parallelMap(transform: suspend (T) -> R): List<R> = coroutineScope {
    map { async { transform(it) } }.awaitAll()
}

fun Iterable<*>.elementsEqual(other: Iterable<*>): Boolean {
    if (size != null && other.size != null && size != other.size) {
        return false
    }
    val closeable1 = CloseableIterator.wrap(iterator())
    val closeable2 = CloseableIterator.wrap(other.iterator())
    return useAll(closeable1, closeable2) { iterator1, iterator2 ->
        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) {
                return@useAll false
            }
            val element1 = iterator1.next()
            val element2 = iterator2.next()
            if (element1 != element2) {
                return@useAll false
            }
        }
        !iterator2.hasNext()
    }
}

fun Iterable<*>.hashElements(): Int = CloseableIterator.wrap(iterator()).use { iterator ->
    var hash = 1
    while (iterator.hasNext()) {
        val element = iterator.next()
        hash = 31 * hash + element.hashCode()
    }
    return hash
}

private val Iterable<*>.size: Long?
    get() = when (this) {
        is Collection<*> -> size.toLong()
        is SizedIterable<*> -> size
        else -> null
    }
