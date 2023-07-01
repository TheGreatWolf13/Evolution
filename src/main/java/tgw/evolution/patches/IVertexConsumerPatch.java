package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;

public interface IVertexConsumerPatch {

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
