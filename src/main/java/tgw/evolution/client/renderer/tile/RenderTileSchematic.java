package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;

public class RenderTileSchematic extends TileEntityRenderer<TESchematic> {

    private static void renderBox(Tessellator tessellator,
                                  BufferBuilder bufferBuilder,
                                  double startX,
                                  double startY,
                                  double startZ,
                                  double endX,
                                  double endY,
                                  double endZ,
                                  int colorAlpha,
                                  int colorGrey,
                                  int colorAxis) {
        GlStateManager.lineWidth(2.0F);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(startX, startY, startZ).color(colorGrey, colorGrey, colorGrey, 0.0F).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(colorGrey, colorAxis, colorAxis, colorAlpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(colorAxis, colorAxis, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(colorAxis, colorGrey, colorAxis, colorAlpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(colorGrey, colorGrey, colorGrey, colorAlpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(colorGrey, colorGrey, colorGrey, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.lineWidth(1.0F);
    }

    private static void renderInvisibleBlocks(TESchematic tile,
                                              double x,
                                              double y,
                                              double z,
                                              BlockPos schematicPos,
                                              Tessellator tessellator,
                                              BufferBuilder bufferBuilder,
                                              boolean bool) {
        GlStateManager.lineWidth(bool ? 3.0F : 1.0F);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        IBlockReader iblockreader = tile.getWorld();
        BlockPos blockpos = tile.getPos();
        BlockPos blockpos1 = blockpos.add(schematicPos);
        for (BlockPos blockpos2 : BlockPos.getAllInBoxMutable(blockpos1, blockpos1.add(tile.getStructureSize()).add(-1, -1, -1))) {
            BlockState blockstate = iblockreader.getBlockState(blockpos2);
            boolean flag = blockstate.isAir();
            boolean flag1 = blockstate.getBlock() == Blocks.STRUCTURE_VOID;
            if (flag || flag1) {
                float f = flag ? 0.05F : 0.0F;
                double d0 = ((blockpos2.getX() - blockpos.getX()) + 0.45F) + x - f;
                double d1 = ((blockpos2.getY() - blockpos.getY()) + 0.45F) + y - f;
                double d2 = ((blockpos2.getZ() - blockpos.getZ()) + 0.45F) + z - f;
                double d3 = ((blockpos2.getX() - blockpos.getX()) + 0.55F) + x + f;
                double d4 = ((blockpos2.getY() - blockpos.getY()) + 0.55F) + y + f;
                double d5 = ((blockpos2.getZ() - blockpos.getZ()) + 0.55F) + z + f;
                if (bool) {
                    WorldRenderer.drawBoundingBox(bufferBuilder, d0, d1, d2, d3, d4, d5, 0.0F, 0.0F, 0.0F, 1.0F);
                }
                else if (flag) {
                    WorldRenderer.drawBoundingBox(bufferBuilder, d0, d1, d2, d3, d4, d5, 0.5F, 0.5F, 1.0F, 1.0F);
                }
                else {
                    WorldRenderer.drawBoundingBox(bufferBuilder, d0, d1, d2, d3, d4, d5, 1.0F, 0.25F, 0.25F, 1.0F);
                }
            }
        }
        tessellator.draw();
    }

    @Override
    public boolean isGlobalRenderer(TESchematic te) {
        return true;
    }

    @Override
    public void render(TESchematic tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if (Minecraft.getInstance().player.canUseCommandBlock() || Minecraft.getInstance().player.isSpectator()) {
            super.render(tile, x, y, z, partialTicks, destroyStage);
            BlockPos schematicPos = tile.getSchematicPos();
            BlockPos size = tile.getStructureSize();
            if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
                if (tile.getMode() == SchematicMode.SAVE || tile.getMode() == SchematicMode.LOAD) {
                    double schematicPosX = schematicPos.getX();
                    double schematicPosZ = schematicPos.getZ();
                    double startingY = y + schematicPos.getY() - 0.01;
                    double endY = startingY + size.getY() + 0.02;
                    double partialX;
                    double partialZ;
                    switch (tile.getMirror()) {
                        case LEFT_RIGHT:
                            partialX = size.getX() + 0.02;
                            partialZ = -(size.getZ() + 0.02);
                            break;
                        case FRONT_BACK:
                            partialX = -(size.getX() + 0.02);
                            partialZ = size.getZ() + 0.02;
                            break;
                        default:
                            partialX = size.getX() + 0.02;
                            partialZ = size.getZ() + 0.02;
                    }
                    double startingX;
                    double startingZ;
                    double endX;
                    double endZ;
                    switch (tile.getRotation()) {
                        case CLOCKWISE_90:
                            startingX = x + (partialZ < 0 ? schematicPosX - 0.01 : schematicPosX + 1 + 0.01);
                            startingZ = z + (partialX < 0 ? schematicPosZ + 1 + 0.01 : schematicPosZ - 0.01);
                            endX = startingX - partialZ;
                            endZ = startingZ + partialX;
                            break;
                        case CLOCKWISE_180:
                            startingX = x + (partialX < 0 ? schematicPosX - 0.01 : schematicPosX + 1 + 0.01);
                            startingZ = z + (partialZ < 0 ? schematicPosZ - 0.01 : schematicPosZ + 1 + 0.01);
                            endX = startingX - partialX;
                            endZ = startingZ - partialZ;
                            break;
                        case COUNTERCLOCKWISE_90:
                            startingX = x + (partialZ < 0 ? schematicPosX + 1 + 0.01 : schematicPosX - 0.01);
                            startingZ = z + (partialX < 0 ? schematicPosZ - 0.01 : schematicPosZ + 1 + 0.01);
                            endX = startingX + partialZ;
                            endZ = startingZ - partialX;
                            break;
                        default:
                            startingX = x + (partialX < 0 ? schematicPosX + 1 + 0.01 : schematicPosX - 0.01);
                            startingZ = z + (partialZ < 0 ? schematicPosZ + 1 + 0.01 : schematicPosZ - 0.01);
                            endX = startingX + partialX;
                            endZ = startingZ + partialZ;
                    }
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    GlStateManager.disableFog();
                    GlStateManager.disableLighting();
                    GlStateManager.disableTexture();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                                     GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                                     GlStateManager.SourceFactor.ONE,
                                                     GlStateManager.DestFactor.ZERO);
                    this.setLightmapDisabled(true);
                    if (tile.getMode() == SchematicMode.SAVE || tile.showsBoundingBox()) {
                        renderBox(tessellator, bufferbuilder, startingX, startingY, startingZ, endX, endY, endZ, 255, 223, 127);
                    }
                    if (tile.getMode() == SchematicMode.SAVE && tile.showsAir()) {
                        renderInvisibleBlocks(tile, x, y, z, schematicPos, tessellator, bufferbuilder, true);
                        renderInvisibleBlocks(tile, x, y, z, schematicPos, tessellator, bufferbuilder, false);
                    }
                    this.setLightmapDisabled(false);
                    GlStateManager.lineWidth(1.0F);
                    GlStateManager.enableLighting();
                    GlStateManager.enableTexture();
                    GlStateManager.enableDepthTest();
                    GlStateManager.depthMask(true);
                    GlStateManager.enableFog();
                }
            }
        }
    }
}
