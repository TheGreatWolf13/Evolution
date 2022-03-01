package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.client.models.tile.ModelTEMolding;
import tgw.evolution.init.EvolutionResources;

public class RenderTEMolding implements BlockEntityRenderer<TEMolding> {

    private final ModelTEMolding model = new ModelTEMolding();

    public RenderTEMolding(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TEMolding tile, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        matrices.pushPose();
        matrices.scale(0.5f, 0.5f, 0.5f);
//        this.model.setupBase(tile.matrices[1] == null);
//        this.model.setupLayers(tile.matrices[0], 0);
//        for (int i = 1; i < 5; i++) {
//            if (tile.matrices[i] == null) {
//                this.model.setupLayers(Patterns.FALSE_MATRIX, i);
//            }
//            else {
//                this.model.setupLayers(tile.matrices[i], i);
//            }
//        }
        VertexConsumer modelBuffer = buffer.getBuffer(this.model.renderType(EvolutionResources.BLOCK_MOLDING));
        this.model.renderToBuffer(matrices, modelBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        matrices.popPose();
    }
}
