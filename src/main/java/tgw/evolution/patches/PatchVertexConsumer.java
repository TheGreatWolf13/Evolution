package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;

public interface PatchVertexConsumer {

    default void putBulkData(PoseStack.Pose entry,
                             BakedQuad quad,
                             float r,
                             float g,
                             float b,
                             int light,
                             int overlay,
                             boolean readExistingColor) {
        this.putBulkData(entry, quad, r, g, b, 1.0f, light, overlay, readExistingColor);
    }

    void putBulkData(PoseStack.Pose entry,
                     BakedQuad quad,
                     float r,
                     float g,
                     float b,
                     float a,
                     int light,
                     int overlay,
                     boolean readExistingColor);

    void putBulkData(PoseStack.Pose entry,
                     BakedQuad quad,
                     float bright0,
                     float bright1,
                     float bright2,
                     float bright3,
                     float r,
                     float g,
                     float b,
                     int light0,
                     int light1,
                     int light2,
                     int light3,
                     int overlay);
}
