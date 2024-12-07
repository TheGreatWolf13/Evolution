package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.renderer.chunk.LevelRenderer;

@Mixin(StructureBlockRenderer.class)
public abstract class MixinStructureBlockRenderer {

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public void render(StructureBlockEntity tile,
                       float partialTick,
                       PoseStack matrices,
                       MultiBufferSource bufferSource,
                       int packedLight,
                       int packedOverlay) {
        assert Minecraft.getInstance().player != null;
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            BlockPos pos = tile.getStructurePos();
            Vec3i size = tile.getStructureSize();
            if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
                if (tile.getMode() == StructureMode.SAVE || tile.getMode() == StructureMode.LOAD) {
                    double x = pos.getX();
                    double z = pos.getZ();
                    double minY = pos.getY();
                    double maxY = minY + size.getY();
                    double dx;
                    double dz;
                    switch (tile.getMirror()) {
                        case LEFT_RIGHT -> {
                            dx = size.getX();
                            dz = -size.getZ();
                        }
                        case FRONT_BACK -> {
                            dx = -size.getX();
                            dz = size.getZ();
                        }
                        default -> {
                            dx = size.getX();
                            dz = size.getZ();
                        }
                    }
                    double minX;
                    double minZ;
                    double maxX;
                    double maxZ;
                    switch (tile.getRotation()) {
                        case CLOCKWISE_90 -> {
                            minX = dz < 0 ? x : x + 1;
                            minZ = dx < 0 ? z + 1 : z;
                            maxX = minX - dz;
                            maxZ = minZ + dx;
                        }
                        case CLOCKWISE_180 -> {
                            minX = dx < 0 ? x : x + 1;
                            minZ = dz < 0 ? z : z + 1;
                            maxX = minX - dx;
                            maxZ = minZ - dz;
                        }
                        case COUNTERCLOCKWISE_90 -> {
                            minX = dz < 0 ? x + 1 : x;
                            minZ = dx < 0 ? z : z + 1;
                            maxX = minX + dz;
                            maxZ = minZ - dx;
                        }
                        default -> {
                            minX = dx < 0 ? x + 1 : x;
                            minZ = dz < 0 ? z + 1 : z;
                            maxX = minX + dx;
                            maxZ = minZ + dz;
                        }
                    }
                    VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());
                    if (tile.getMode() == StructureMode.SAVE || tile.getShowBoundingBox()) {
                        LevelRenderer.renderLineBox(matrices, builder, minX, minY, minZ, maxX, maxY, maxZ,
                                                    0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
                    }
                    if (tile.getMode() == StructureMode.SAVE && tile.getShowAir()) {
                        this.renderInvisibleBlocks(tile, builder, pos, matrices);
                    }
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    private void renderInvisibleBlocks(StructureBlockEntity tile, VertexConsumer builder, BlockPos pos, PoseStack matrices) {
        BlockGetter level = tile.getLevel();
        BlockPos blockpos = tile.getBlockPos();
        BlockPos blockpos1 = blockpos.offset(pos);
        assert level != null;
        for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos1, blockpos1.offset(tile.getStructureSize()).offset(-1, -1, -1))) {
            BlockState state = level.getBlockState(blockpos2);
            int i = 0;
            if (state.isAir()) {
                i = 1;
            }
            else if (state.is(Blocks.STRUCTURE_VOID)) {
                i = 2;
            }
            else if (state.is(Blocks.BARRIER)) {
                i = 3;
            }
            else if (state.is(Blocks.LIGHT)) {
                i = 4;
            }
            if (i > 0) {
                float offset = i == 1 ? 0.05F : 0.0F;
                double x0 = (blockpos2.getX() - blockpos.getX()) + 0.45F - offset;
                double y0 = (blockpos2.getY() - blockpos.getY()) + 0.45F - offset;
                double z0 = (blockpos2.getZ() - blockpos.getZ()) + 0.45F - offset;
                double x1 = (blockpos2.getX() - blockpos.getX()) + 0.55F + offset;
                double y1 = (blockpos2.getY() - blockpos.getY()) + 0.55F + offset;
                double z1 = (blockpos2.getZ() - blockpos.getZ()) + 0.55F + offset;
                switch (i) {
                    case 1 -> LevelRenderer.renderLineBox(matrices, builder, x0, y0, z0, x1, y1, z1, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
                    case 2 -> LevelRenderer.renderLineBox(matrices, builder, x0, y0, z0, x1, y1, z1, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
                    case 3 -> LevelRenderer.renderLineBox(matrices, builder, x0, y0, z0, x1, y1, z1, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                    default -> LevelRenderer.renderLineBox(matrices, builder, x0, y0, z0, x1, y1, z1, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
                }
            }
        }
    }
}
