package tgw.evolution.client.models.pipeline;

public interface IBlittableVertexType<T extends IVertexSink> extends IBufferVertexType<T> {
    /**
     * Creates a {@link IVertexSink} which writes into a {@link IVertexBufferView}. This allows for specialization
     * when the memory storage is known.
     *
     * @param buffer The backing vertex buffer
     * @param direct True if direct memory access is allowed, otherwise false
     */
    T createBufferWriter(IVertexBufferView buffer, boolean direct);

    default T createBufferWriter(IVertexBufferView buffer) {
        return this.createBufferWriter(buffer, true);
    }
}
