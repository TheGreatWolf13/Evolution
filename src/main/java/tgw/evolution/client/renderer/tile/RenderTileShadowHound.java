//package tgw.evolution.client.renderer.tile;
//
//import com.mojang.blaze3d.platform.GlStateManager;
//import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.util.ResourceLocation;
//import tgw.evolution.Evolution;
//import tgw.evolution.blocks.tileentities.TEShadowHound;
//import tgw.evolution.client.models.tile.ModelTileShadowHound;
//import tgw.evolution.util.MathHelper;
//
//public class RenderTileShadowHound extends TileEntityRenderer<TEShadowHound> {
//
//    private final ModelTileShadowHound model = new ModelTileShadowHound();
//    private final ResourceLocation texture = Evolution.location("textures/block/shadowhound.png");
//
//    @Override
//    public void render(TEShadowHound tile, double x, double y, double z, float partialTicks, int destroyStage) {
//        PlayerEntity clientPlayer = Evolution.PROXY.getClientPlayer();
//        float distance = MathHelper.distance(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5, clientPlayer
//        .posX, clientPlayer.posY, clientPlayer.posZ);
//        if (distance <= 2 || !tile.hasWorld()) {
//            GlStateManager.pushMatrix();
//            GlStateManager.translatef((float) x, (float) y, (float) z);
//            this.bindTexture(this.texture);
//            this.model.render();
//            GlStateManager.popMatrix();
//        }
//        else if (distance <= 10) {
//            GlStateManager.pushMatrix();
//            GlStateManager.translatef((float) x, (float) y, (float) z);
//            GlStateManager.enableBlend();
//            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            GlStateManager.color4f(1.0f, 1.0f, 1.0f, (10.0f - distance) / 8.0f);
//            this.bindTexture(this.texture);
//            this.model.render();
//            GlStateManager.popMatrix();
//        }
//    }
//}
