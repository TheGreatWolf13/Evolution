package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.entities.misc.EntityFallingTimber;
import tgw.evolution.util.MathHelper;

@OnlyIn(Dist.CLIENT)
public class RenderFallingTimber extends EntityRenderer<EntityFallingTimber> {

    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();

    public RenderFallingTimber(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(EntityFallingTimber entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockState state = entity.getBlockState();
        this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
        MUTABLE_POS.setPos(entity.posX, entity.getBoundingBox().maxY, entity.posZ);
        GlStateManager.translatef((float) (x - MUTABLE_POS.getX() - 0.5), (float) (y - MUTABLE_POS.getY()), (float) (z - MUTABLE_POS.getZ() - 0.5));
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        blockrendererdispatcher.getBlockModelRenderer()
                               .renderModel(entity.world,
                                            blockrendererdispatcher.getModelForState(state),
                                            state,
                                            MUTABLE_POS,
                                            bufferbuilder,
                                            false,
                                            MathHelper.RANDOM,
                                            MathHelper.getPositionRandom(MUTABLE_POS));
        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityFallingTimber entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}