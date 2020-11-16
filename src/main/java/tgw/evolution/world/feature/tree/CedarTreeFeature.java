package tgw.evolution.world.feature.tree;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.blocks.*;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.TreeUtils;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class CedarTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {

    private static final BlockState LOG = EvolutionBlocks.LOG_CEDAR.get().getDefaultState().with(BlockLog.TREE, true);
    private static final BlockState LEAVES = EvolutionBlocks.LEAVES_CEDAR.get().getDefaultState();

    public CedarTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory, boolean notify) {
        super(configFactory, notify);
    }

    @Override
    protected boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos pos, MutableBoundingBox box) {
        int trunkHeight = rand.nextInt(4) + 10;
        int numberOfBranches = trunkHeight - 4 - rand.nextInt(3);
        if (pos.getY() >= 1 && pos.getY() + trunkHeight + 1 <= ((IWorld) worldIn).getWorld().getHeight()) {
            boolean flag = true;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int i1 = pos.getY(); i1 <= pos.getY() + 1 + trunkHeight && flag; ++i1) {
                int j1;
                if (i1 - pos.getY() < 2) {
                    j1 = 0;
                }
                else {
                    j1 = 3;
                }
                for (int k1 = pos.getX() - j1; k1 <= pos.getX() + j1 && flag; ++k1) {
                    for (int l1 = pos.getZ() - j1; l1 <= pos.getZ() + j1 && flag; ++l1) {
                        if (i1 >= 0 && i1 < ((IWorld) worldIn).getWorld().getHeight()) {
                            BlockState iblockstate = ((IBlockReader) worldIn).getBlockState(mutablePos.setPos(k1, i1, l1));
                            if (!iblockstate.isAir((IBlockReader) worldIn, mutablePos) && !(iblockstate.getBlock() instanceof BlockLeaves)) {
                                flag = false;
                            }
                        }
                        else {
                            flag = false;
                        }
                    }
                }
            }
            if (!flag) {
                return false;
            }
            if (BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(pos.down()),
                                             (BlockSapling) EvolutionBlocks.SAPLING_CEDAR.get()) &&
                pos.getY() < ((IWorld) worldIn).getWorld().getHeight() - trunkHeight - 1) {
                TreeUtils.setDirtAt(worldIn, pos.down());
                int branchStart = trunkHeight - numberOfBranches;
                for (int placingTrunks = 0; placingTrunks < trunkHeight; placingTrunks++) {
                    BlockState trunkState = ((IBlockReader) worldIn).getBlockState(pos.up(placingTrunks));
                    if (trunkState.isAir((IBlockReader) worldIn, pos.up(placingTrunks)) || trunkState.getBlock() instanceof BlockLeaves) {
                        this.setLogState(changedBlocks, worldIn, pos.up(placingTrunks), LOG, box);
                    }
                    if (placingTrunks >= branchStart) {
                        mutablePos.setPos(pos).move(Direction.UP, placingTrunks);
                        for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
                            BlockPos leaf = mutablePos.offset(direction);
                            if (((IBlockReader) worldIn).getBlockState(leaf).canBeReplacedByLeaves((IWorldReader) worldIn, leaf)) {
                                this.setBlockState(worldIn, leaf, LEAVES);
                            }
                        }
                    }
                }
                int blocksAtTop = 1 + rand.nextInt(3);
                Direction oldDirection = Direction.DOWN;
                Direction firstDirection = Direction.byHorizontalIndex(rand.nextInt(4));
                for (int placingBranches = branchStart; placingBranches < trunkHeight - blocksAtTop; placingBranches++) {
                    int branchSize = 1;
                    if (trunkHeight - blocksAtTop - 3 > placingBranches) {
                        branchSize += rand.nextInt(3);
                    }
                    else if (trunkHeight - blocksAtTop - 2 > placingBranches) {
                        branchSize += rand.nextInt(2);
                    }
                    Direction secondDirection = Direction.byHorizontalIndex(rand.nextInt(4));
                    while (firstDirection == secondDirection) {
                        secondDirection = Direction.byHorizontalIndex(rand.nextInt(4));
                    }
                    this.makeBranch(worldIn, firstDirection, branchSize, pos, placingBranches, changedBlocks, box);
                    if (firstDirection.getOpposite() != secondDirection && oldDirection != firstDirection.getOpposite() && rand.nextBoolean()) {
                        branchSize = 1 + rand.nextInt(2);
                        this.makeBranch(worldIn, firstDirection.getOpposite(), branchSize, pos, placingBranches, changedBlocks, box);
                    }
                    oldDirection = firstDirection;
                    firstDirection = secondDirection;
                }
                BlockPos lastLeaf = pos.up(trunkHeight);
                if (((IBlockReader) worldIn).getBlockState(lastLeaf).canBeReplacedByLeaves((IWorldReader) worldIn, lastLeaf)) {
                    this.setBlockState(worldIn, lastLeaf, LEAVES);
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private void makeBranch(IWorldGenerationReader worldIn,
                            Direction direction,
                            int branchSize,
                            BlockPos pos,
                            int height,
                            Set<BlockPos> changedBlocks,
                            MutableBoundingBox box) {
        BlockState branchState = ((IBlockReader) worldIn).getBlockState(pos.up(height).offset(direction));
        Direction.Axis axis = direction == Direction.NORTH || direction == Direction.SOUTH ? Direction.Axis.Z : Direction.Axis.X;
        if (branchState.isAir((IBlockReader) worldIn, pos.up(height).offset(direction)) || branchState.getBlock() instanceof BlockLeaves) {
            this.setLogState(changedBlocks, worldIn, pos.up(height).offset(direction), LOG.with(BlockXYZAxis.AXIS, axis), box);
            this.placeBranchLeaves(worldIn, pos, height, direction);
        }
        if (branchSize > 1) {
            branchState = ((IBlockReader) worldIn).getBlockState(pos.up(height + 1).offset(direction, 2));
            if (branchState.isAir((IBlockReader) worldIn, pos.up(height + 1).offset(direction, 2)) || branchState.getBlock() instanceof BlockLeaves) {
                this.setLogState(changedBlocks, worldIn, pos.up(height + 1).offset(direction, 2), LOG.with(BlockXYZAxis.AXIS, axis), box);
                this.placeBranchLeaves(worldIn, pos.up().offset(direction), height, direction);
            }
            if (branchSize > 2) {
                branchState = ((IBlockReader) worldIn).getBlockState(pos.up(height + 2).offset(direction, 3));
                if (branchState.isAir((IBlockReader) worldIn, pos.up(height + 2).offset(direction, 3)) ||
                    branchState.getBlock() instanceof BlockLeaves) {
                    this.setLogState(changedBlocks, worldIn, pos.up(height + 2).offset(direction, 3), LOG.with(BlockXYZAxis.AXIS, axis), box);
                    this.placeBranchLeaves(worldIn, pos.up(2).offset(direction, 2), height, direction);
                }
            }
        }
        BlockPos lastLeaf = pos.up(height + branchSize - 1).offset(direction, branchSize + 2);
        if (((IBlockReader) worldIn).getBlockState(lastLeaf).canBeReplacedByLeaves((IWorldReader) worldIn, lastLeaf)) {
            this.setBlockState(worldIn, lastLeaf, LEAVES);
        }
    }

    private void placeBranchLeaves(IWorldGenerationReader worldIn, BlockPos pos, int height, Direction direction) {
        BlockPos[] leavesPos = {pos.up(height + 1).offset(direction),
                                pos.up(height + 1).offset(direction).offset(direction.rotateAround(Axis.Y)),
                                pos.up(height + 1).offset(direction).offset(direction.rotateAround(Axis.Y).getOpposite()),
                                pos.up(height).offset(direction).offset(direction.rotateAround(Axis.Y)),
                                pos.up(height).offset(direction).offset(direction.rotateAround(Axis.Y), 2),
                                pos.up(height).offset(direction).offset(direction.rotateAround(Axis.Y).getOpposite()),
                                pos.up(height).offset(direction).offset(direction.rotateAround(Axis.Y).getOpposite(), 2),
                                pos.up(height).offset(direction, 2),
                                pos.up(height).offset(direction, 2).offset(direction.rotateAround(Axis.Y)),
                                pos.up(height).offset(direction, 2).offset(direction.rotateAround(Axis.Y).getOpposite()),
                                pos.up(height + 1).offset(direction, 2),};
        for (BlockPos leavesPo : leavesPos) {
            if (((IBlockReader) worldIn).getBlockState(leavesPo).canBeReplacedByLeaves((IWorldReader) worldIn, leavesPo)) {
                this.setBlockState(worldIn, leavesPo, LEAVES);
            }
        }
    }
}
