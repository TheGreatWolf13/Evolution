package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.physics.Fluid;

import java.util.Random;

@Mixin(LavaFluid.class)
public abstract class Mixin_M_LavaFluid extends FlowingFluid {

    @Override
    public Fluid fluid() {
        return Fluid.LAVA;
    }

    @Override
    public double getFlowStrength(DimensionType type) {
        return type.ultraWarm() ? 0.007 : 0.002_333_333_333_333_333_5;
    }

    @Shadow
    protected abstract boolean hasFlammableNeighbours(LevelReader levelReader, BlockPos blockPos);

    @Shadow
    protected abstract boolean isFlammable(LevelReader levelReader, BlockPos blockPos);

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(Level level, int x, int y, int z, FluidState fluidState, Random random) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            int i = random.nextInt(3);
            if (i > 0) {
                int offX = x;
                int offY = y;
                int offZ = z;
                for (int j = 0; j < i; ++j) {
                    offX += random.nextInt(3) - 1;
                    offY += 1;
                    offZ += random.nextInt(3) - 1;
                    if (!level.isLoaded_(offX, offY, offZ)) {
                        return;
                    }
                    BlockState stateAtOff = level.getBlockState_(offX, offY, offZ);
                    if (stateAtOff.isAir()) {
                        BlockPos offPos = new BlockPos(offX, offY, offZ);
                        if (this.hasFlammableNeighbours(level, offPos)) {
                            level.setBlockAndUpdate_(offX, offY, offZ, BaseFireBlock.getState(level, offPos));
                            return;
                        }
                    }
                    else if (stateAtOff.getMaterial().blocksMotion()) {
                        return;
                    }
                }
            }
            else {
                for (int k = 0; k < 3; ++k) {
                    int offX = x + random.nextInt(3) - 1;
                    int offZ = z + random.nextInt(3) - 1;
                    if (!level.isLoaded_(offX, y, offZ)) {
                        return;
                    }
                    if (level.isEmptyBlock_(offX, y + 1, offZ)) {
                        BlockPos offPos = new BlockPos(offX, y, offZ);
                        if (this.isFlammable(level, offPos)) {
                            level.setBlockAndUpdate_(offX, y + 1, offZ, BaseFireBlock.getState(level, offPos));
                        }
                    }
                }
            }
        }
    }
}
