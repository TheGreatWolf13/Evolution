package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;

public class RenderTESchematic extends TileEntityRenderer<TESchematic> {

    public RenderTESchematic(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    private static void renderInvisibleBlocks(TESchematic tile, IVertexBuilder buffer, BlockPos schematicPos, boolean bool, MatrixStack matrices) {
        IBlockReader world = tile.getLevel();
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
                    WorldRenderer.renderLineBox(matrices, buffer, x0, y0, z0, x1, y1, z1, 0.0F, 0.0F, 0.0F, 1.0F);
                }
                else if (isAir) {
                    WorldRenderer.renderLineBox(matrices, buffer, x0, y0, z0, x1, y1, z1, 0.5F, 0.5F, 1.0F, 1.0F);
                }
                else {
                    WorldRenderer.renderLineBox(matrices, buffer, x0, y0, z0, x1, y1, z1, 1.0F, 0.25F, 0.25F, 1.0F);
                }
            }
        }
    }

    @Override
    public void render(TESchematic tile, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight, int packedOverlay) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            BlockPos schematicPos = tile.getSchematicPos();
            BlockPos size = tile.getStructureSize();
            if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
                if (tile.getMode() == SchematicMode.SAVE || tile.getMode() == SchematicMode.LOAD) {
                    double schematicPosX = schematicPos.getX();
                    double schematicPosZ = schematicPos.getZ();
                    double startingY = schematicPos.getY();
                    double endY = startingY + size.getY();
                    double partialX;
                    double partialZ;
                    switch (tile.getMirror()) {
                        case LEFT_RIGHT:
                            partialX = size.getX();
                            partialZ = -size.getZ();
                            break;
                        case FRONT_BACK:
                            partialX = -size.getX();
                            partialZ = size.getZ();
                            break;
                        default:
                            partialX = size.getX();
                            partialZ = size.getZ();
                    }
                    double startingX;
                    double startingZ;
                    double endX;
                    double endZ;
                    switch (tile.getRotation()) {
                        case CLOCKWISE_90:
                            startingX = partialZ < 0 ? schematicPosX : schematicPosX + 1;
                            startingZ = partialX < 0 ? schematicPosZ + 1 : schematicPosZ;
                            endX = startingX - partialZ;
                            endZ = startingZ + partialX;
                            break;
                        case CLOCKWISE_180:
                            startingX = partialX < 0 ? schematicPosX : schematicPosX + 1;
                            startingZ = partialZ < 0 ? schematicPosZ : schematicPosZ + 1;
                            endX = startingX - partialX;
                            endZ = startingZ - partialZ;
                            break;
                        case COUNTERCLOCKWISE_90:
                            startingX = partialZ < 0 ? schematicPosX + 1 : schematicPosX;
                            startingZ = partialX < 0 ? schematicPosZ : schematicPosZ + 1;
                            endX = startingX + partialZ;
                            endZ = startingZ - partialX;
                            break;
                        default:
                            startingX = partialX < 0 ? schematicPosX + 1 : schematicPosX;
                            startingZ = partialZ < 0 ? schematicPosZ + 1 : schematicPosZ;
                            endX = startingX + partialX;
                            endZ = startingZ + partialZ;
                    }
                    IVertexBuilder drawBuffer = buffer.getBuffer(RenderType.lines());
                    if (tile.getMode() == SchematicMode.SAVE || tile.showsBoundingBox()) {
                        WorldRenderer.renderLineBox(matrices,
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
