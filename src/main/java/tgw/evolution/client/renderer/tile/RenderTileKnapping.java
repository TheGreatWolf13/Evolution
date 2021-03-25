package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.client.models.tile.ModelTileKnapping;
import tgw.evolution.init.EvolutionResources;

public class RenderTileKnapping extends TileEntityRenderer<TEKnapping> {

    private final ModelTileKnapping knappingModel = new ModelTileKnapping();

    private ModelTileKnapping getModel(TEKnapping tileEntityKnapping) {
        Block block = this.getWorld().getBlockState(tileEntityKnapping.getPos()).getBlock();
        ResourceLocation resourceLocation = null;
        if (block instanceof BlockKnapping) {
            resourceLocation = EvolutionResources.KNAPPING[((BlockKnapping) block).getVariant().getId()];
        }
        this.bindTexture(resourceLocation);
        return this.knappingModel;
    }

    @Override
    public void render(TEKnapping tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y, (float) z);
        this.getModel(tileEntityIn).renderAll(tileEntityIn.matrix);
        GlStateManager.popMatrix();
    }
}
