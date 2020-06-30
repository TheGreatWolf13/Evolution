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
import net.minecraftforge.common.IPlantable;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.blocks.BlockSapling;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class TreeFeature extends AbstractTreeFeature<NoFeatureConfig> {

    private static final BlockState DEFAULT_TRUNK = EvolutionBlocks.LOG_OAK.get().getDefaultState().with(BlockLog.TREE, true);
    private static final BlockState DEFAULT_LEAF = EvolutionBlocks.LEAVES_OAK.get().getDefaultState();
    /**
     * The minimum height of a generated tree.
     */
    protected final int minTreeHeight;
    /**
     * True if this tree should grow Vines.
     */
    private final BlockState stateWood;
    private final BlockState stateLeaves;

    public TreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory, boolean notify) {
        this(configFactory, notify, 4, DEFAULT_TRUNK, DEFAULT_LEAF);
    }

    public TreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory, boolean notify, int minTreeHeightIn, BlockState woodState, BlockState leavesState) {
        super(configFactory, notify);
        this.minTreeHeight = minTreeHeightIn;
        this.stateWood = woodState;
        this.stateLeaves = leavesState;
        this.sapling = (IPlantable) EvolutionBlocks.SAPLING_OAK.get();
    }

    @Override
    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos position, MutableBoundingBox box) {
        int treeHeight = this.treeHeight(rand);
        if (position.getY() >= 1 && position.getY() + treeHeight + 1 <= ((IWorld) worldIn).getWorld().getHeight()) {
            boolean flag = true;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int j = position.getY(); j <= position.getY() + 1 + treeHeight; ++j) {
                int k = 1;
                if (j == position.getY()) {
                    k = 0;
                }
                if (j >= position.getY() + 1 + treeHeight - 2) {
                    k = 2;
                }
                for (int l = position.getX() - k; l <= position.getX() + k && flag; ++l) {
                    for (int i1 = position.getZ() - k; i1 <= position.getZ() + k && flag; ++i1) {
                        if (j >= 0 && j < ((IWorld) worldIn).getWorld().getHeight()) {
                            if (!BlockSapling.canGrowInto(worldIn, mutablePos.setPos(l, j, i1))) {
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
            if (BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(position.down()), this.sapling) && position.getY() < ((IWorld) worldIn).getWorld().getHeight() - treeHeight - 1) {
                this.setDirtAt(worldIn, position.down(), position);
                for (int placingLogs = 0; placingLogs < treeHeight; ++placingLogs) {
                    BlockState stateForLog = ((IBlockReader) worldIn).getBlockState(position.up(placingLogs));
                    if (stateForLog.canBeReplacedByLogs((IWorldReader) worldIn, position.up(placingLogs))) {
                        this.setLogState(changedBlocks, worldIn, position.up(placingLogs), this.stateWood, box);
                    }
                }
                BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
                for (int leafY = position.getY() - 3 + treeHeight; leafY <= position.getY() + treeHeight; ++leafY) {
                    int i4 = leafY - (position.getY() + treeHeight);
                    int j1 = 1 - i4 / 2;
                    for (int leafX = position.getX() - j1; leafX <= position.getX() + j1; ++leafX) {
                        int l1 = leafX - position.getX();
                        for (int leafZ = position.getZ() - j1; leafZ <= position.getZ() + j1; ++leafZ) {
                            int j2 = leafZ - position.getZ();
                            if (Math.abs(l1) != j1 || Math.abs(j2) != j1 || rand.nextInt(2) != 0 && i4 != 0) {
                                BlockState stateForLeaf = ((IBlockReader) worldIn).getBlockState(leafPos.setPos(leafX, leafY, leafZ));
                                if (stateForLeaf.canBeReplacedByLeaves((IWorldReader) worldIn, leafPos)) {
                                    this.setBlockState(worldIn, leafPos, this.stateLeaves);
                                }
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    protected int treeHeight(Random rand) {
        return this.minTreeHeight + rand.nextInt(3);
    }

    @Override
    public TreeFeature setSapling(IPlantable sapling) {
        this.sapling = sapling;
        return this;
    }
}
