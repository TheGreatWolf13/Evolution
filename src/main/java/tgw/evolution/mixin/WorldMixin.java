package tgw.evolution.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.blocks.IFluidLoggable;

@Mixin(World.class)
public abstract class WorldMixin extends CapabilityProvider<World> {

    public WorldMixin(Class<World> baseClass) {
        super(baseClass);
    }

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Inject(method = "removeBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onRemoveBlock(BlockPos pos, boolean isMoving, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = this.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            if (((IFluidLoggable) block).remove((World) (Object) this, pos, state)) {
                cir.setReturnValue(true);
            }
        }
    }
}
