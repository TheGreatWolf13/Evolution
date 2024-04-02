package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

public interface PatchSheetedDecalTextureGenerator {

    default void set(VertexConsumer consumer, Matrix4f pose, Matrix3f normal) {
        throw new AbstractMethodError();
    }
}
