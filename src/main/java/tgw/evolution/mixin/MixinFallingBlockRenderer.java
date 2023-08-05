package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.math.MathHelper;

@Mixin(FallingBlockRenderer.class)
public abstract class MixinFallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {

    public MixinFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @Overwrite
    public void render(FallingBlockEntity entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
        BlockState blockState = entity.getBlockState();
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            Level level = entity.getLevel();
            if (blockState != level.getBlockState(entity.blockPosition()) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
                matrices.pushPose();
                BlockPos blockPos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
                matrices.translate(-0.5, 0.0, -0.5);
                BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
                blockRenderDispatcher.getModelRenderer()
                                     .tesselateBlock(level,
                                                     blockRenderDispatcher.getBlockModel(blockState),
                                                     blockState,
                                                     blockPos,
                                                     matrices,
                                                     buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)),
                                                     false,
                                                     MathHelper.RANDOM,
                                                     blockState.getSeed(entity.getStartPos()),
                                                     OverlayTexture.NO_OVERLAY,
                                                     IModelData.EMPTY);
                matrices.popPose();
                super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
            }
        }
    }
}
