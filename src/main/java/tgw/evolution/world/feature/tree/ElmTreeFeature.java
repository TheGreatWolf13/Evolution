//package tgw.evolution.world.feature.tree;
//
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.Direction;
//import net.minecraft.util.Direction.Axis;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.IWorldReader;
//import net.minecraft.world.gen.IWorldGenerationReader;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.BlockLeaves;
//import tgw.evolution.blocks.BlockUtils;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.TreeUtils;
//
//import java.util.Random;
//import java.util.Set;
//import java.util.function.Function;
//
//import static tgw.evolution.init.EvolutionBStates.AXIS;
//import static tgw.evolution.init.EvolutionBStates.TREE;
//
//public class ElmTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static final BlockState LOG = EvolutionBlocks.LOG_ELM.get().getDefaultState().with(TREE, true);
//    private static final BlockState LEAVES = EvolutionBlocks.LEAVES_ELM.get().getDefaultState();
//
//    public ElmTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory, boolean notify) {
//        super(configFactory, notify);
//    }
//
//    private void branchLeaves(BlockPos pos, int height, Direction direction, IWorldGenerationReader worldIn) {
//        BlockPos[] leavesPos = {pos.up(height + 1).offset(direction),
//                                pos.up(height).offset(direction).offset(direction),
//                                pos.up(height).offset(direction).offset(direction.rotateAround(Axis.Y)),
//                                pos.up(height).offset(direction).offset(direction.rotateAround(Axis.Y).rotateAround(Axis.Y)),
//                                pos.up(height).offset(direction).offset(direction.rotateAround(Axis.Y).rotateAround(Axis.Y).rotateAround(Axis.Y)),};
//        for (BlockPos leavesPo : leavesPos) {
//            if (((IBlockReader) worldIn).getBlockState(leavesPo).canBeReplacedByLeaves((IWorldReader) worldIn, leavesPo)) {
//                this.setBlockState(worldIn, leavesPo, LEAVES);
//            }
//        }
//    }
//
//    @Override
//    protected boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos pos, MutableBoundingBox box) {
//        int trunkHeight = rand.nextInt(5) + 12;
//        int numberOfBranchesLower = 1 + rand.nextInt(3);
//        if (pos.getY() >= 1 && pos.getY() + trunkHeight + 1 <= ((IWorld) worldIn).getWorld().getHeight()) {
//            boolean flag = true;
//            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
//            for (int i1 = pos.getY(); i1 <= pos.getY() + 1 + trunkHeight && flag; ++i1) {
//                int j1;
//                if (i1 - pos.getY() < 2) {
//                    j1 = 0;
//                }
//                else {
//                    j1 = 3;
//                }
//                for (int k1 = pos.getX() - j1; k1 <= pos.getX() + j1 && flag; ++k1) {
//                    for (int l1 = pos.getZ() - j1; l1 <= pos.getZ() + j1 && flag; ++l1) {
//                        if (i1 >= 0 && i1 < ((IWorld) worldIn).getWorld().getHeight()) {
//                            BlockState iblockstate = ((IBlockReader) worldIn).getBlockState(blockpos$mutableblockpos.setPos(k1, i1, l1));
//                            if (!iblockstate.isAir((IBlockReader) worldIn, blockpos$mutableblockpos) &&
//                                !(iblockstate.getBlock() instanceof BlockLeaves)) {
//                                flag = false;
//                            }
//                        }
//                        else {
//                            flag = false;
//                        }
//                    }
//                }
//            }
//            if (!flag) {
//                return false;
//            }
//            if (BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(pos.down()), EvolutionBlocks.SAPLING_ELM.get()) &&
//                pos.getY() < ((IWorld) worldIn).getWorld().getHeight() - trunkHeight - 1) {
//                TreeUtils.setDirtAt(worldIn, pos.down());
//                BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
//                for (int placingTrunks = 0; placingTrunks < trunkHeight; placingTrunks++) {
//                    BlockState trunkState = ((IBlockReader) worldIn).getBlockState(pos.up(placingTrunks));
//                    if (placingTrunks < trunkHeight / 2) {
//                        if (trunkState.isAir((IBlockReader) worldIn, pos.up(placingTrunks)) || trunkState.getBlock() instanceof BlockLeaves) {
//                            this.setLogState(changedBlocks, worldIn, pos.up(placingTrunks), LOG, box);
//                        }
//                    }
//                    else {
//                        for (int i = -2; i <= 2; i++) {
//                            for (int j = -2; j <= 2; j++) {
//                                leafPos.setPos(pos).move(Direction.UP, placingTrunks).move(i, 0, j);
//                                if (Math.abs(i) != 2 || Math.abs(j) != 2) {
//                                    if (((IBlockReader) worldIn).getBlockState(leafPos).canBeReplacedByLeaves((IWorldReader) worldIn, leafPos)) {
//                                        this.setBlockState(worldIn, leafPos, LEAVES);
//                                    }
//                                    if (((IBlockReader) worldIn).getBlockState(leafPos.up())
//                                                                .canBeReplacedByLeaves((IWorldReader) worldIn, leafPos.up())) {
//                                        this.setBlockState(worldIn, leafPos.up(), LEAVES);
//                                    }
//                                }
//                            }
//                        }
//                        BlockPos trunkPos = pos.up(placingTrunks);
//                        Direction trunkFacing = Direction.byHorizontalIndex(rand.nextInt(4));
//                        int originalSize = trunkHeight - placingTrunks;
//                        for (int i = 0; i < 4; i++) {
//                            Direction trunkFacingSec = Direction.byHorizontalIndex(rand.nextInt(4));
//                            while (trunkFacing.getOpposite() == trunkFacingSec) {
//                                trunkFacingSec = Direction.byHorizontalIndex(rand.nextInt(4));
//                            }
//                            int reduction = rand.nextInt(3);
//                            int size = originalSize - reduction;
//                            int start = rand.nextInt(3);
//                            for (int j = 0; j < size; j++) {
//                                trunkPos = pos.up(placingTrunks + j).offset(trunkFacing);
//                                if (j > start) {
//                                    trunkPos = trunkPos.offset(trunkFacingSec);
//                                    if (trunkFacing != trunkFacingSec) {
//                                        trunkPos = trunkPos.offset(trunkFacing);
//                                    }
//                                }
//                                trunkState = ((IBlockReader) worldIn).getBlockState(trunkPos);
//                                if (trunkState.isAir((IBlockReader) worldIn, trunkPos) || trunkState.getBlock() instanceof BlockLeaves) {
//                                    this.setLogState(changedBlocks, worldIn, trunkPos, LOG, box);
//                                    if (j < 2) {
//                                        if (((IBlockReader) worldIn).getBlockState(trunkPos.north())
//                                                                    .canBeReplacedByLeaves((IWorldReader) worldIn, trunkPos.north())) {
//                                            this.setBlockState(worldIn, trunkPos.north(), LEAVES);
//                                        }
//                                        if (((IBlockReader) worldIn).getBlockState(trunkPos.south())
//                                                                    .canBeReplacedByLeaves((IWorldReader) worldIn, trunkPos.south())) {
//                                            this.setBlockState(worldIn, trunkPos.south(), LEAVES);
//                                        }
//                                        if (((IBlockReader) worldIn).getBlockState(trunkPos.west())
//                                                                    .canBeReplacedByLeaves((IWorldReader) worldIn, trunkPos.west())) {
//                                            this.setBlockState(worldIn, trunkPos.west(), LEAVES);
//                                        }
//                                        if (((IBlockReader) worldIn).getBlockState(trunkPos.east())
//                                                                    .canBeReplacedByLeaves((IWorldReader) worldIn, trunkPos.east())) {
//                                            this.setBlockState(worldIn, trunkPos.east(), LEAVES);
//                                        }
//                                    }
//                                }
//                            }
//                            this.placeLeaves(worldIn, trunkPos);
//                            trunkFacing = Direction.byHorizontalIndex(trunkFacing.getHorizontalIndex() + 1);
//                        }
//                        break;
//                    }
//                }
//                Direction branchFacingF = Direction.byHorizontalIndex(rand.nextInt(4));
//                int branchLowStart = trunkHeight / 4 - 1;
//                BlockState branchState = ((IBlockReader) worldIn).getBlockState(pos.up(branchLowStart).offset(branchFacingF));
//                Direction.Axis axis = branchFacingF == Direction.NORTH || branchFacingF == Direction.SOUTH ? Direction.Axis.Z : Direction.Axis.X;
//                if (numberOfBranchesLower > 1) {
//                    Direction branchFacingS = Direction.byHorizontalIndex(rand.nextInt(4));
//                    while (branchFacingF == branchFacingS) {
//                        branchFacingS = Direction.byHorizontalIndex(rand.nextInt(4));
//                    }
//                    if (branchState.isAir((IBlockReader) worldIn, pos.up(branchLowStart).offset(branchFacingF)) ||
//                        branchState.getBlock() instanceof BlockLeaves) {
//                        this.setLogState(changedBlocks, worldIn, pos.up(branchLowStart).offset(branchFacingF), LOG.with(AXIS, axis), box);
//                        this.branchLeaves(pos, branchLowStart, branchFacingF, worldIn);
//                    }
//                    branchState = ((IBlockReader) worldIn).getBlockState(pos.up(branchLowStart + 1).offset(branchFacingS));
//                    axis = branchFacingS == Direction.NORTH || branchFacingS == Direction.SOUTH ? Direction.Axis.Z : Direction.Axis.X;
//                    if (branchState.isAir((IBlockReader) worldIn, pos.up(branchLowStart + 1).offset(branchFacingS)) ||
//                        branchState.getBlock() instanceof BlockLeaves) {
//                        this.setLogState(changedBlocks, worldIn, pos.up(branchLowStart + 1).offset(branchFacingS), LOG.with(AXIS, axis), box);
//                        this.branchLeaves(pos, branchLowStart + 1, branchFacingS, worldIn);
//                    }
//                    if (numberOfBranchesLower > 2) {
//                        Direction branchFacingT = Direction.byHorizontalIndex(rand.nextInt(4));
//                        while (branchFacingT == branchFacingS || branchFacingT == branchFacingF) {
//                            branchFacingT = Direction.byHorizontalIndex(rand.nextInt(4));
//                        }
//                        axis = branchFacingT == Direction.NORTH || branchFacingT == Direction.SOUTH ? Direction.Axis.Z : Direction.Axis.X;
//                        branchState = ((IBlockReader) worldIn).getBlockState(pos.up(branchLowStart - 1).offset(branchFacingT));
//                        if (branchState.isAir((IBlockReader) worldIn, pos.up(branchLowStart - 1).offset(branchFacingT)) ||
//                            branchState.getBlock() instanceof BlockLeaves) {
//                            this.setLogState(changedBlocks, worldIn, pos.up(branchLowStart - 1).offset(branchFacingT), LOG.with(AXIS, axis), box);
//                            this.branchLeaves(pos, branchLowStart - 1, branchFacingT, worldIn);
//                        }
//                    }
//                }
//                else {
//                    if (branchState.isAir((IBlockReader) worldIn, pos.up(branchLowStart).offset(branchFacingF)) ||
//                        branchState.getBlock() instanceof BlockLeaves) {
//                        this.setLogState(changedBlocks, worldIn, pos.up(branchLowStart).offset(branchFacingF), LOG.with(AXIS, axis), box);
//                        this.branchLeaves(pos, branchLowStart, branchFacingF, worldIn);
//                    }
//                }
//                return true;
//            }
//            return false;
//        }
//        return false;
//    }
//
//    private void placeLeaves(IWorldGenerationReader worldIn, BlockPos pos) {
//        BlockPos[] leavesPos = {pos.up(),
//                                pos.west(),
//                                pos.east(),
//                                pos.north(),
//                                pos.south(),
//                                pos.west().down(),
//                                pos.east().down(),
//                                pos.north().down(),
//                                pos.south().down(),
//                                pos.west().down().north(),
//                                pos.east().down().south(),
//                                pos.north().down().east(),
//                                pos.south().down().west(),};
//        for (BlockPos leavesPo : leavesPos) {
//            if (((IBlockReader) worldIn).getBlockState(leavesPo).canBeReplacedByLeaves((IWorldReader) worldIn, leavesPo)) {
//                this.setBlockState(worldIn, leavesPo, LEAVES);
//            }
//        }
//    }
//}
