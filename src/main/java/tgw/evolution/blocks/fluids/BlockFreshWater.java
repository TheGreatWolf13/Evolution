package tgw.evolution.blocks.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

public class BlockFreshWater extends FlowingFluidBlock {

    public BlockFreshWater() {
        super(EvolutionFluids.FRESH_WATER_FLOWING,
              Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(HarvestLevel.UNBREAKABLE).noDrops());
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        Block newBlock = newState.getBlock();
        if (newBlock == this || newBlock == Blocks.AIR) {
            return;
        }
        int currentLevel = state.getFluidState().getLevel();
        if (currentLevel == 0) {
            return;
        }
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        DirectionList list = new DirectionList();
        list.fillHorizontal();
        while (!list.isEmpty()) {
            mutablePos.setPos(pos).move(list.getRandomAndRemove(MathHelper.RANDOM));
            BlockState stateAtPos = worldIn.getBlockState(mutablePos);
            if (BlockUtils.canBeReplacedByWater(stateAtPos)) {
                if (stateAtPos.getBlock() == this) {
                    int levelAlreadyAtPos = stateAtPos.getFluidState().getLevel();
                    if (levelAlreadyAtPos == 8) {
                        continue;
                    }
                    int levelForReplacement = Math.min(currentLevel + levelAlreadyAtPos, 8);
                    worldIn.setBlockState(mutablePos, FluidFreshWater.getFlowingState(levelForReplacement, false).getBlockState());
                    currentLevel = currentLevel - levelForReplacement + levelAlreadyAtPos;
                    if (currentLevel == 0) {
                        return;
                    }
                }
                else {
                    if (stateAtPos.getBlock() instanceof IReplaceable) {
                        ((IReplaceable) stateAtPos.getBlock()).onReplaced(stateAtPos, worldIn, mutablePos);
                    }
                    worldIn.setBlockState(mutablePos, FluidFreshWater.getFlowingState(currentLevel, false).getBlockState());
                    return;
                }
            }
        }
        mutablePos.setPos(pos);
        for (int y = pos.getY() + 1; y < 256; y++) {
            mutablePos.setY(y);
            BlockState stateAtPos = worldIn.getBlockState(mutablePos);
            if (BlockUtils.canBeReplacedByWater(stateAtPos)) {
                Block blockAtPos = stateAtPos.getBlock();
                if (blockAtPos == this) {
                    int levelAtPos = stateAtPos.getFluidState().getLevel();
                    if (levelAtPos == 8) {
                        continue;
                    }
                    int levelForReplacement = Math.min(currentLevel + levelAtPos, 8);
                    worldIn.setBlockState(mutablePos, FluidFreshWater.getFlowingState(levelForReplacement, false).getBlockState());
                    currentLevel = currentLevel - levelForReplacement + levelAtPos;
                    if (currentLevel == 0) {
                        return;
                    }
                }
                else {
                    if (blockAtPos instanceof IReplaceable) {
                        ((IReplaceable) blockAtPos).onReplaced(stateAtPos, worldIn, mutablePos);
                    }
                    worldIn.setBlockState(mutablePos, FluidFreshWater.getFlowingState(currentLevel, false).getBlockState());
                    return;
                }
            }
            ChunkStorageCapability.add(worldIn.getChunkAt(pos), EnumStorage.WATER, currentLevel);
            return;
        }
    }
}
