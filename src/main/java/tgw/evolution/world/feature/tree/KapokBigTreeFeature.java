package tgw.evolution.world.feature.tree;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.IWorldGenerationBaseReader;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.HugeTreesFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.common.IPlantable;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class KapokBigTreeFeature extends HugeTreesFeature<NoFeatureConfig> {

    public KapokBigTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean notify, int baseHeightIn, int extraRandomHeightIn, BlockState logState, BlockState leavesState) {
        super(config, notify, baseHeightIn, extraRandomHeightIn, logState, leavesState);
        this.setSapling((IPlantable) EvolutionBlocks.SAPLING_KAPOK.get());
    }

    private static boolean isSpaceAt(IWorldGenerationBaseReader worldIn, BlockPos leavesPos, int height) {
        if (leavesPos.getY() >= 1 && leavesPos.getY() + height + 1 <= worldIn.getMaxHeight()) {
            boolean flag = true;
            for (int i = 0; i <= 1 + height; ++i) {
                int j = 2;
                if (i == 0) {
                    j = 1;
                }
                else if (i >= 1 + height - 2) {
                    j = 2;
                }
                for (int k = -j; k <= j && flag; ++k) {
                    for (int l = -j; l <= j && flag; ++l) {
                        if (leavesPos.getY() + i < 0 || leavesPos.getY() + i >= worldIn.getMaxHeight() || !func_214587_a(worldIn, leavesPos.add(k, i, l))) {
                            flag = false;
                        }
                    }
                }
            }
            return flag;
        }
        return false;
    }

    @Override
    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos position, MutableBoundingBox p_208519_5_) {
        int i = this.getHeight(rand);
        if (!this.check(worldIn, position, i)) {
            return false;
        }
        this.placeSomething(worldIn, position.up(i), 2, p_208519_5_, changedBlocks);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int j = position.getY() + i - 2 - rand.nextInt(4); j > position.getY() + i / 2; j -= 2 + rand.nextInt(4)) {
            float f = rand.nextFloat() * ((float) Math.PI * 2F);
            int k = position.getX() + (int) (0.5F + MathHelper.cos(f) * 4.0F);
            int l = position.getZ() + (int) (0.5F + MathHelper.sin(f) * 4.0F);
            for (int i1 = 0; i1 < 5; ++i1) {
                k = position.getX() + (int) (1.5F + MathHelper.cos(f) * i1);
                l = position.getZ() + (int) (1.5F + MathHelper.sin(f) * i1);
                this.setLogState(changedBlocks, worldIn, mutablePos.setPos(k, j - 3 + i1 / 2, l), this.trunk, p_208519_5_);
            }
            int j2 = 1 + rand.nextInt(2);
            for (int k1 = j - j2; k1 <= j; ++k1) {
                int l1 = k1 - j;
                this.func_222838_b(worldIn, mutablePos.setPos(k, k1, l), 1 - l1, p_208519_5_, changedBlocks);
            }
        }
        for (int i2 = 0; i2 < i; ++i2) {
            BlockPos blockpos = position.up(i2);
            if (func_214587_a(worldIn, blockpos)) {
                this.setLogState(changedBlocks, worldIn, blockpos, this.trunk, p_208519_5_);
                if (i2 > 0) {
                    this.tryPlaceVines(worldIn, rand, blockpos.west(), VineBlock.EAST);
                    this.tryPlaceVines(worldIn, rand, blockpos.north(), VineBlock.SOUTH);
                }
            }
            if (i2 < i - 1) {
                BlockPos blockpos1 = blockpos.east();
                if (func_214587_a(worldIn, blockpos1)) {
                    this.setLogState(changedBlocks, worldIn, blockpos1, this.trunk, p_208519_5_);
                    if (i2 > 0) {
                        this.tryPlaceVines(worldIn, rand, blockpos1.east(), VineBlock.WEST);
                        this.tryPlaceVines(worldIn, rand, blockpos1.north(), VineBlock.SOUTH);
                    }
                }
                BlockPos blockpos2 = blockpos.south().east();
                if (func_214587_a(worldIn, blockpos2)) {
                    this.setLogState(changedBlocks, worldIn, blockpos2, this.trunk, p_208519_5_);
                    if (i2 > 0) {
                        this.tryPlaceVines(worldIn, rand, blockpos2.east(), VineBlock.WEST);
                        this.tryPlaceVines(worldIn, rand, blockpos2.south(), VineBlock.NORTH);
                    }
                }
                BlockPos blockpos3 = blockpos.south();
                if (func_214587_a(worldIn, blockpos3)) {
                    this.setLogState(changedBlocks, worldIn, blockpos3, this.trunk, p_208519_5_);
                    if (i2 > 0) {
                        this.tryPlaceVines(worldIn, rand, blockpos3.west(), VineBlock.EAST);
                        this.tryPlaceVines(worldIn, rand, blockpos3.south(), VineBlock.NORTH);
                    }
                }
            }
        }
        return true;
    }

    private void tryPlaceVines(IWorldGenerationReader worldIn, Random random, BlockPos pos, BooleanProperty sideProperty) {
        if (random.nextInt(3) > 0 && isAir(worldIn, pos)) {
            this.setBlockState(worldIn, pos, Blocks.VINE.getDefaultState().with(sideProperty, true));
        }
    }

    private void placeSomething(IWorldGenerationReader worldIn, BlockPos p_214601_2_, int p_214601_3_, MutableBoundingBox p_214601_4_, Set<BlockPos> changedBlocks) {
        for (int j = -2; j <= 0; ++j) {
            this.func_222839_a(worldIn, p_214601_2_.up(j), p_214601_3_ + 1 - j, p_214601_4_, changedBlocks);
        }
    }

    protected boolean check(IWorldGenerationReader worldIn, BlockPos pos, int height) {
        return isSpaceAt(worldIn, pos, height) && this.otherMethod(worldIn, pos);
    }

    private boolean otherMethod(IWorldGenerationReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.down();
        boolean isSoil = BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(blockpos), this.sapling);
        if (isSoil && pos.getY() >= 2) {
            this.setDirtAt(worldIn, blockpos, pos);
            this.setDirtAt(worldIn, blockpos.east(), pos);
            this.setDirtAt(worldIn, blockpos.south(), pos);
            this.setDirtAt(worldIn, blockpos.south().east(), pos);
            return true;
        }
        return false;
    }
}