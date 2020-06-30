package tgw.evolution.world.feature.tree;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.blocks.BlockLeaves;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.blocks.BlockSapling;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class MapleTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {

    private static final BlockState LOG = EvolutionBlocks.LOG_MAPLE.get().getDefaultState().with(BlockLog.TREE, true);
    private static final BlockState LEAVES = EvolutionBlocks.LEAVES_MAPLE.get().getDefaultState();

    public MapleTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean notify) {
        super(config, notify);
    }

    @Override
    protected boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos pos, MutableBoundingBox box) {
        int trunkHeight = rand.nextInt(4) + 7;
        if (pos.getY() >= 1 && pos.getY() + trunkHeight + 1 <= ((IWorld) worldIn).getWorld().getHeight()) {
            boolean flag = true;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
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
                            BlockState iblockstate = ((IBlockReader) worldIn).getBlockState(blockpos$mutableblockpos.setPos(k1, i1, l1));
                            if (!iblockstate.isAir((IBlockReader) worldIn, blockpos$mutableblockpos) && !(iblockstate.getBlock() instanceof BlockLeaves)) {
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
            if (BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(pos.down()), (BlockSapling) EvolutionBlocks.SAPLING_CEDAR.get()) && pos.getY() < ((IWorld) worldIn).getWorld().getHeight() - trunkHeight - 1) {
                this.setDirtAt(worldIn, pos.down(), pos);
                for (int placingTrunks = 0; placingTrunks < trunkHeight; placingTrunks++) {
                    BlockState trunkState = ((IBlockReader) worldIn).getBlockState(pos.up(placingTrunks));
                    if (trunkState.isAir((IBlockReader) worldIn, pos.up(placingTrunks)) || trunkState.getBlock() instanceof BlockLeaves) {
                        this.setLogState(changedBlocks, worldIn, pos.up(placingTrunks), LOG, box);
                        if (placingTrunks == trunkHeight - 1 || placingTrunks == 1) {
                            this.topCross(worldIn, pos, placingTrunks + 1);
                        }
                        else if (placingTrunks == trunkHeight - 2) {
                            this.leavesSquare3(worldIn, pos, placingTrunks + 1, rand);
                        }
                        else if (placingTrunks == 2 || placingTrunks == trunkHeight - 3) {
                            this.leavesSquare5(worldIn, pos, placingTrunks + 1);
                        }
                        else if (placingTrunks > 2) {
                            this.leavesSquare7(worldIn, pos, placingTrunks + 1);
                        }
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private void topCross(IWorldGenerationReader worldIn, BlockPos pos, int height) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) != 1 || Math.abs(z) != 1) {
                    if (((IBlockReader) worldIn).getBlockState(pos.add(x, height, z)).canBeReplacedByLeaves((IWorldReader) worldIn, pos.add(x, height, z))) {
                        this.setBlockState(worldIn, pos.add(x, height, z), LEAVES);
                    }
                }
            }
        }
    }

    private void leavesSquare3(IWorldGenerationReader worldIn, BlockPos pos, int height, Random rand) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) != 1 || Math.abs(z) != 1) {
                    if (((IBlockReader) worldIn).getBlockState(pos.add(x, height, z)).canBeReplacedByLeaves((IWorldReader) worldIn, pos.add(x, height, z))) {
                        this.setBlockState(worldIn, pos.add(x, height, z), LEAVES);
                    }
                }
                else if (rand.nextBoolean()) {
                    if (((IBlockReader) worldIn).getBlockState(pos.add(x, height, z)).canBeReplacedByLeaves((IWorldReader) worldIn, pos.add(x, height, z))) {
                        this.setBlockState(worldIn, pos.add(x, height, z), LEAVES);
                    }
                }
            }
        }
    }

    private void leavesSquare5(IWorldGenerationReader worldIn, BlockPos pos, int height) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(z) != 2 || Math.abs(x) != 2) {
                    if (((IBlockReader) worldIn).getBlockState(pos.add(x, height, z)).canBeReplacedByLeaves((IWorldReader) worldIn, pos.add(x, height, z))) {
                        this.setBlockState(worldIn, pos.add(x, height, z), LEAVES);
                    }
                }
            }
        }
    }

    private void leavesSquare7(IWorldGenerationReader worldIn, BlockPos pos, int height) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if ((Math.abs(x) != 3 || Math.abs(z) != 3) && (Math.abs(x) != 3 || Math.abs(z) != 2) && (Math.abs(x) != 2 || Math.abs(z) != 3)) {
                    if (((IBlockReader) worldIn).getBlockState(pos.add(x, height, z)).canBeReplacedByLeaves((IWorldReader) worldIn, pos.add(x, height, z))) {
                        this.setBlockState(worldIn, pos.add(x, height, z), LEAVES);
                    }
                }
            }
        }
    }
}