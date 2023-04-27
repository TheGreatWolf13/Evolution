//package tgw.evolution.world.feature.tree;
//
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.gen.IWorldGenerationReader;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.BlockLeaves;
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
//public class RedwoodGiantTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static final BlockState LOG = EvolutionBlocks.LOG_REDWOOD.get().getDefaultState().with(TREE, true);
//    private static final BlockState LEAVES = EvolutionBlocks.LEAVES_REDWOOD.get().getDefaultState();
//
//    public RedwoodGiantTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean doBlockNofityOnPlace) {
//        super(config, doBlockNofityOnPlace);
//    }
//
//    private static boolean placeTreeOfHeight(IWorldGenerationReader worldIn, BlockPos pos, int height) {
//        int posX = pos.getX();
//        int posY = pos.getY();
//        int posZ = pos.getZ();
//        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
//        for (int l = 0; l <= height + 1; ++l) {
//            int i1 = 1;
//            if (l == 0) {
//                i1 = 0;
//            }
//            if (l >= height - 1) {
//                i1 = 2;
//            }
//            for (int j1 = -i1; j1 <= i1; ++j1) {
//                for (int k1 = -i1; k1 <= i1; ++k1) {
//                    if (!BlockSapling.canGrowInto(worldIn, blockpos$mutableblockpos.setPos(posX + j1, posY + l, posZ + k1))) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    @Override
//    protected boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader world, Random rand, BlockPos pos, MutableBoundingBox boundsIn) {
//        int trunkHeight = 7 + rand.nextInt(14);
//        this.placeTrunk(changedBlocks, world, pos, boundsIn);
//        //tree within world height limit
//        if (pos.getY() >= 1 && pos.getY() + trunkHeight + 1 < 256) {
//            boolean isSoil = BlockUtils.canSustainSapling(((IBlockReader) world).getBlockState(pos.down()), EvolutionBlocks.SAPLING_REDWOOD.get());
//            if (!isSoil) {
//                return false;
//            }
//            if (!placeTreeOfHeight(world, pos, trunkHeight)) {
//                return false;
//            }
//            //the NW corner sapling grows first
//            TreeUtils.setDirtAt(world, pos.down());
//            TreeUtils.setDirtAt(world, pos.down().east());
//            TreeUtils.setDirtAt(world, pos.down().east(2));
//            TreeUtils.setDirtAt(world, pos.down().south());
//            TreeUtils.setDirtAt(world, pos.down().south(2));
//            TreeUtils.setDirtAt(world, pos.down().south().east());
//            TreeUtils.setDirtAt(world, pos.down().south(2).east());
//            TreeUtils.setDirtAt(world, pos.down().south().east(2));
//            TreeUtils.setDirtAt(world, pos.down().south(2).east(2));
//            int changedPosX = pos.getX();
//            int changedPosZ = pos.getZ();
//            int leavesLayer = 0;
//            BlockPos.MutableBlockPos trunkPos = new BlockPos.MutableBlockPos();
//            for (int placingTrunks = 0; placingTrunks < trunkHeight + 15; ++placingTrunks) {
//                //placing main trunk
//                int posYForPlacement = pos.getY() + placingTrunks;
//                trunkPos.setPos(changedPosX, posYForPlacement, changedPosZ);
//                if (placingTrunks < trunkHeight) {
//                    this.placeTrunk(changedBlocks, world, trunkPos, boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east(2), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.south(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.south(2), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east().south(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east(2).south(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east().south(2), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east(2).south(2), boundsIn);
//                }
//                else if (placingTrunks < trunkHeight + 8) {
//                    this.placeTrunk(changedBlocks, world, trunkPos.east(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.south(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east().south(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east(2).south(), boundsIn);
//                    this.placeTrunk(changedBlocks, world, trunkPos.east().south(2), boundsIn);
//                    if (++leavesLayer % 2 == 0) {
//                        for (int i = -4; i <= 4; i++) {
//                            for (int j = -4; j <= 4; j++) {
//                                if ((Math.abs(i) != 4 || Math.abs(j) != 4) &&
//                                    (Math.abs(i) != 4 || Math.abs(j) != 3) &&
//                                    (Math.abs(i) != 3 || Math.abs(j) != 4)) {
//                                    this.placeLeaves(world, pos.east().south().getX() + i, posYForPlacement, pos.east().south().getZ() + j);
//                                }
//                            }
//                        }
//                    }
//                    else {
//                        for (int i = -3; i <= 3; i++) {
//                            for (int j = -3; j <= 3; j++) {
//                                if (Math.abs(i) != 3 || Math.abs(j) != 3) {
//                                    this.placeLeaves(world, pos.east().south().getX() + i, posYForPlacement, pos.east().south().getZ() + j);
//                                }
//                            }
//                        }
//                    }
//                }
//                else if (placingTrunks < trunkHeight + 12) {
//                    this.placeTrunk(changedBlocks, world, trunkPos.east().south(), boundsIn);
//                    if (++leavesLayer % 2 == 0) {
//                        for (int i = -2; i <= 2; i++) {
//                            for (int j = -2; j <= 2; j++) {
//                                if (Math.abs(i) != 2 || Math.abs(j) != 2) {
//                                    this.placeLeaves(world, pos.east().south().getX() + i, posYForPlacement, pos.east().south().getZ() + j);
//                                }
//                            }
//                        }
//                    }
//                    else {
//                        for (int i = -3; i <= 3; i++) {
//                            for (int j = -3; j <= 3; j++) {
//                                if (Math.abs(i) != 3 || Math.abs(j) != 3) {
//                                    this.placeLeaves(world, pos.east().south().getX() + i, posYForPlacement, pos.east().south().getZ() + j);
//                                }
//                            }
//                        }
//                    }
//                }
//                else {
//                    if (placingTrunks == trunkHeight + 12) {
//                        for (int i = -1; i <= 1; i++) {
//                            for (int j = -1; j <= 1; j++) {
//                                this.placeLeaves(world, pos.east().south().getX() + i, posYForPlacement, pos.east().south().getZ() + j);
//                            }
//                        }
//                    }
//                    else {
//                        for (int i = -1; i <= 1; i++) {
//                            for (int j = -1; j <= 1; j++) {
//                                if (Math.abs(i) != 1 || Math.abs(j) != 1) {
//                                    this.placeLeaves(world, pos.east().south().getX() + i, posYForPlacement, pos.east().south().getZ() + j);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            return true;
//        }
//        return false;
//    }
//
//    private void placeLeaves(IWorldGenerationReader iWorld, int posX, int posY, int posZ) {
//        BlockPos leafPos = new BlockPos(posX, posY, posZ);
//        if (((IBlockReader) iWorld).getBlockState(leafPos).isAir((IBlockReader) iWorld, leafPos)) {
//            this.setBlockState(iWorld, leafPos, LEAVES);
//        }
//    }
//
//    private void placeTrunk(Set<BlockPos> changedBlocks, IWorldGenerationReader iWorld, BlockPos pos, MutableBoundingBox box) {
//        BlockState state = ((IBlockReader) iWorld).getBlockState(pos);
//        if (state.isAir((IBlockReader) iWorld, pos) || state.getBlock() instanceof BlockLeaves) {
//            if (BlockSapling.canGrowInto(iWorld, pos)) {
//                this.setLogState(changedBlocks, iWorld, pos, LOG, box);
//            }
//        }
//    }
//}
