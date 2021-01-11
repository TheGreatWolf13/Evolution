package tgw.evolution.blocks;

import net.minecraft.block.BedrockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMass extends Block {

    private final int mass;

    public BlockMass(Properties properties, int mass) {
        super(properties);
        this.mass = mass;
    }

    public static void updateWeight(World world, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (int i = pos.getY() - 1; i >= 0; i--) {
            mutablePos.setY(i);
            BlockState down = world.getBlockState(mutablePos);
            if (BlockUtils.isReplaceable(down)) {
                BlockUtils.scheduleBlockTick(world, mutablePos.up(), 10);
                BlockUtils.scheduleBlockTick(world, mutablePos, 10);
                return;
            }
            if (down.getBlock() instanceof BlockStone || down.getBlock() instanceof BedrockBlock) {
                return;
            }
        }
    }

    public int getBaseMass() {
        return this.mass;
    }

    public int getMass(BlockState state) {
        return this.mass;
    }

    public int getMass(World world, BlockPos pos, BlockState state) {
        return this.getMass(state);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (pos.up().equals(fromPos)) {
                updateWeight(world, pos);
            }
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isRemote) {
            BlockUtils.scheduleBlockTick(world, pos, 2);
        }
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        entity.fall(fallDistance, this instanceof ISoftBlock ? ((ISoftBlock) this).getSlowdownTop(world.getBlockState(pos)) : 1.0f);
    }
}
