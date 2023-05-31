package io.github.shaksternano.mediamanipulator.media.io.reader;

import com.google.common.collect.Iterables;
import io.github.shaksternano.mediamanipulator.util.IterableUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class BaseMediaReader<E> extends AbstractCollection<E> implements MediaReader<E> {

    private static final int SPLITERATOR_CHARACTERISTICS = Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;

    private final String format;
    protected double frameRate;
    protected int frameCount;
    protected long duration;
    protected double frameDuration;
    protected int audioChannels;
    protected int audioSampleRate;
    protected int audioBitrate;
    protected int width;
    protected int height;

    public BaseMediaReader(String format) {
        this.format = format;
    }

    @Override
    public double frameRate() {
        return frameRate;
    }

    @Override
    public boolean animated() {
        return this.size() > 1;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public double frameDuration() {
        return frameDuration;
    }

    @Override
    public int audioChannels() {
        return audioChannels;
    }

    @Override
    public int audioSampleRate() {
        return audioSampleRate;
    }

    @Override
    public int audioBitrate() {
        return audioBitrate;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public E first() throws IOException {
        return readFrame(0);
    }

    @Override
    public int size() {
        return frameCount;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        try (var iterator = iterator()) {
            while (iterator.hasNext()) {
                action.accept(iterator.next());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, SPLITERATOR_CHARACTERISTICS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            IterableUtil.hashElements(this),
            format()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MediaReader<?> other) {
            return Iterables.elementsEqual(this, other)
                && Objects.equals(format(), other.format());
        } else {
            return false;
        }
    }
}
