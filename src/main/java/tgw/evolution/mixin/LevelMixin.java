package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.patches.ILevelPatch;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(Level.class)
public abstract class LevelMixin extends CapabilityProvider<Level> implements ILevelPatch, LevelAccessor {

    @Shadow
    @Final
    public boolean isClientSide;
    @Shadow
    protected int randValue;

    public LevelMixin(Class<Level> baseClass) {
        super(baseClass);
    }

    @Override
    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Override
    @Shadow
    public abstract int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ);

    @Override
    public BlockPos getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out) {
        this.randValue = this.randValue * 3 + 0x3c6e_f35f;
        int rand = this.randValue >> 2;
        return out.set(x + (rand & 15), y + (rand >> 16 & mask), z + (rand >> 8 & 15));
    }

    @Shadow
    public abstract boolean isRaining();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocating a BlockPos to check the heightmap y coordinate only.
     */
    @Overwrite
    public boolean isRainingAt(BlockPos pos) {
        if (!this.isRaining()) {
            return false;
        }
        if (!this.canSeeSky(pos)) {
            return false;
        }
        //The method getHeightmapPos(Heightmap.Types, BlockPos) allocates a new BlockPos using the x and z coordinates from the original BlockPos
        // and the y from the heightmap, however, only the y coordinate is needed for this check, so allocating the BlockPos is wasteful.
        if (this.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY()) {
            return false;
        }
        Biome biome = this.getBiome(pos).value();
        return biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.warmEnoughToRain(pos);
    }

    /**
     * @author TheGreatWolf
     * @reason Add ticking for sloping blocks
     */
    @Override
    @Overwrite
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        if (!this.isClientSide) {
            BlockUtils.updateSlopingBlocks(this, pos);
        }
        FluidState fluidState = this.getFluidState(pos);
        return this.setBlock(pos, fluidState.createLegacyBlock(), BlockFlags.NOTIFY_AND_UPDATE | (isMoving ? BlockFlags.IS_MOVING : 0));
    }
}
