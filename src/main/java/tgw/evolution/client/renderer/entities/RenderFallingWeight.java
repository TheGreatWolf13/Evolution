package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.util.MathHelper;

@OnlyIn(Dist.CLIENT)
public class RenderFallingWeight extends EntityRenderer<EntityFallingWeight> {

    private static final BlockPos.Mutable MUTABLE_POS = new BlockPos.Mutable();

    public RenderFallingWeight(EntityRendererManager manager) {
        super(manager);
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFallingWeight entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }

    @Override
    public void render(EntityFallingWeight entity, float yaw, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() == BlockRenderType.MODEL) {
            World world = entity.level;
            matrices.pushPose();
            MUTABLE_POS.set(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
            matrices.translate(-0.5, 0, -0.5);
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            for (RenderType type : RenderType.chunkBufferLayers()) {
                if (RenderTypeLookup.canRenderInLayer(state, type)) {
                    ForgeHooksClient.setRenderLayer(type);
                    dispatcher.getModelRenderer()
                              .tesselateBlock(world,
                                              dispatcher.getBlockModel(state),
                                              state,
                                              MUTABLE_POS,
                                              matrices,
                                              buffer.getBuffer(type),
                                              false,
                                              MathHelper.RANDOM,
                                              MathHelper.getPositionRandom(MUTABLE_POS),
                                              OverlayTexture.NO_OVERLAY);
                }
            }
            ForgeHooksClient.setRenderLayer(null);
            matrices.popPose();
            super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
        }
    }
}