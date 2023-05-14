package tgw.evolution.util.collection;

import com.google.common.collect.UnmodifiableIterator;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public abstract class AbstractReusableIterator<T> extends UnmodifiableIterator<T> {

    private @Nullable T next;
    private State state = State.NOT_READY;

    @Nullable
    protected abstract T computeNext();

    @Nullable
    protected final T endOfData() {
        this.state = State.DONE;
        return null;
    }

    @Override
    public final boolean hasNext() {
        return switch (this.state) {
            case FAILED -> throw new IllegalStateException("Iterator failed!");
            case DONE -> false;
            case READY -> true;
            case NOT_READY -> this.tryToComputeNext();
        };
    }

    @Override
    @Nullable
    public final T next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        this.state = State.NOT_READY;
        T result = this.next;
        this.next = null;
        return result;
    }

    @Nullable
    public final T peek() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.next;
    }

    protected final void reset() {
        this.state = State.NOT_READY;
        this.next = null;
    }

    private boolean tryToComputeNext() {
        this.state = State.FAILED;
        this.next = this.computeNext();
        if (this.state != State.DONE) {
            this.state = State.READY;
            return true;
        }
        return false;
    }

    private enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED
    }
}
