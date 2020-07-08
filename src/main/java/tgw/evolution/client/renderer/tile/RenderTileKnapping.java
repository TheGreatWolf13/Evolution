package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.client.models.tile.ModelTileKnapping;

public class RenderTileKnapping extends TileEntityRenderer<TEKnapping> {

    private final ModelTileKnapping knappingModel = new ModelTileKnapping();

    @Override
    public void render(TEKnapping tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y, (float) z);
        this.getModel(tileEntityIn).renderAll(tileEntityIn.matrix);
        GlStateManager.popMatrix();
    }

    private ModelTileKnapping getModel(TEKnapping tileEntityKnapping) {
        Block block = this.getWorld().getBlockState(tileEntityKnapping.getPos()).getBlock();
        ResourceLocation resourceLocation = null;
        if (block instanceof BlockKnapping) {
            resourceLocation = Evolution.location("textures/block/knapping_" + ((BlockKnapping) block).getStoneName().getName() + ".png");
        }
        this.bindTexture(resourceLocation);
        return this.knappingModel;
    }
}
