package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.blocks.IFluidLoggable;
import tgw.evolution.patches.ILevelPatch;

@Mixin(Level.class)
public abstract class LevelMixin extends CapabilityProvider<Level> implements ILevelPatch {

    @Shadow
    protected int randValue;

    public LevelMixin(Class<Level> baseClass) {
        super(baseClass);
    }

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Override
    public void getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out) {
        this.randValue = this.randValue * 3 + 0x3c6e_f35f;
        int rand = this.randValue >> 2;
        out.set(x + (rand & 15), y + (rand >> 16 & mask), z + (rand >> 8 & 15));
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
