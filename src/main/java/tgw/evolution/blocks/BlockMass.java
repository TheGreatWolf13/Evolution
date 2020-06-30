package tgw.evolution.blocks;

import net.minecraft.block.BedrockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMass extends Block {

    private final int mass;

    public BlockMass(Properties properties, int mass) {
        super(properties);
        this.mass = mass;
    }

    public static void updateWeight(World worldIn, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (int i = pos.getY() - 1; i >= 0; i--) {
            mutablePos.setY(i);
            BlockState down = worldIn.getBlockState(mutablePos);
            if (BlockUtils.isReplaceable(down)) {
                BlockUtils.scheduleBlockTick(worldIn, mutablePos.up(), 10);
                BlockUtils.scheduleBlockTick(worldIn, mutablePos, 10);
                return;
            }
            if (down.getBlock() instanceof BlockStone || down.getBlock() instanceof BedrockBlock) {
                return;
            }
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isRemote) {
            BlockUtils.scheduleBlockTick(worldIn, pos, 2);
        }
    }

    /**
     * @param state : The current BlockState of the Block
     */
    public int getMass(BlockState state) {
        return this.mass;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (pos.up().equals(fromPos)) {
                updateWeight(worldIn, pos);
            }
        }
    }
}
