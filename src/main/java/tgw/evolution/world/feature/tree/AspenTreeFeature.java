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
//public class AspenTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static BlockState LOG = EvolutionBlocks.LOG_ASPEN.get().getDefaultState().with(TREE, true);
//    private static BlockState LEAVES = EvolutionBlocks.LEAVES_ASPEN.get().getDefaultState();
//    private final int dec;
//
//    public AspenTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn,
//                            boolean notify,
//                            BlockState log,
//                            BlockState leaves,
//                            int dec) {
//        super(configFactoryIn, notify);
//        LOG = log;
//        LEAVES = leaves;
//        this.dec = dec;
//    }
//
//    public AspenTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn, boolean notify) {
//        super(configFactoryIn, notify);
//        LOG = EvolutionBlocks.LOG_ASPEN.get().getDefaultState().with(TREE, true);
//        LEAVES = EvolutionBlocks.LEAVES_ASPEN.get().getDefaultState();
//        this.dec = 0;
//    }
//
//    @Override
//    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos pos, MutableBoundingBox box) {
//        int posX = pos.getX();
//        int posY = pos.getY();
//        int posZ = pos.getZ();
//        int height = 8 + rand.nextInt(8) - this.dec;
//        int leafMin = posY + 2 + rand.nextInt(3);
//        if (posY < 1) {
//            return false;
//        }
//        if (height + 1 > 256) {
//            return false;
//        }
//        int posYInc = posY;
//        boolean flag = true;
//        while (posYInc <= posY + height + 1) {
//            int range = 1;
//            if (posYInc == posY) {
//                range = 0;
//            }
//            if (posYInc >= leafMin) {
//                range = 2;
//            }
//            int xInc = posX - range;
//            while (xInc <= posX + range && flag) {
//                for (int zInc = posZ - range; zInc <= posZ + range && flag; ++zInc) {
//                    if (posYInc >= 0 && posYInc < 256) {
//                        if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(new BlockPos(xInc, posYInc, zInc)))) {
//                            continue;
//                        }
//                        flag = false;
//                        continue;
//                    }
//                    flag = false;
//                }
//                ++xInc;
//            }
//            if (!(xInc <= posX + range && flag)) {
//                ++posYInc;
//            }
//        }
//        boolean canGrow = false;
//        if (!(posYInc <= posY + height + 1)) {
//            if (!flag) {
//                return false;
//            }
//            canGrow = true;
//        }
//        int k1;
//        int i12;
//        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
//        for (i12 = posX; i12 <= posX && canGrow; ++i12) {
//            for (k1 = posZ; k1 <= posZ && canGrow; ++k1) {
//                BlockState below = ((IBlockReader) worldIn).getBlockState(mutablePos.setPos(i12, posY - 1, k1));
//                if (BlockUtils.canSustainSapling(below, EvolutionBlocks.SAPLING_ASPEN.get())) {
//                    continue;
//                }
//                canGrow = false;
//            }
//        }
//        if (!canGrow) {
//            return false;
//        }
//        for (i12 = posX; i12 <= posX; ++i12) {
//            for (k1 = posZ; k1 <= posZ; ++k1) {
//                TreeUtils.setDirtAt(worldIn, mutablePos.setPos(i12, posY - 1, k1));
//            }
//        }
//        BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos(posX, leafMin, posZ);
//        int leafTop = posY + height + 1;
//        while (leafPos.getY() <= leafTop) {
//            int leafWidth = 2;
//            if (leafPos.getY() >= leafTop - 1) {
//                leafWidth = 0;
//            }
//            else if (leafPos.getY() >= leafTop - 3 || leafPos.getY() <= leafMin + 1 || rand.nextInt(4) == 0) {
//                leafWidth = 1;
//            }
//            int branches = 4 + rand.nextInt(5);
//            int b = 0;
//            while (b < branches) {
//                leafPos.setPos(posX, leafPos.getY(), posZ);
//                int length = 4 + rand.nextInt(8);
//                for (int l = 0; l < length && Math.abs(leafPos.getX() - posX) <= leafWidth && Math.abs(leafPos.getZ() - posZ) <= leafWidth; ++l) {
//                    if (((IBlockReader) worldIn).getBlockState(leafPos).canBeReplacedByLeaves((IWorldReader) worldIn, leafPos) ||
//                        ((IBlockReader) worldIn).getBlockState(leafPos).getBlock() instanceof BlockLeaves) {
//                        this.placeLeafAt((IWorld) worldIn, leafPos);
//                        Direction dir = Direction.byHorizontalIndex(rand.nextInt(4));
//                        leafPos.move(dir);
//                    }
//                    else {
//                        break;
//                    }
//                }
//                ++b;
//            }
//            leafPos.move(Direction.UP);
//        }
//        for (int i = posY; i < posY + height; i++) {
//            this.placeTrunkAt(changedBlocks, worldIn, mutablePos.setPos(posX, i, posZ), box);
//        }
//        return true;
//    }
//
//    private void placeLeafAt(IWorld worldIn, BlockPos pos) {
//        BlockState iblockstate = worldIn.getBlockState(pos);
//        if (iblockstate.canBeReplacedByLeaves(worldIn, pos)) {
//            this.setBlockState(worldIn, pos, LEAVES);
//        }
//    }
//
//    private void placeTrunkAt(Set<BlockPos> setBlockPos, IWorldGenerationReader iWorld, BlockPos pos, MutableBoundingBox box) {
//        this.setLogState(setBlockPos, iWorld, pos, LOG, box);
//    }
//}
