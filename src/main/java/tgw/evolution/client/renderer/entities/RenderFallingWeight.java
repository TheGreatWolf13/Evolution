package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.entities.misc.EntityFallingWeight;

import java.util.Random;

public class RenderFallingWeight extends EntityRenderer<EntityFallingWeight> {

    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();
    private static final Random RANDOM = new Random();

    public RenderFallingWeight(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFallingWeight entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(EntityFallingWeight entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() == RenderShape.MODEL) {
            Level level = entity.level;
            matrices.pushPose();
            MUTABLE_POS.set(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
            matrices.translate(-0.5, 0, -0.5);
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            dispatcher.getModelRenderer()
                      .tesselateBlock(level, dispatcher.getBlockModel(state), state, MUTABLE_POS, matrices, buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)), false, RANDOM, Mth.getSeed(MUTABLE_POS), OverlayTexture.NO_OVERLAY, IModelData.EMPTY);
            matrices.popPose();
            super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
        }
    }
}