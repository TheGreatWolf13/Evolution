//package tgw.evolution.world.feature.tree;
//
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.IWorldReader;
//import net.minecraft.world.gen.IWorldGenerationReader;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.BlockLeaves;
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
//public class SpruceTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static final BlockState TRUNK = EvolutionBlocks.LOG_SPRUCE.get().getDefaultState().with(TREE, true);
//    private static final BlockState LEAF = EvolutionBlocks.LEAVES_SPRUCE.get().getDefaultState();
//
//    public SpruceTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean notify) {
//        super(config, notify);
//    }
//
//    @Override
//    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader world, Random rand, BlockPos position, MutableBoundingBox box) {
//        int trunkHeight = rand.nextInt(4) + 6;
//        int j = 1 + rand.nextInt(2);
//        int maximumLeafRadius = 2 + rand.nextInt(2);
//        if (position.getY() >= 1 && position.getY() + trunkHeight + 1 <= ((IWorld) world).getWorld().getHeight()) {
//            boolean flag = true;
//            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
//            for (int i1 = position.getY(); i1 <= position.getY() + 1 + trunkHeight && flag; ++i1) {
//                int j1;
//                if (i1 - position.getY() < j) {
//                    j1 = 0;
//                }
//                else {
//                    j1 = maximumLeafRadius;
//                }
//                for (int k1 = position.getX() - j1; k1 <= position.getX() + j1 && flag; ++k1) {
//                    for (int l1 = position.getZ() - j1; l1 <= position.getZ() + j1 && flag; ++l1) {
//                        if (i1 >= 0 && i1 < ((IWorld) world).getWorld().getHeight()) {
//                            BlockState iblockstate = ((IBlockReader) world).getBlockState(mutablePos.setPos(k1, i1, l1));
//                            if (!iblockstate.isAir((IBlockReader) world, mutablePos) && !(iblockstate.getBlock() instanceof BlockLeaves)) {
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
//            if (BlockUtils.canSustainSapling(((IBlockReader) world).getBlockState(position.down()), EvolutionBlocks.SAPLING_SPRUCE.get()) &&
//                position.getY() < ((IWorld) world).getWorld().getHeight() - trunkHeight - 1) {
//                TreeUtils.setDirtAt(world, position.down());
//                int leafRadius = rand.nextInt(2);
//                int j3 = 1;
//                int k3 = 0;
//                int leafLayers = trunkHeight - j;
//                BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
//                for (int placingLeafY = 0; placingLeafY <= leafLayers; ++placingLeafY) {
//                    int leafY = position.getY() + trunkHeight - placingLeafY;
//                    for (int leafX = position.getX() - leafRadius; leafX <= position.getX() + leafRadius; ++leafX) {
//                        int cornerX = leafX - position.getX();
//                        for (int leafZ = position.getZ() - leafRadius; leafZ <= position.getZ() + leafRadius; ++leafZ) {
//                            int cornerZ = leafZ - position.getZ();
//                            if (Math.abs(cornerX) != leafRadius || Math.abs(cornerZ) != leafRadius || leafRadius <= 0) {
//                                leafPos.setPos(leafX, leafY, leafZ);
//                                if (((IBlockReader) world).getBlockState(leafPos).canBeReplacedByLeaves((IWorldReader) world, leafPos)) {
//                                    this.setBlockState(world, leafPos, LEAF);
//                                }
//                            }
//                        }
//                    }
//                    if (leafRadius >= j3) {
//                        leafRadius = k3;
//                        k3 = 1;
//                        ++j3;
//                        if (j3 > maximumLeafRadius) {
//                            j3 = maximumLeafRadius;
//                        }
//                    }
//                    else {
//                        ++leafRadius;
//                    }
//                }
//                int trunkReduction = rand.nextInt(3);
//                for (int placingTrunks = 0; placingTrunks < trunkHeight - trunkReduction; ++placingTrunks) {
//                    BlockState trunkState = ((IBlockReader) world).getBlockState(position.up(placingTrunks));
//                    if (trunkState.isAir((IBlockReader) world, position.up(placingTrunks)) || trunkState.getBlock() instanceof BlockLeaves) {
//                        this.setLogState(changedBlocks, world, position.up(placingTrunks), TRUNK, box);
//                    }
//                }
//                return true;
//            }
//            return false;
//        }
//        return false;
//    }
//}
