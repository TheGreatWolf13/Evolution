package tgw.evolution.client.models.pipeline;

public abstract class AbstractVertexTransformer<T extends IVertexSink> implements IVertexSink {
    protected final T delegate;

    protected AbstractVertexTransformer(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public void ensureCapacity(int count) {
        this.delegate.ensureCapacity(count);
    }

    @Override
    public void flush() {
        this.delegate.flush();
    }

    @Override
    public int getVertexCount() {
        return this.delegate.getVertexCount();
    }
}
