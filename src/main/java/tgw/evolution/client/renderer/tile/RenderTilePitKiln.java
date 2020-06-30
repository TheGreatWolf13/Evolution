package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockPitKiln;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.client.models.tile.ModelLog;
import tgw.evolution.client.models.tile.ModelTilePitKiln;

public class RenderTilePitKiln extends TileEntityRenderer<TEPitKiln> {

    public static final ResourceLocation PIT_KILN = Evolution.location("textures/block/pit_kiln.png");
    public static final ResourceLocation LOG_ACACIA = Evolution.location("textures/block/pit_acacia.png");
    public static final ResourceLocation LOG_ASPEN = Evolution.location("textures/block/pit_aspen.png");
    public static final ResourceLocation LOG_BIRCH = Evolution.location("textures/block/pit_birch.png");
    public static final ResourceLocation LOG_CEDAR = Evolution.location("textures/block/pit_cedar.png");
    public static final ResourceLocation LOG_EBONY = Evolution.location("textures/block/pit_ebony.png");
    public static final ResourceLocation LOG_ELM = Evolution.location("textures/block/pit_elm.png");
    public static final ResourceLocation LOG_EUCALYPTUS = Evolution.location("textures/block/pit_eucalyptus.png");
    public static final ResourceLocation LOG_FIR = Evolution.location("textures/block/pit_fir.png");
    public static final ResourceLocation LOG_KAPOK = Evolution.location("textures/block/pit_kapok.png");
    public static final ResourceLocation LOG_MANGROVE = Evolution.location("textures/block/pit_mangrove.png");
    public static final ResourceLocation LOG_MAPLE = Evolution.location("textures/block/pit_maple.png");
    public static final ResourceLocation LOG_OAK = Evolution.location("textures/block/pit_oak.png");
    public static final ResourceLocation LOG_OLD_OAK = Evolution.location("textures/block/pit_old_oak.png");
    public static final ResourceLocation LOG_PALM = Evolution.location("textures/block/pit_palm.png");
    public static final ResourceLocation LOG_PINE = Evolution.location("textures/block/pit_pine.png");
    public static final ResourceLocation LOG_REDWOOD = Evolution.location("textures/block/pit_redwood.png");
    public static final ResourceLocation LOG_SPRUCE = Evolution.location("textures/block/pit_spruce.png");
    public static final ResourceLocation LOG_WILLOW = Evolution.location("textures/block/pit_willow.png");
    private static final ResourceLocation[] LOGS = {LOG_ACACIA,
                                                    LOG_ASPEN,
                                                    LOG_BIRCH,
                                                    LOG_CEDAR,
                                                    LOG_EBONY,
                                                    LOG_ELM,
                                                    LOG_EUCALYPTUS,
                                                    LOG_FIR,
                                                    LOG_KAPOK,
                                                    LOG_MANGROVE,
                                                    LOG_MAPLE,
                                                    LOG_OAK,
                                                    LOG_OLD_OAK,
                                                    LOG_PALM,
                                                    LOG_PINE,
                                                    LOG_REDWOOD,
                                                    LOG_SPRUCE,
                                                    LOG_WILLOW};
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
    public void render(TEPitKiln tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        if (tileEntityIn.single) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.5f, (float) y - 0.005f, (float) z + 0.5f);
            GlStateManager.scalef(1.7f, 1.7f, 1.7f);
            this.itemRenderer.renderItem(tileEntityIn.nwStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
        else {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.25f, (float) y - 0.005f, (float) z + 0.25f);
            this.itemRenderer.renderItem(tileEntityIn.nwStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.75f, (float) y - 0.005f, (float) z + 0.25f);
            this.itemRenderer.renderItem(tileEntityIn.neStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.75f, (float) y - 0.005f, (float) z + 0.75f);
            this.itemRenderer.renderItem(tileEntityIn.seStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.25f, (float) y - 0.005f, (float) z + 0.75f);
            this.itemRenderer.renderItem(tileEntityIn.swStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
        GlStateManager.translatef((float) x, (float) y, (float) z);
        int layers = tileEntityIn.getWorld().getBlockState(tileEntityIn.getPos()).get(BlockPitKiln.LAYERS);
        this.bindTexture(PIT_KILN);
        this.model.render(layers);
        this.renderLogs(layers, tileEntityIn.logs);
        GlStateManager.popMatrix();
    }

    private void renderLogs(int layers, byte[] logs) {
        for (int i = 8; i < layers && logs[i - 8] != -1; i++) {
            this.bindTexture(LOGS[logs[i - 8]]);
            this.logs[i - 8].render();
        }
    }
}
