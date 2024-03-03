package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.physics.Fluid;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(WaterFluid.class)
public abstract class MixinWaterFluid extends FlowingFluid {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(Level level, int x, int y, int z, FluidState state, RandomGenerator random) {
        if (!state.isSource() && !state.getValue(FALLING)) {
            if (random.nextInt(64) == 0) {
                level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
            }
        }
        else if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.UNDERWATER, x + random.nextDouble(), y + random.nextDouble(), z + random.nextDouble(), 0, 0, 0);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        Evolution.deprecatedMethod();
        this.beforeDestroyingBlock_(level, pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public void beforeDestroyingBlock_(LevelAccessor level, int x, int y, int z, BlockState state) {
        BlockEntity tile = state.hasBlockEntity() ? level.getBlockEntity_(x, y, z) : null;
        BlockUtils.dropResources(state, level, x, y, z, tile, null, ItemStack.EMPTY);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.material.Fluid fluid, Direction direction) {
        Evolution.deprecatedMethod();
        return this.canBeReplacedWith_(state, level, pos.getX(), pos.getY(), pos.getZ(), fluid, direction);
    }

    @Override
    public boolean canBeReplacedWith_(FluidState state, BlockGetter level, int x, int y, int z, net.minecraft.world.level.material.Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
    }

    @Override
    public Fluid fluid() {
        return Fluid.WATER;
    }

    @Override
    public double getFlowStrength(DimensionType type) {
        return 0.014;
    }
}
