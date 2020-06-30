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
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.blocks.BlockLeaves;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.blocks.BlockSapling;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class WillowTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {

    private final BlockState LOG = EvolutionBlocks.LOG_WILLOW.get().getDefaultState().with(BlockLog.TREE, true);
    private final BlockState LEAVES = EvolutionBlocks.LEAVES_WILLOW.get().getDefaultState();

    public WillowTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean notify) {
        super(config, notify);
    }

    @Override
    protected boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos pos, MutableBoundingBox box) {
        int height = 8 + rand.nextInt(6);
        int j1 = pos.getY();
        BlockState below = ((IBlockReader) worldIn).getBlockState(pos.down());
        boolean flag = true;
        if (!(pos.getY() >= 1 && height + 1 <= 256)) {
            flag = false;
        }
        int k1;
        int i1;
        while (j1 <= pos.getY() + height + 1) {
            int range = 1;
            if (j1 == pos.getY()) {
                range = 0;
            }
            if (j1 >= pos.getY() + height - 1) {
                range = 2;
            }
            i1 = pos.getX() - range;
            while (i1 <= pos.getX() + range && flag) {
                for (k1 = pos.getZ() - range; k1 <= pos.getZ() + range && flag; ++k1) {
                    if (j1 >= 0 && j1 < 256) {
                        if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(new BlockPos(i1, j1, k1)))) {
                            continue;
                        }
                        flag = false;
                        continue;
                    }
                    flag = false;
                }
                ++i1;
            }
            if (!(i1 <= pos.getX() + range && flag)) {
                ++j1;
            }
        }
        if (!BlockUtils.canSustainSapling(below, (BlockSapling) EvolutionBlocks.SAPLING_WILLOW.get())) {
            return false;
        }
        if (!flag) {
            return false;
        }
        ArrayList<BlockPos> vineGrows = new ArrayList<>();
        int angle = 0;
        BlockPos.MutableBlockPos vinePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos basePos = new BlockPos.MutableBlockPos();
        while (angle < 360) {
            float angleR = (float) Math.toRadians(angle += 30 + rand.nextInt(30));
            float sin = MathHelper.sin(angleR);
            float cos = MathHelper.cos(angleR);
            int base = pos.getY() + height - 3 - rand.nextInt(3);
            int length = 2 + rand.nextInt(4);
            i1 = pos.getX();
            j1 = base;
            k1 = pos.getZ();
            basePos.setPos(i1, j1, k1);
            for (int l = 0; l < length; ++l) {
                if (l > 0 && (l % 4 == 0 || rand.nextInt(3) == 0)) {
                    ++j1;
                }
                if (rand.nextFloat() < Math.abs(cos)) {
                    i1 += Math.signum(cos);
                }
                if (rand.nextFloat() < Math.abs(sin)) {
                    k1 += Math.signum(sin);
                }
                basePos.setPos(i1, j1, k1);
                this.setLogState(changedBlocks, worldIn, basePos, this.LOG, box);
            }
            this.spawnLeafCluster(worldIn, rand, basePos);
            vineGrows.add(vinePos.setPos(i1, j1, k1));
        }
        BlockPos.MutableBlockPos placePos = new BlockPos.MutableBlockPos();
        for (j1 = 0; j1 < height; ++j1) {
            placePos.setPos(pos.getX(), pos.getY() + j1, pos.getZ());
            this.setLogState(changedBlocks, worldIn, placePos, this.LOG, box);
            if (j1 != height - 1) {
                continue;
            }
            this.spawnLeafCluster(worldIn, rand, placePos);
            vineGrows.add(placePos);
        }
        BlockPos.MutableBlockPos root = new BlockPos.MutableBlockPos();
        for (i1 = pos.getX() - 1; i1 <= pos.getX() + 1; ++i1) {
            for (k1 = pos.getZ() - 1; k1 <= pos.getZ() + 1; ++k1) {
                int i2 = i1 - pos.getX();
                int k2 = k1 - pos.getZ();
                if (Math.abs(i2) == Math.abs(k2)) {
                    continue;
                }
                int rootY = pos.getY() + 1 + rand.nextInt(2);
                root.setPos(i1, rootY, k1);
                while (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(root))) {
                    this.setLogState(changedBlocks, worldIn, root, this.LOG, box);
                    --rootY;
                    root.setPos(i1, rootY, k1);
                    rand.nextInt(3);
                }
            }
        }
        for (BlockPos coords : vineGrows) {
            this.spawnVineCluster(worldIn, rand, coords);
        }
        return true;
    }

    private void spawnLeafCluster(IWorldGenerationReader worldIn, Random rand, BlockPos pos) {
        int leafRange = 3;
        int leafRangeSq = leafRange * leafRange;
        int leafRangeSqLess = (int) ((leafRange - 0.5) * (leafRange - 0.5));
        int i1 = pos.getX() - leafRange;
        BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
        while (i1 <= pos.getX() + leafRange) {
            for (int j1 = pos.getY() - leafRange; j1 <= pos.getY() + leafRange; ++j1) {
                for (int k1 = pos.getZ() - leafRange; k1 <= pos.getZ() + leafRange; ++k1) {
                    int i2 = i1 - pos.getX();
                    int j2 = j1 - pos.getY();
                    int k2 = k1 - pos.getZ();
                    leafPos.setPos(i1, j1, k1);
                    BlockState state = ((IBlockReader) worldIn).getBlockState(leafPos);
                    int taxicab = Math.abs(i2) + Math.abs(j2) + Math.abs(k2);
                    int dist = i2 * i2 + j2 * j2 + k2 * k2;
                    if (dist >= leafRangeSqLess && (dist >= leafRangeSq || rand.nextInt(3) != 0) || taxicab > 4 || !BlockUtils.isReplaceable(state) && !(state.getBlock() instanceof BlockLeaves)) {
                        continue;
                    }
                    this.setBlockState(worldIn, leafPos, this.LEAVES);
                }
            }
            ++i1;
        }
    }

    private void spawnVineCluster(IWorldGenerationReader worldIn, Random rand, BlockPos pos) {
        int leafRange = 3;
        int leafRangeSq = leafRange * leafRange;
        int i1 = pos.getX() - leafRange;
        BlockPos.MutableBlockPos vinePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos placePos = new BlockPos.MutableBlockPos();
        while (i1 <= pos.getX() + leafRange) {
            for (int j1 = pos.getY() - leafRange; j1 <= pos.getY() + leafRange; ++j1) {
                for (int k1 = pos.getZ() - leafRange; k1 <= pos.getZ() + leafRange; ++k1) {
                    int i2 = i1 - pos.getX();
                    int j2 = j1 - pos.getY();
                    int k2 = k1 - pos.getZ();
                    int dist = i2 * i2 + j2 * j2 + k2 * k2;
                    if (dist >= leafRangeSq) {
                        continue;
                    }
                    vinePos.setPos(i1, j1, k1);
                    BlockState state = ((IBlockReader) worldIn).getBlockState(vinePos);
                    if (state != this.LEAVES) {
                        continue;
                    }
                    placePos.setPos(i1 - 1, j1, k1);
                    int vineChance = 2;
                    if (rand.nextInt(vineChance) == 0 && ((IBlockReader) worldIn).getBlockState(placePos).isAir((IBlockReader) worldIn, placePos)) {
                        this.growVines(worldIn, rand, placePos, VineBlock.EAST);
                    }
                    placePos.setPos(i1 + 1, j1, k1);
                    if (rand.nextInt(vineChance) == 0 && ((IBlockReader) worldIn).getBlockState(placePos).isAir((IBlockReader) worldIn, placePos)) {
                        this.growVines(worldIn, rand, placePos, VineBlock.WEST);
                    }
                    placePos.setPos(i1, j1, k1 - 1);
                    if (rand.nextInt(vineChance) == 0 && ((IBlockReader) worldIn).getBlockState(placePos).isAir((IBlockReader) worldIn, placePos)) {
                        this.growVines(worldIn, rand, placePos, VineBlock.SOUTH);
                    }
                    placePos.setPos(i1, j1, k1 + 1);
                    if (rand.nextInt(vineChance) != 0 || !((IBlockReader) worldIn).getBlockState(placePos).isAir((IBlockReader) worldIn, placePos)) {
                        continue;
                    }
                    this.growVines(worldIn, rand, placePos, VineBlock.NORTH);
                }
            }
            ++i1;
        }
    }

    private void growVines(IWorldGenerationReader worldIn, Random rand, BlockPos pos, BooleanProperty direction) {
        this.setBlockState(worldIn, pos, Blocks.VINE.getDefaultState().with(direction, true));
        int vines = 0;
        while (((IBlockReader) worldIn).getBlockState(pos.down()).isAir((IBlockReader) worldIn, pos.down())) {
            if (vines >= 2 + rand.nextInt(4)) {
                return;
            }
            this.setBlockState(worldIn, pos, Blocks.VINE.getDefaultState().with(direction, true));
            ++vines;
            pos = pos.down();
        }
    }
}
