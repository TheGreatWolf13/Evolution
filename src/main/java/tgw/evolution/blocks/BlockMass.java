package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.EvolutionDamage;

public class BlockMass extends BlockGeneric {

    private final int mass;

    public BlockMass(Properties properties, int mass) {
        super(properties);
        this.mass = mass;
    }

    public static void updateWeight(Level level, BlockPos pos) {
        MutableBlockPos mutablePos = new MutableBlockPos();
        mutablePos.set(pos);
        for (int i = pos.getY() - 1; i >= 0; i--) {
            mutablePos.setY(i);
            BlockState down = level.getBlockState(mutablePos);
            if (BlockUtils.isReplaceable(down)) {
                BlockUtils.scheduleBlockTick(level, mutablePos.above(), 10);
                BlockUtils.scheduleBlockTick(level, mutablePos, 10);
                return;
            }
            if (down.getBlock() instanceof BlockStone || down.getBlock() == Blocks.BEDROCK) {
                return;
            }
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance,
                               this instanceof ICollisionBlock collisionBlock ? collisionBlock.getSlowdownTop(state) : 1.0f,
                               EvolutionDamage.FALL);
    }

    public int getBaseMass() {
        return this.mass;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.85F;
    }

    public int getMass(Level level, BlockPos pos, BlockState state) {
        return this.getMass(state);
    }

    public int getMass(BlockState state) {
        return this.mass;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (pos.above().equals(fromPos)) {
                updateWeight(level, pos);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockUtils.scheduleBlockTick(level, pos, 2);
        }
    }
}
