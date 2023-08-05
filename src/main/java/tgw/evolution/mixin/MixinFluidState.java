package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchFluidState;

import java.util.Random;

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

    @Overwrite
    public VoxelShape getShape(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getShape_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getShape_(BlockGetter level, int x, int y, int z) {
        return this.getType().getShape_((FluidState) (Object) this, level, x, y, z);
    }

    @Shadow
    public abstract Fluid getType();

    @Overwrite
    public void randomTick(Level level, BlockPos pos, Random random) {
        Evolution.deprecatedMethod();
        this.randomTick_(level, pos.getX(), pos.getY(), pos.getZ(), random);
    }

    @Override
    public void randomTick_(Level level, int x, int y, int z, Random random) {
        this.getType().randomTick_(level, x, y, z, (FluidState) (Object) this, random);
    }
}
