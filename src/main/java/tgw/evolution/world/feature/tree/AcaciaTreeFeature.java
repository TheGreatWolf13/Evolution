//package tgw.evolution.world.feature.tree;
//
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.BlockPos.MutableBlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.IWorldReader;
//import net.minecraft.world.gen.IWorldGenerationReader;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.BlockLeaves;
//import tgw.evolution.blocks.BlockSapling;
//import tgw.evolution.blocks.util.BlockUtils;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.OriginMutableBlockPos;
//import tgw.evolution.util.TreeUtils;
//
//import java.util.Random;
//import java.util.Set;
//import java.util.function.Function;
//
//import static tgw.evolution.init.EvolutionBStates.TREE;
//
//public class AcaciaTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static final BlockState TRUNK = EvolutionBlocks.LOG_ACACIA.get().getDefaultState().with(TREE, true);
//    private static final BlockState LEAF = EvolutionBlocks.LEAVES_ACACIA.get().getDefaultState();
//
//    public AcaciaTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory, boolean doBlockNotify) {
//        super(configFactory, doBlockNotify);
//    }
//
//    @Override
//    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos position, MutableBoundingBox box) {
//        int numLogs = rand.nextInt(3) + rand.nextInt(3) + 6;
//        //testing valid space
//        //tests if within world height limits
//        if (position.getY() >= 1 && position.getY() + numLogs + 1 <= ((IWorld) worldIn).getWorld().getHeight()) {
//            boolean validSpace = true;
//            MutableBlockPos testPos = new MutableBlockPos();
//            for (int absY = position.getY(); absY <= position.getY() + 1 + numLogs; ++absY) {
//                int testPosModifier = 1;
//                if (absY == position.getY()) {
//                    testPosModifier = 0;
//                }
//                if (absY >= position.getY() + 1 + numLogs - 2) {
//                    testPosModifier = 2;
//                }
//                for (int testPosX = position.getX() - testPosModifier; testPosX <= position.getX() + testPosModifier && validSpace; ++testPosX) {
//                    for (int testPosZ = position.getZ() - testPosModifier; testPosZ <= position.getZ() + testPosModifier && validSpace;
//                    ++testPosZ) {
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
//            boolean isSoil = BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(position.down()),
//                                                          EvolutionBlocks.SAPLING_ACACIA.get());
//            if (isSoil && position.getY() < ((IWorld) worldIn).getWorld().getHeight() - numLogs - 1) {
//                TreeUtils.setDirtAt(worldIn, position.down());
//                Direction topInclination = Direction.Plane.HORIZONTAL.random(rand);
//                int topInclinationStartHeight = numLogs - rand.nextInt(4) - 1;
//                int inclinationLimit = 3 - rand.nextInt(3);
//                int trunkPosX = position.getX();
//                int trunkPosZ = position.getZ();
//                int lastTrunkY = 0;
//                MutableBlockPos trunkPos = new MutableBlockPos();
//                for (int placingTrunks = 0; placingTrunks < numLogs; ++placingTrunks) {
//                    int trunkPosY = position.getY() + placingTrunks;
//                    if (placingTrunks >= topInclinationStartHeight && inclinationLimit > 0) {
//                        trunkPosX += topInclination.getXOffset();
//                        trunkPosZ += topInclination.getZOffset();
//                        --inclinationLimit;
//                    }
//                    trunkPos.setPos(trunkPosX, trunkPosY, trunkPosZ);
//                    BlockState stateTrunk = ((IBlockReader) worldIn).getBlockState(trunkPos);
//                    if (stateTrunk.isAir((IBlockReader) worldIn, trunkPos) || stateTrunk.getBlock() instanceof BlockLeaves) {
//                        this.placeTrunkAt(changedBlocks, worldIn, trunkPos, box);
//                        lastTrunkY = trunkPosY;
//                    }
//                }
//                //Placing leaves in a 5 x 5 without corners
//                OriginMutableBlockPos leafPos = new OriginMutableBlockPos(trunkPosX, lastTrunkY, trunkPosZ);
//                for (int leafPosX = -3; leafPosX <= 3; ++leafPosX) {
//                    for (int leafPosZ = -3; leafPosZ <= 3; ++leafPosZ) {
//                        //test to see if not corners
//                        if (Math.abs(leafPosX) != 3 || Math.abs(leafPosZ) != 3) {
//                            this.placeLeafAt(worldIn, leafPos.reset().add(leafPosX, 0, leafPosZ).getPos());
//                        }
//                    }
//                }
//                //placing leaves in a 3 x 3 at the topmost layer
//                leafPos.setOrigin(trunkPosX, lastTrunkY + 1, trunkPosZ);
//                for (int leafPosTopX = -1; leafPosTopX <= 1; ++leafPosTopX) {
//                    for (int leafPosTopZ = -1; leafPosTopZ <= 1; ++leafPosTopZ) {
//                        this.placeLeafAt(worldIn, leafPos.reset().add(leafPosTopX, 0, leafPosTopZ).getPos());
//                    }
//                }
//                //placing leaves at the top most layer, making a cross
//                this.placeLeafAt(worldIn, leafPos.reset().offset(Direction.EAST, 2).getPos());
//                this.placeLeafAt(worldIn, leafPos.reset().offset(Direction.WEST, 2).getPos());
//                this.placeLeafAt(worldIn, leafPos.reset().offset(Direction.NORTH, 2).getPos());
//                this.placeLeafAt(worldIn, leafPos.reset().offset(Direction.SOUTH, 2).getPos());
//                trunkPosX = position.getX();
//                trunkPosZ = position.getZ();
//                //create a branch
//                Direction branchInclination = Direction.Plane.HORIZONTAL.random(rand);
//                if (branchInclination != topInclination) {
//                    int branchStartHeight = topInclinationStartHeight - rand.nextInt(2) - 1;
//                    int branchSize = 1 + rand.nextInt(3);
//                    lastTrunkY = 0;
//                    MutableBlockPos branchPos = new MutableBlockPos();
//                    for (int branchStartPosY = branchStartHeight; branchStartPosY < numLogs && branchSize > 0; --branchSize) {
//                        if (branchStartPosY >= 1) {
//                            int branchPosY = position.getY() + branchStartPosY;
//                            trunkPosX += branchInclination.getXOffset();
//                            trunkPosZ += branchInclination.getZOffset();
//                            branchPos.setPos(trunkPosX, branchPosY, trunkPosZ);
//                            BlockState stateBranch = ((IBlockReader) worldIn).getBlockState(branchPos);
//                            if (stateBranch.isAir((IBlockReader) worldIn, branchPos) || stateBranch.getBlock() instanceof BlockLeaves) {
//                                this.placeTrunkAt(changedBlocks, worldIn, branchPos, box);
//                                lastTrunkY = branchPosY;
//                            }
//                        }
//                        ++branchStartPosY;
//                    }
//                    if (lastTrunkY > 0) {
//                        OriginMutableBlockPos branchLastPos = new OriginMutableBlockPos(trunkPosX, lastTrunkY, trunkPosZ);
//                        //place leaves in 5 by 5 - corners
//                        for (int branchLeafX = -2; branchLeafX <= 2; ++branchLeafX) {
//                            for (int branchLeafZ = -2; branchLeafZ <= 2; ++branchLeafZ) {
//                                //check if not corners
//                                if (Math.abs(branchLeafX) != 2 || Math.abs(branchLeafZ) != 2) {
//                                    this.placeLeafAt(worldIn, branchLastPos.reset().add(branchLeafX, 0, branchLeafZ).getPos());
//                                }
//                            }
//                        }
//                        //placing leaves in 3 by 3
//                        branchLastPos.setOrigin(trunkPosX, lastTrunkY + 1, trunkPosZ);
//                        for (int branchLeafTopX = -1; branchLeafTopX <= 1; ++branchLeafTopX) {
//                            for (int branchLeafTopZ = -1; branchLeafTopZ <= 1; ++branchLeafTopZ) {
//                                this.placeLeafAt(worldIn, branchLastPos.reset().add(branchLeafTopX, 0, branchLeafTopZ).getPos());
//                            }
//                        }
//                    }
//                }
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