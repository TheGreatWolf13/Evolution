package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMass extends BlockGeneric {

    private final int mass;

    public BlockMass(Properties properties, int mass) {
        super(properties);
        this.mass = mass;
    }

    public static void updateWeight(World world, BlockPos pos) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        mutablePos.set(pos);
        for (int i = pos.getY() - 1; i >= 0; i--) {
            mutablePos.setY(i);
            BlockState down = world.getBlockState(mutablePos);
            if (BlockUtils.isReplaceable(down)) {
                BlockUtils.scheduleBlockTick(world, mutablePos.above(), 10);
                BlockUtils.scheduleBlockTick(world, mutablePos, 10);
                return;
            }
            if (down.getBlock() instanceof BlockStone || down.getBlock() == Blocks.BEDROCK) {
                return;
            }
        }
    }

    @Override
    public void fallOn(World world, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance,
                               this instanceof ICollisionBlock ? ((ICollisionBlock) this).getSlowdownTop(world.getBlockState(pos)) : 1.0f);
    }

    public int getBaseMass() {
        return this.mass;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.85F;
    }

    public int getMass(World world, BlockPos pos, BlockState state) {
        return this.getMass(state);
    }

    public int getMass(BlockState state) {
        return this.mass;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (pos.above().equals(fromPos)) {
                updateWeight(world, pos);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClientSide) {
            BlockUtils.scheduleBlockTick(world, pos, 2);
        }
    }
}
