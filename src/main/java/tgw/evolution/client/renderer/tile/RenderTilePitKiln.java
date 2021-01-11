package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.client.models.tile.ModelLog;
import tgw.evolution.client.models.tile.ModelTilePitKiln;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.DirectionDiagonal;

import static tgw.evolution.init.EvolutionBStates.LAYERS_0_16;

public class RenderTilePitKiln extends TileEntityRenderer<TEPitKiln> {

    public final ModelTilePitKiln model = new ModelTilePitKiln();
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

    public RenderTilePitKiln() {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(TEPitKiln tile, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        if (tile.isSingle()) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.5f, (float) y - 0.005f, (float) z + 0.5f);
            GlStateManager.scalef(1.7f, 1.7f, 1.7f);
            this.itemRenderer.renderItem(tile.getStack(DirectionDiagonal.NORTH_WEST), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
        else {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.25f, (float) y - 0.005f, (float) z + 0.25f);
            this.itemRenderer.renderItem(tile.getStack(DirectionDiagonal.NORTH_WEST), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.75f, (float) y - 0.005f, (float) z + 0.25f);
            this.itemRenderer.renderItem(tile.getStack(DirectionDiagonal.NORTH_EAST), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.75f, (float) y - 0.005f, (float) z + 0.75f);
            this.itemRenderer.renderItem(tile.getStack(DirectionDiagonal.SOUTH_EAST), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.25f, (float) y - 0.005f, (float) z + 0.75f);
            this.itemRenderer.renderItem(tile.getStack(DirectionDiagonal.SOUTH_WEST), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
        GlStateManager.translatef((float) x, (float) y, (float) z);
        int layers = tile.getWorld().getBlockState(tile.getPos()).get(LAYERS_0_16);
        this.bindTexture(EvolutionResources.PIT_KILN);
        this.model.render(layers);
        this.renderLogs(layers, tile.getLogs());
        GlStateManager.popMatrix();
    }

    private void renderLogs(int layers, byte[] logs) {
        for (int i = 8; i < layers && logs[i - 8] != -1; i++) {
            this.bindTexture(EvolutionResources.PIT_LOGS[logs[i - 8]]);
            this.logs[i - 8].render();
        }
    }
}
