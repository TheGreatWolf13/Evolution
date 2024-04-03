package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.math.MathHelper;

import java.util.Random;
import java.util.function.Consumer;

public abstract class BlockGenericSpreadable extends BlockPhysics {

    protected static final FastRandom RANDOM = new FastRandom();

    public BlockGenericSpreadable(Block.Properties builder) {
        super(builder.randomTicks());
    }

    public abstract BlockState deadBlockState();

    @Override
    public void dropLoot(BlockState state, ServerLevel level, int x, int y, int z, ItemStack tool, @Nullable BlockEntity tile, @Nullable Entity entity, Random random, Consumer<ItemStack> consumer) {
        this.deadBlockState().dropLoot(level, x, y, z, tool, tile, entity, random, consumer);
    }

    @Override
    public BlockState getStateForPhysicsChange(BlockState state) {
        return this.deadBlockState();
    }

    @Override
    public void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean isMoving) {
        super.neighborChanged_(state, level, x, y, z, oldBlock, fromX, fromY, fromZ, isMoving);
        if (!level.isClientSide) {
            if (x == fromX && z == fromZ && y + 1 == fromY) {
                if (level.getBlockState_(x, y + 1, z).isFaceSturdy_(level, x, y + 1, z, Direction.DOWN)) {
                    level.setBlockAndUpdate_(x, y, z, this.deadBlockState());
                }
            }
        }
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        int skyLight = level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue_(BlockPos.asLong(x, y + 1, z));
        if (skyLight <= 4) {
            //Cannot survive
            level.setBlockAndUpdate_(x, y, z, this.deadBlockState());
            return;
        }
        if (skyLight >= 12) {
            //Try to spread
            int allowedDirections = 0;
            int count = 0;
            for (Direction dir : DirectionUtil.HORIZ_NESW) {
                int xOffset = x + dir.getStepX();
                int zOffset = z + dir.getStepZ();
                BlockState stateAtSide = level.getBlockState_(xOffset, y, zOffset);
                if (stateAtSide.getBlock() instanceof IGrassSpreadable spreadable) {
                    if (spreadable.canReceiveGrass(level, stateAtSide, xOffset, y, zOffset)) {
                        allowedDirections |= 1 << DirectionUtil.horizontalIndex(dir);
                        ++count;
                    }
                    else {
                        BlockState stateAtSideUp = level.getBlockState_(xOffset, y + 1, zOffset);
                        if (stateAtSideUp.getBlock() instanceof IGrassSpreadable s) {
                            if (s.canReceiveGrass(level, stateAtSideUp, xOffset, y + 1, zOffset)) {
                                allowedDirections |= 1 << DirectionUtil.horizontalIndex(dir) + 8;
                                ++count;
                            }
                        }
                    }
                }
                else {
                    BlockState stateAtSideDown = level.getBlockState_(xOffset, y - 1, zOffset);
                    if (stateAtSideDown.getBlock() instanceof IGrassSpreadable spreadable) {
                        if (spreadable.canReceiveGrass(level, stateAtSideDown, xOffset, y - 1, zOffset)) {
                            allowedDirections |= 1 << DirectionUtil.horizontalIndex(dir) + 4;
                            ++count;
                        }
                    }
                    BlockState stateAtSideUp = level.getBlockState_(xOffset, y + 1, zOffset);
                    if (stateAtSideUp.getBlock() instanceof IGrassSpreadable spreadable) {
                        if (spreadable.canReceiveGrass(level, stateAtSideUp, xOffset, y + 1, zOffset)) {
                            allowedDirections |= 1 << DirectionUtil.horizontalIndex(dir) + 8;
                            ++count;
                        }
                    }
                }
            }
            if (count > 0) {
                int bit = MathHelper.getRandomSetBitFrom(random, allowedDirections, count);
                int xOffset = x;
                int yOffset = y;
                int zOffset = z;
                if (bit >= 8) {
                    ++yOffset;
                    bit -= 8;
                }
                else if (bit >= 4) {
                    --yOffset;
                    bit -= 4;
                }
                Direction dir = DirectionUtil.HORIZ_NESW[bit];
                xOffset += dir.getStepX();
                zOffset += dir.getStepZ();
                BlockState stateToSpread = level.getBlockState_(xOffset, yOffset, zOffset);
                IGrassSpreadable spreadable = (IGrassSpreadable) stateToSpread.getBlock();
                int allowanceCost = spreadable.getAllowanceCost(stateToSpread);
                if (level.getChunkAt_(xOffset, zOffset).getChunkStorage().getAllowance().ifHasConsumeGrassAllowance(allowanceCost)) {
                    level.setBlockAndUpdate_(xOffset, yOffset, zOffset, spreadable.getGrass().defaultBlockState());
                }
            }
            else {
                this.growTallGrass(level, x, y, z);
            }
        }
    }

    protected abstract float getGrassDensity();

    protected abstract int getTallGrassAllowanceCost();

    protected void growTallGrass(ServerLevel level, int x, int y, int z) {
        BlockState stateAbove = level.getBlockState_(x, y + 1, z);
        if (stateAbove.isAir()) {
            if (RANDOM.setSeedAndReturn(Mth.getSeed(x, y, z)).nextFloat() < this.getGrassDensity()) {
                if (level.getChunkAt_(x, z).getChunkStorage().getAllowance().ifHasConsumeTallGrassAllowance(this.getTallGrassAllowanceCost())) {
                    level.setBlockAndUpdate_(x, y + 1, z, EvolutionBlocks.SHORT_GRASS.defaultBlockState());
                }
            }
        }
        else if (stateAbove.getBlock() == EvolutionBlocks.SHORT_GRASS) {
            if (RANDOM.setSeedAndReturn(Mth.getSeed(x, y + 1, z)).nextFloat() < this.getGrassDensity()) {
                if (level.getBlockState_(x, y + 2, z).isAir()) {
                    if (level.getChunkAt_(x, z).getChunkStorage().getAllowance().ifHasConsumeTallGrassAllowance(2 * this.getTallGrassAllowanceCost())) {
                        level.setBlockAndUpdate_(x, y + 1, z, EvolutionBlocks.TALL_GRASS.defaultBlockState());
                        level.setBlockAndUpdate_(x, y + 2, z, EvolutionBlocks.TALL_GRASS.defaultBlockState().setValue(EvolutionBStates.HALF, DoubleBlockHalf.UPPER));
                    }
                }
            }
        }
    }
}