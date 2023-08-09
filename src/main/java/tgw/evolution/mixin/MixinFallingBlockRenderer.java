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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.models.data.IModelData;

import java.util.Random;

@Mixin(FallingBlockRenderer.class)
public abstract class MixinFallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {

    private static final Random RANDOM = new Random();

    public MixinFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @Overwrite
    public void render(FallingBlockEntity entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() == RenderShape.MODEL) {
            Level level = entity.getLevel();
            if (state != level.getBlockState(entity.blockPosition()) && state.getRenderShape() != RenderShape.INVISIBLE) {
                matrices.pushPose();
                matrices.translate(-0.5, 0.0, -0.5);
                BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
                dispatcher.getModelRenderer().tesselateBlock(level,
                                                             dispatcher.getBlockModel(state),
                                                             state,
                                                             Mth.floor(entity.getX()),
                                                             Mth.floor(entity.getBoundingBox().maxY),
                                                             Mth.floor(entity.getZ()),
                                                             matrices,
                                                             buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)),
                                                             false,
                                                             RANDOM,
                                                             state.getSeed(entity.getStartPos()), OverlayTexture.NO_OVERLAY, IModelData.EMPTY
                );
                matrices.popPose();
                super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
            }
        }
    }
}
