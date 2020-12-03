package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.util.MathHelper;

@OnlyIn(Dist.CLIENT)
public class RenderFallingWeight extends EntityRenderer<EntityFallingWeight> {

    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();

    public RenderFallingWeight(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(EntityFallingWeight entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockState state = entity.getBlockState();
        if (state.getRenderType() == BlockRenderType.MODEL) {
            World world = entity.world;
            this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
            MUTABLE_POS.setPos(entity.posX, entity.getBoundingBox().maxY, entity.posZ);
            GlStateManager.translatef((float) (x - MUTABLE_POS.getX() - 0.5),
                                      (float) (y - (int) entity.getBoundingBox().maxY),
                                      (float) (z - MUTABLE_POS.getZ() - 0.5));
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
            blockrendererdispatcher.getBlockModelRenderer()
                                   .renderModel(world,
                                                blockrendererdispatcher.getModelForState(state),
                                                state,
                                                MUTABLE_POS,
                                                bufferbuilder,
                                                true,
                                                MathHelper.RANDOM,
                                                MathHelper.getPositionRandom(MUTABLE_POS));
            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFallingWeight entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}