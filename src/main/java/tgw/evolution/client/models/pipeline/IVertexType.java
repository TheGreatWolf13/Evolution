package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;

public interface IVertexType<T extends IVertexSink> {
    /**
     * If this vertex type supports {@link IBufferVertexType}, then this method returns this vertex type as a
     * blittable type, performing a safe cast.
     */
    @Nullable
    default IBlittableVertexType<T> asBlittable() {
        return null;
    }

    /**
     * Creates a {@link IVertexSink} which can write into any {@link VertexConsumer}. This is generally used when
     * a special implementation of {@link VertexConsumer} is used that cannot be optimized for, or when
     * complex/unsupported transformations need to be performed using vanilla code paths.
     *
     * @param consumer The {@link VertexConsumer} to write into
     */
    T createFallbackWriter(VertexConsumer consumer);
}
