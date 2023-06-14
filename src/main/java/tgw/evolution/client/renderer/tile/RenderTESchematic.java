package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

public class RenderTESchematic implements BlockEntityRenderer<TESchematic> {

    public RenderTESchematic(BlockEntityRendererProvider.Context context) {
    }

    private static void renderInvisibleBlocks(TESchematic tile, VertexConsumer buffer, BlockPos schematicPos, boolean bool, PoseStack matrices) {
        LevelReader world = tile.getLevel();
        BlockPos tilePos = tile.getBlockPos();
        BlockPos schematicAbsPos = tilePos.offset(schematicPos);
        for (BlockPos mutable : BlockPos.betweenClosed(schematicAbsPos, schematicAbsPos.offset(tile.getStructureSize()).offset(-1, -1, -1))) {
            BlockState state = world.getBlockState(mutable);
            boolean isAir = state.isAir();
            boolean isVoid = state.getBlock() == Blocks.STRUCTURE_VOID;
            if (isAir || isVoid) {
                float size = isAir ? 0.05F : 0.0F;
                double x0 = (mutable.getX() - tilePos.getX()) + 0.45F - size;
                double y0 = (mutable.getY() - tilePos.getY()) + 0.45F - size;
                double z0 = (mutable.getZ() - tilePos.getZ()) + 0.45F - size;
                double x1 = (mutable.getX() - tilePos.getX()) + 0.55F + size;
                double y1 = (mutable.getY() - tilePos.getY()) + 0.55F + size;
                double z1 = (mutable.getZ() - tilePos.getZ()) + 0.55F + size;
                if (bool) {
                    EvLevelRenderer.renderLineBox(matrices, buffer, x0, y0, z0, x1, y1, z1, 0.0F, 0.0F, 0.0F, 1.0F);
                }
                else if (isAir) {
                    EvLevelRenderer.renderLineBox(matrices, buffer, x0, y0, z0, x1, y1, z1, 0.5F, 0.5F, 1.0F, 1.0F);
                }
                else {
                    EvLevelRenderer.renderLineBox(matrices, buffer, x0, y0, z0, x1, y1, z1, 1.0F, 0.25F, 0.25F, 1.0F);
                }
            }
        }
    }

    @Override
    public void render(TESchematic tile, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            BlockPos schematicPos = tile.getSchematicPos();
            Vec3i size = tile.getStructureSize();
            if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
                if (tile.getMode() == SchematicMode.SAVE || tile.getMode() == SchematicMode.LOAD) {
                    double schematicPosX = schematicPos.getX();
                    double schematicPosZ = schematicPos.getZ();
                    double startingY = schematicPos.getY();
                    double endY = startingY + size.getY();
                    double partialX;
                    double partialZ;
                    switch (tile.getMirror()) {
                        case LEFT_RIGHT -> {
                            partialX = size.getX();
                            partialZ = -size.getZ();
                        }
                        case FRONT_BACK -> {
                            partialX = -size.getX();
                            partialZ = size.getZ();
                        }
                        default -> {
                            partialX = size.getX();
                            partialZ = size.getZ();
                        }
                    }
                    double startingX;
                    double startingZ;
                    double endX;
                    double endZ;
                    switch (tile.getRotation()) {
                        case CLOCKWISE_90 -> {
                            startingX = partialZ < 0 ? schematicPosX : schematicPosX + 1;
                            startingZ = partialX < 0 ? schematicPosZ + 1 : schematicPosZ;
                            endX = startingX - partialZ;
                            endZ = startingZ + partialX;
                        }
                        case CLOCKWISE_180 -> {
                            startingX = partialX < 0 ? schematicPosX : schematicPosX + 1;
                            startingZ = partialZ < 0 ? schematicPosZ : schematicPosZ + 1;
                            endX = startingX - partialX;
                            endZ = startingZ - partialZ;
                        }
                        case COUNTERCLOCKWISE_90 -> {
                            startingX = partialZ < 0 ? schematicPosX + 1 : schematicPosX;
                            startingZ = partialX < 0 ? schematicPosZ : schematicPosZ + 1;
                            endX = startingX + partialZ;
                            endZ = startingZ - partialX;
                        }
                        default -> {
                            startingX = partialX < 0 ? schematicPosX + 1 : schematicPosX;
                            startingZ = partialZ < 0 ? schematicPosZ + 1 : schematicPosZ;
                            endX = startingX + partialX;
                            endZ = startingZ + partialZ;
                        }
                    }
                    VertexConsumer drawBuffer = buffer.getBuffer(RenderType.lines());
                    if (tile.getMode() == SchematicMode.SAVE || tile.showsBoundingBox()) {
                        EvLevelRenderer.renderLineBox(matrices,
                                                      drawBuffer,
                                                      startingX,
                                                      startingY,
                                                      startingZ,
                                                      endX,
                                                      endY,
                                                      endZ,
                                                      0.9f,
                                                      0.9f,
                                                      0.9f,
                                                      1.0f,
                                                      0.5f,
                                                      0.5f,
                                                      0.5f);
                    }
                    if (tile.getMode() == SchematicMode.SAVE && tile.showsAir()) {
                        renderInvisibleBlocks(tile, drawBuffer, schematicPos, true, matrices);
                        renderInvisibleBlocks(tile, drawBuffer, schematicPos, false, matrices);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TESchematic te) {
        return true;
    }
}
