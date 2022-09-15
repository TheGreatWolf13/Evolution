package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.blocks.IFluidLoggable;
import tgw.evolution.patches.ILevelPatch;

@Mixin(Level.class)
public abstract class LevelMixin extends CapabilityProvider<Level> implements ILevelPatch, CommonLevelAccessor {

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
    public void getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out) {
        this.randValue = this.randValue * 3 + 0x3c6e_f35f;
        int rand = this.randValue >> 2;
        out.set(x + (rand & 15), y + (rand >> 16 & mask), z + (rand >> 8 & 15));
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

    @Inject(method = "removeBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onRemoveBlock(BlockPos pos, boolean isMoving, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = this.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable fluid) {
            if (fluid.remove((Level) (Object) this, pos, state)) {
                cir.setReturnValue(true);
            }
        }
    }
}
