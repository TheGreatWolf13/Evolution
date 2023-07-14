package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchFluidState;

@Mixin(FluidState.class)
public abstract class MixinFluidState extends StateHolder<Fluid, FluidState> implements PatchFluidState {

    public MixinFluidState(Fluid object,
                           ImmutableMap<Property<?>, Comparable<?>> immutableMap,
                           MapCodec<FluidState> mapCodec) {
        super(object, immutableMap, mapCodec);
    }

    @Overwrite
    public float getHeight(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getHeight_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getHeight_(BlockGetter level, int x, int y, int z) {
        return this.getType().getHeight_((FluidState) (Object) this, level, x, y, z);
    }

    @Shadow
    public abstract Fluid getType();
}
