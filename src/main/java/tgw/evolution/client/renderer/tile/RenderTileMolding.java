package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.client.models.tile.ModelTileMolding;

public class RenderTileMolding extends TileEntityRenderer<TEMolding> {

    private final ModelTileMolding model = new ModelTileMolding();
    private static final ResourceLocation TEXTURE = Evolution.location("textures/block/molding_block.png");

    public RenderTileMolding() {
    }

    @Override
    public void render(TEMolding tile, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y, (float) z);
        this.bindTexture(TEXTURE);
        if (tile.matrices[1] == null) {
            this.model.renderBase();
        }
        this.model.renderLayer(tile.matrices[0], 0);
        for (int i = 1; i < 5; i++) {
            if (tile.matrices[i] == null) {
                break;
            }
            this.model.renderLayer(tile.matrices[i], i);
        }
        GlStateManager.popMatrix();
    }
}
