package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IVertexDrain {

    /**
     * Returns a {@link IVertexDrain} implementation on the provided {@link VertexConsumer}. Since the interface
     * is always implemented on a given VertexConsumer, this is simply implemented as a cast internally.
     *
     * @param consumer The {@link VertexConsumer}
     * @return A {@link IVertexDrain}
     */
    static IVertexDrain of(VertexConsumer consumer) {
        return (IVertexDrain) consumer;
    }

    /**
     * Returns a {@link IVertexSink} of type {@link T}, created from {@param factory}, which transforms and writes
     * vertices through this vertex drain.
     *
     * @param factory The factory to create a vertex sink using
     * @param <T>     The vertex sink's type
     * @return A new {@link IVertexSink} of type {@link T}
     */
    <T extends IVertexSink> T createSink(IVertexType<T> factory);
}
