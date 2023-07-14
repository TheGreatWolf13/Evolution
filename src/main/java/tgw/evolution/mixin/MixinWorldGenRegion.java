package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEUtils;

@Mixin(WorldGenRegion.class)
public abstract class MixinWorldGenRegion implements LevelReader {

    @Shadow @Final private static Logger LOGGER;

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.warn("getBlockEntity(BlockPos) should not be called!");
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        ChunkAccess chunk = this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        BlockEntity te = chunk.getBlockEntity_(x, y, z);
        if (te != null) {
            return te;
        }
        CompoundTag compoundTag = chunk.getBlockEntityNbt_(x, y, z);
        BlockState state = chunk.getBlockState_(x, y, z);
        if (compoundTag != null) {
            if ("DUMMY".equals(compoundTag.getString("id"))) {
                if (!state.hasBlockEntity()) {
                    return null;
                }
                te = ((EntityBlock) state.getBlock()).newBlockEntity(new BlockPos(x, y, z), state);
            }
            else {
                te = TEUtils.loadStatic(x, y, z, state, compoundTag);
            }
            if (te != null) {
                chunk.setBlockEntity(te);
                return te;
            }
        }
        if (state.hasBlockEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created at [{}, {}, {}]", x, y, z);
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.warn("getBlockState(BlockPos) should not be called!");
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getBlockState_(x, y, z);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.warn("getFluidState(BlockPos) should not be called!");
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getFluidState_(x, y, z);
    }
}
