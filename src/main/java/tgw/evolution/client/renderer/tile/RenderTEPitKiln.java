package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.client.models.tile.ModelLog;
import tgw.evolution.client.models.tile.ModelTEPitKiln;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.DirectionDiagonal;

import static tgw.evolution.init.EvolutionBStates.LAYERS_0_16;

public class RenderTEPitKiln extends TileEntityRenderer<TEPitKiln> {

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

    public RenderTEPitKiln(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(TEPitKiln tile, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight, int packedOverlay) {
        if (tile.isRemoved()) {
            return;
        }
        matrices.pushPose();
        if (tile.isSingle()) {
            matrices.pushPose();
            matrices.translate(0.5, -0.005, 0.5);
            matrices.scale(1.7f, 1.7f, 1.7f);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.NORTH_WEST),
                                           ItemCameraTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer);
            matrices.popPose();
        }
        else {
            matrices.pushPose();
            matrices.translate(0.25, -0.005, 0.25);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.NORTH_WEST),
                                           ItemCameraTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0.75, -0.005, 0.25);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.NORTH_EAST),
                                           ItemCameraTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0.75, -0.005, 0.75);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.SOUTH_EAST),
                                           ItemCameraTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0.25, -0.005, 0.75f);
            this.itemRenderer.renderStatic(tile.getStack(DirectionDiagonal.SOUTH_WEST),
                                           ItemCameraTransforms.TransformType.GROUND,
                                           packedLight,
                                           packedOverlay,
                                           matrices,
                                           buffer);
            matrices.popPose();
        }
        int layers = tile.getLevel().getBlockState(tile.getBlockPos()).getValue(LAYERS_0_16);
        this.model.setup(layers);
        IVertexBuilder modelBuffer = buffer.getBuffer(this.model.renderType(EvolutionResources.BLOCK_PIT_KILN));
        this.model.renderToBuffer(matrices, modelBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        this.renderLogs(matrices, buffer, packedLight, packedOverlay, layers, tile.getLogs());
        matrices.popPose();
    }

    private void renderLogs(MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight, int packedOverlay, int layers, byte[] logs) {
        for (int i = 8; i < layers && logs[i - 8] != -1; i++) {
            IVertexBuilder modelBuffer = buffer.getBuffer(this.logs[i - 8].renderType(EvolutionResources.BLOCK_PIT_LOG[logs[i - 8]]));
            this.logs[i - 8].renderToBuffer(matrices, modelBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
