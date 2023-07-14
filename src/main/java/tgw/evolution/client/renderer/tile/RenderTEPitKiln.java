package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.client.models.tile.ModelLog;
import tgw.evolution.client.models.tile.ModelTEPitKiln;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.math.DirectionDiagonal;

import static tgw.evolution.init.EvolutionBStates.LAYERS_0_16;

public class RenderTEPitKiln implements BlockEntityRenderer<TEPitKiln> {

    public final ModelTEPitKiln model = new ModelTEPitKiln();
    private final ItemRenderer itemRenderer;
    private final ModelLog log1 = new ModelLog(0, 0);
    private final ModelLog log2 = new ModelLog(1, 0);
    private final ModelLog log3 = new ModelLog(2, 0);
    private final ModelLog log4 = new ModelLog(3, 0);
    private final ModelLog log5 = new ModelLog(0, 1);
    private final ModelLog log6 = new ModelLog(1, 1);
    private final ModelLog log7 = new ModelLog(2, 1);
    private final ModelLog log8 = new ModelLog(3, 1);
    private final ModelLog[] logs = {this.log1, this.log2, this.log3, this.log4, this.log5, this.log6, this.log7, this.log8};

    public RenderTEPitKiln(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(TEPitKiln tile, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.isRemoved()) {
            return;
        }
        matrices.pushPose();
        if (tile.isSingle()) {
            matrices.pushPose();
            matrices.translate(0.5, -0.005, 0.5);
            matrices.scale(1.7f, 1.7f, 1.7f);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.NORTH_WEST),
                                           ItemTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer,
                                           0);
            matrices.popPose();
        }
        else {
            matrices.pushPose();
            matrices.translate(0.25, -0.005, 0.25);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.NORTH_WEST),
                                           ItemTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer,
                                           0);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0.75, -0.005, 0.25);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.NORTH_EAST),
                                           ItemTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer,
                                           0);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0.75, -0.005, 0.75);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.SOUTH_EAST),
                                           ItemTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer,
                                           0);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0.25, -0.005, 0.75f);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.SOUTH_WEST),
                                           ItemTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer,
                                           0);
            matrices.popPose();
        }
        assert tile.getLevel() != null;
        int layers = tile.getLevel().getBlockState(tile.getBlockPos()).getValue(LAYERS_0_16);
        this.model.setup(layers);
        VertexConsumer modelBuffer = buffer.getBuffer(this.model.renderType(EvolutionResources.BLOCK_PIT_KILN));
        this.model.renderToBuffer(matrices, modelBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        this.renderLogs(matrices, buffer, packedLight, packedOverlay, layers, tile.getLogs());
        matrices.popPose();
    }

    private void renderLogs(PoseStack matrices, MultiBufferSource buffer, int packedLight, int packedOverlay, int layers, byte[] logs) {
        for (int i = 8; i < layers && logs[i - 8] != -1; i++) {
            VertexConsumer modelBuffer = buffer.getBuffer(this.logs[i - 8].renderType(EvolutionResources.BLOCK_PIT_LOG[logs[i - 8]]));
            this.logs[i - 8].renderToBuffer(matrices, modelBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
