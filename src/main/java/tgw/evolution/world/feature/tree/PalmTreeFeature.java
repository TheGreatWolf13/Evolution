//package tgw.evolution.world.feature.tree;
//
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.IWorldReader;
//import net.minecraft.world.gen.IWorldGenerationReader;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.BlockSapling;
//import tgw.evolution.blocks.util.BlockUtils;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.TreeUtils;
//
//import java.util.Random;
//import java.util.Set;
//import java.util.function.Function;
//
//import static tgw.evolution.init.EvolutionBStates.TREE;
//
//public class PalmTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static final BlockState TRUNK = EvolutionBlocks.LOG_PALM.get().getDefaultState().with(TREE, true);
//    private static final BlockState LEAF = EvolutionBlocks.LEAVES_PALM.get().getDefaultState();
//
//    public PalmTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean doBlockNotify) {
//        super(config, doBlockNotify);
//    }
//
//    private void makeCanopy(IWorldGenerationReader worldIn, BlockPos pos) {
//        for (int i = -2; i <= 2; i++) {
//            for (int j = -2; j <= 2; j++) {
//                this.placeLeafAt(worldIn, pos.add(i, 0, j));
//            }
//        }
//        BlockPos[] leavesPos = {pos.up(),
//                                pos.up().north(),
//                                pos.up().south(),
//                                pos.up().west(),
//                                pos.up().east(),
//                                pos.north(3),
//                                pos.north(3).down(),
//                                pos.north(4).down(),
//                                pos.south(3),
//                                pos.south(3).down(),
//                                pos.south(4).down(),
//                                pos.west(3),
//                                pos.west(3).down(),
//                                pos.west(4).down(),
//                                pos.east(3),
//                                pos.east(3).down(),
//                                pos.east(4).down(),
//                                pos.north(2).west(2).down(),
//                                pos.north(2).east(2).down(),
//                                pos.south(2).west(2).down(),
//                                pos.south(2).east(2).down(),};
//        for (BlockPos leavesPo : leavesPos) {
//            this.placeLeafAt(worldIn, leavesPo);
//        }
//    }
//
//    @Override
//    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos pos, MutableBoundingBox box) {
//        int numLogs = 9 + rand.nextInt(5);
//        //testing valid space
//        //tests if within world height limits
//        if (pos.getY() >= 1 && pos.getY() + numLogs + 1 <= ((IWorld) worldIn).getWorld().getHeight()) {
//            boolean validSpace = true;
//            BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
//            for (int absY = pos.getY(); absY <= pos.getY() + 1 + numLogs; ++absY) {
//                int testPosModifier = 1;
//                if (absY == pos.getY()) {
//                    testPosModifier = 0;
//                }
//                if (absY >= pos.getY() + 1 + numLogs - 2) {
//                    testPosModifier = 2;
//                }
//                for (int testPosX = pos.getX() - testPosModifier; testPosX <= pos.getX() + testPosModifier && validSpace; ++testPosX) {
//                    for (int testPosZ = pos.getZ() - testPosModifier; testPosZ <= pos.getZ() + testPosModifier && validSpace; ++testPosZ) {
//                        if (absY >= 0 && absY < ((IWorld) worldIn).getWorld().getHeight()) {
//                            if (!BlockSapling.canGrowInto(worldIn, testPos.setPos(testPosX, absY, testPosZ))) {
//                                validSpace = false;
//                            }
//                        }
//                        else {
//                            validSpace = false;
//                        }
//                    }
//                }
//            }
//            if (!validSpace) {
//                return false;
//            }
//            //placing trunks
//            boolean isSoil = BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(pos.down()), EvolutionBlocks.SAPLING_PALM.get());
//            if (isSoil && pos.getY() < ((IWorld) worldIn).getWorld().getHeight() - numLogs - 1) {
//                TreeUtils.setDirtAt(worldIn, pos.down());
//                Direction inclination = Direction.Plane.HORIZONTAL.random(rand);
//                int first = numLogs / 3 + rand.nextInt(2);
//                int second = rand.nextInt(numLogs / 3 + 2);
//                BlockPos trunkPos = pos;
//                for (int placingTrunks = 0; placingTrunks < numLogs; placingTrunks++) {
//                    trunkPos = pos.up(placingTrunks);
//                    if (placingTrunks > first) {
//                        trunkPos = trunkPos.offset(inclination);
//                    }
//                    if (placingTrunks > second) {
//                        trunkPos = trunkPos.offset(inclination);
//                    }
//                    this.placeTrunkAt(changedBlocks, worldIn, trunkPos, box);
//                }
//                this.makeCanopy(worldIn, trunkPos);
//                this.placeTrunkAt(changedBlocks, worldIn, pos.offset(inclination.getOpposite()), box);
//                return true;
//            }
//            return false;
//        }
//        return false;
//    }
//
//    private void placeLeafAt(IWorldGenerationReader worldIn, BlockPos pos) {
//        BlockState iblockstate = ((IBlockReader) worldIn).getBlockState(pos);
//        if (iblockstate.canBeReplacedByLeaves((IWorldReader) worldIn, pos)) {
//            this.setBlockState(worldIn, pos, LEAF);
//        }
//    }
//
//    private void placeTrunkAt(Set<BlockPos> setBlockPos, IWorldGenerationReader iWorld, BlockPos pos, MutableBoundingBox box) {
//        this.setLogState(setBlockPos, iWorld, pos, TRUNK, box);
//    }
//}