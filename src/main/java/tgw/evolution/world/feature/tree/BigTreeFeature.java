//package tgw.evolution.world.feature.tree;
//
//import com.google.common.collect.Lists;
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.material.Material;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.gen.IWorldGenerationReader;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.BlockSapling;
//import tgw.evolution.blocks.BlockUtils;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.TreeUtils;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Random;
//import java.util.Set;
//import java.util.function.Function;
//
//import static tgw.evolution.init.EvolutionBStates.AXIS;
//import static tgw.evolution.init.EvolutionBStates.TREE;
//
//public class BigTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static final BlockState LOG = EvolutionBlocks.LOG_OAK.get().getDefaultState().with(TREE, true);
//    private static final BlockState LEAVES = EvolutionBlocks.LEAVES_OAK.get().getDefaultState();
//
//    public BigTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory, boolean notify) {
//        super(configFactory, notify);
//    }
//
//    private static float foliageShape(int y) {
//        if (y >= 0 && y < 5) {
//            return y != 0 && y != 4 ? 3.0F : 2.0F;
//        }
//        return -1.0F;
//    }
//
//    /**
//     * Returns the absolute greatest distance in the BlockPos object.
//     */
//    private static int getGreatestDistance(BlockPos posIn) {
//        int i = MathHelper.abs(posIn.getX());
//        int j = MathHelper.abs(posIn.getY());
//        int k = MathHelper.abs(posIn.getZ());
//        if (k > i && k > j) {
//            return k;
//        }
//        return Math.max(j, i);
//    }
//
//    private static Direction.Axis getLoxAxis(BlockPos pos0, BlockPos pos1) {
//        int deltaX = Math.abs(pos1.getX() - pos0.getX());
//        int deltaZ = Math.abs(pos1.getZ() - pos0.getZ());
//        int maxDelta = Math.max(deltaX, deltaZ);
//        if (maxDelta > 0) {
//            if (deltaX == maxDelta) {
//                return Direction.Axis.X;
//            }
//            return Direction.Axis.Z;
//        }
//        return Direction.Axis.Y;
//    }
//
//    private static float treeShape(int p_208527_1_, int p_208527_2_) {
//        if (p_208527_2_ < p_208527_1_ * 0.3F) {
//            return -1.0F;
//        }
//        float f = p_208527_1_ / 2.0F;
//        float f1 = f - p_208527_2_;
//        float f2 = MathHelper.sqrt(f * f - f1 * f1);
//        if (f1 == 0.0F) {
//            f2 = f;
//        }
//        else if (Math.abs(f1) >= f) {
//            return 0.0F;
//        }
//        return f2 * 0.5F;
//    }
//
//    private static boolean trimBranches(int p_208522_1_, int p_208522_2_) {
//        return p_208522_2_ >= p_208522_1_ * 0.2D;
//    }
//
//    private int checkLocation(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, BlockPos pos, int p_208528_4_, MutableBoundingBox box) {
//        if (!BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(pos.down()), EvolutionBlocks.SAPLING_OAK.get())) {
//            return -1;
//        }
//        int i = this.makeLimb(changedBlocks, worldIn, pos, pos.up(p_208528_4_ - 1), false, box);
//        if (i == -1) {
//            return p_208528_4_;
//        }
//        return i < 6 ? -1 : i;
//    }
//
//    private void crossSection(IWorldGenerationReader worldIn, BlockPos pos, float p_208529_3_) {
//        int i = (int) (p_208529_3_ + 0.618D);
//        for (int j = -i; j <= i; ++j) {
//            for (int k = -i; k <= i; ++k) {
//                if (Math.pow(Math.abs(j) + 0.5D, 2.0D) + Math.pow(Math.abs(k) + 0.5D, 2.0D) <= p_208529_3_ * p_208529_3_) {
//                    BlockPos blockpos = pos.add(j, 0, k);
//                    BlockState iblockstate = ((IBlockReader) worldIn).getBlockState(blockpos);
//                    if (iblockstate.isAir((IBlockReader) worldIn, blockpos) || iblockstate.getMaterial() == Material.LEAVES) {
//                        this.setBlockState(worldIn, blockpos, LEAVES);
//                    }
//                }
//            }
//        }
//    }
//
//    private void foliageCluster(IWorldGenerationReader worldIn, BlockPos p_202393_2_) {
//        for (int i = 0; i < 5; ++i) {
//            this.crossSection(worldIn, p_202393_2_.up(i), foliageShape(i));
//        }
//    }
//
//    private void makeBranches(Set<BlockPos> changedBlocks,
//                              IWorldGenerationReader world,
//                              int p_208524_3_,
//                              BlockPos pos,
//                              List<BigTreeFeature.FoliageCoordinates> list,
//                              MutableBoundingBox box) {
//        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
//        for (BigTreeFeature.FoliageCoordinates foliageCoordinates : list) {
//            int i = foliageCoordinates.getBranchBase();
//            mutablePos.setPos(pos.getX(), i, pos.getZ());
//            //noinspection EqualsBetweenInconvertibleTypes
//            if (!mutablePos.equals(foliageCoordinates) && trimBranches(p_208524_3_, i - pos.getY())) {
//                this.makeLimb(changedBlocks, world, mutablePos, foliageCoordinates, true, box);
//            }
//        }
//    }
//
//    private void makeFoliage(IWorldGenerationReader worldIn, int p_208525_2_, BlockPos pos, List<BigTreeFeature.FoliageCoordinates> list) {
//        for (BigTreeFeature.FoliageCoordinates bigtreefeature$foliagecoordinates : list) {
//            if (trimBranches(p_208525_2_, bigtreefeature$foliagecoordinates.getBranchBase() - pos.getY())) {
//                this.foliageCluster(worldIn, bigtreefeature$foliagecoordinates);
//            }
//        }
//    }
//
//    private int makeLimb(Set<BlockPos> changedBlocks,
//                         IWorldGenerationReader worldIn,
//                         BlockPos p_208523_3_,
//                         BlockPos p_208523_4_,
//                         boolean p_208523_5_,
//                         MutableBoundingBox box) {
//        if (!p_208523_5_ && Objects.equals(p_208523_3_, p_208523_4_)) {
//            return -1;
//        }
//        BlockPos blockpos = p_208523_4_.add(-p_208523_3_.getX(), -p_208523_3_.getY(), -p_208523_3_.getZ());
//        int i = getGreatestDistance(blockpos);
//        float f = (float) blockpos.getX() / i;
//        float f1 = (float) blockpos.getY() / i;
//        float f2 = (float) blockpos.getZ() / i;
//        for (int j = 0; j <= i; ++j) {
//            BlockPos blockpos1 = p_208523_3_.add(0.5F + j * f, 0.5F + j * f1, 0.5F + j * f2);
//            if (p_208523_5_) {
//                this.setLogState(changedBlocks, worldIn, blockpos1, LOG.with(AXIS, getLoxAxis(p_208523_3_, blockpos1)), box);
//            }
//            else if (!BlockSapling.canGrowInto(worldIn, blockpos1)) {
//                return j;
//            }
//        }
//        return -1;
//    }
//
//    private void makeTrunk(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, BlockPos pos, int p_208526_4_, MutableBoundingBox box) {
//        this.makeLimb(changedBlocks, worldIn, pos, pos.up(p_208526_4_), true, box);
//    }
//
//    @Override
//    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos position, MutableBoundingBox box) {
//        Random random = new Random(rand.nextLong());
//        int i = this.checkLocation(changedBlocks, worldIn, position, 5 + random.nextInt(12), box);
//        if (i == -1) {
//            return false;
//        }
//        TreeUtils.setDirtAt(worldIn, position.down());
//        int j = (int) (i * 0.618D);
//        if (j >= i) {
//            j = i - 1;
//        }
//        int k = (int) (1.382D + Math.pow(1.0D * i / 13.0D, 2.0D));
//        if (k < 1) {
//            k = 1;
//        }
//        int l = position.getY() + j;
//        int i1 = i - 5;
//        List<BigTreeFeature.FoliageCoordinates> list = Lists.newArrayList();
//        list.add(new BigTreeFeature.FoliageCoordinates(position.up(i1), l));
//        BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
//        BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();
//        BlockPos.MutableBlockPos mutablePos3 = new BlockPos.MutableBlockPos();
//        for (; i1 >= 0; --i1) {
//            float f = treeShape(i, i1);
//            if (!(f < 0.0F)) {
//                for (int j1 = 0; j1 < k; ++j1) {
//                    double d2 = 1.0D * f * (random.nextFloat() + 0.328D);
//                    double d3 = random.nextFloat() * 2.0F * Math.PI;
//                    double d4 = d2 * Math.sin(d3) + 0.5D;
//                    double d5 = d2 * Math.cos(d3) + 0.5D;
//                    mutablePos1.setPos(position).move(MathHelper.floor(d4), i1 - 1, MathHelper.floor(d5));
//                    mutablePos3.setPos(mutablePos1).move(Direction.UP, 5);
//                    if (this.makeLimb(changedBlocks, worldIn, mutablePos1, mutablePos3, false, box) == -1) {
//                        int k1 = position.getX() - mutablePos1.getX();
//                        int l1 = position.getZ() - mutablePos1.getZ();
//                        double d6 = mutablePos1.getY() - Math.sqrt(k1 * k1 + l1 * l1) * 0.381D;
//                        int i2 = d6 > l ? l : (int) d6;
//                        mutablePos2.setPos(position.getX(), i2, position.getZ());
//                        if (this.makeLimb(changedBlocks, worldIn, mutablePos2, mutablePos1, false, box) == -1) {
//                            //noinspection ObjectAllocationInLoop
//                            list.add(new BigTreeFeature.FoliageCoordinates(mutablePos1, mutablePos2.getY()));
//                        }
//                    }
//                }
//            }
//        }
//        this.makeFoliage(worldIn, i, position, list);
//        this.makeTrunk(changedBlocks, worldIn, position, j, box);
//        this.makeBranches(changedBlocks, worldIn, i, position, list, box);
//        return true;
//    }
//
//    static class FoliageCoordinates extends BlockPos {
//
//        private final int branchBase;
//
//        public FoliageCoordinates(BlockPos pos, int p_i45635_2_) {
//            super(pos.getX(), pos.getY(), pos.getZ());
//            this.branchBase = p_i45635_2_;
//        }
//
//        public int getBranchBase() {
//            return this.branchBase;
//        }
//    }
//}
