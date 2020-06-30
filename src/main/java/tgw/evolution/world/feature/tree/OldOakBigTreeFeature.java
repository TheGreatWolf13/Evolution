package tgw.evolution.world.feature.tree;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.blocks.BlockSapling;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class OldOakBigTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {

    private static final BlockState LOG = EvolutionBlocks.LOG_OLD_OAK.get().getDefaultState().with(BlockLog.TREE, true);
    private static final BlockState LEAVES = EvolutionBlocks.LEAVES_OLD_OAK.get().getDefaultState();

    public OldOakBigTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean notify) {
        super(config, notify);
    }

    private static boolean placeTreeOfHeight(IWorldGenerationReader worldIn, BlockPos pos, int height) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (int l = 0; l <= height + 1; ++l) {
            int i1 = 1;
            if (l == 0) {
                i1 = 0;
            }
            if (l >= height - 1) {
                i1 = 2;
            }
            for (int j1 = -i1; j1 <= i1; ++j1) {
                for (int k1 = -i1; k1 <= i1; ++k1) {
                    if (!BlockSapling.canGrowInto(worldIn, blockpos$mutableblockpos.setPos(posX + j1, posY + l, posZ + k1))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos position, MutableBoundingBox box) {
        int trunkHeight = rand.nextInt(3) + rand.nextInt(2) + 6;
        int posX = position.getX();
        int posY = position.getY();
        int posZ = position.getZ();
        //tree within world height limit
        if (posY >= 1 && posY + trunkHeight + 1 < 256) {
            BlockPos posDown = position.down();
            boolean isSoil = BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(posDown), (BlockSapling) EvolutionBlocks.SAPLING_OLD_OAK.get());
            if (!isSoil) {
                return false;
            }
            if (!OldOakBigTreeFeature.placeTreeOfHeight(worldIn, position, trunkHeight)) {
                return false;
            }
            //the NW corner sapling grows first
            this.setDirtAt(worldIn, posDown, posDown.up());
            this.setDirtAt(worldIn, posDown.east(), posDown.up());
            this.setDirtAt(worldIn, posDown.south(), posDown.up());
            this.setDirtAt(worldIn, posDown.south().east(), posDown.up());
            Direction topInclination = Direction.Plane.HORIZONTAL.random(rand);
            int inclinationHeightStart = trunkHeight - rand.nextInt(4);
            int inclinationLimit = 2 - rand.nextInt(3);
            int changedPosX = posX;
            int changedPosZ = posZ;
            BlockPos.MutableBlockPos trunkPos = new BlockPos.MutableBlockPos();
            for (int placingTrunks = 0; placingTrunks < trunkHeight; ++placingTrunks) {
                if (placingTrunks >= inclinationHeightStart && inclinationLimit > 0) {
                    changedPosX += topInclination.getXOffset();
                    changedPosZ += topInclination.getZOffset();
                    --inclinationLimit;
                }
                //placing main trunk
                int posYForPlacement = posY + placingTrunks;
                trunkPos.setPos(changedPosX, posYForPlacement, changedPosZ);
                BlockState stateTrunk = ((IBlockReader) worldIn).getBlockState(trunkPos);
                if (stateTrunk.isAir((IBlockReader) worldIn, trunkPos) || stateTrunk.isIn(BlockTags.LEAVES)) {
                    this.placeTrunk(changedBlocks, worldIn, trunkPos, box);
                    this.placeTrunk(changedBlocks, worldIn, trunkPos.east(), box);
                    this.placeTrunk(changedBlocks, worldIn, trunkPos.south(), box);
                    this.placeTrunk(changedBlocks, worldIn, trunkPos.east().south(), box);
                }
            }
            //making bottom and top leaf layer
            int changedPosY = posY + trunkHeight - 1;
            for (int xAddition = -2; xAddition <= 0; ++xAddition) {
                for (int zAddition = -2; zAddition <= 0; ++zAddition) {
                    int yAddition = -1;
                    this.placeLeaves(worldIn, changedPosX + xAddition, changedPosY + yAddition, changedPosZ + zAddition);
                    this.placeLeaves(worldIn, 1 + changedPosX - xAddition, changedPosY + yAddition, changedPosZ + zAddition);
                    this.placeLeaves(worldIn, changedPosX + xAddition, changedPosY + yAddition, 1 + changedPosZ - zAddition);
                    this.placeLeaves(worldIn, 1 + changedPosX - xAddition, changedPosY + yAddition, 1 + changedPosZ - zAddition);
                    if ((xAddition > -2 || zAddition > -1) && (xAddition != -1 || zAddition != -2)) {
                        yAddition = 1;
                        this.placeLeaves(worldIn, changedPosX + xAddition, changedPosY + yAddition, changedPosZ + zAddition);
                        this.placeLeaves(worldIn, 1 + changedPosX - xAddition, changedPosY + yAddition, changedPosZ + zAddition);
                        this.placeLeaves(worldIn, changedPosX + xAddition, changedPosY + yAddition, 1 + changedPosZ - zAddition);
                        this.placeLeaves(worldIn, 1 + changedPosX - xAddition, changedPosY + yAddition, 1 + changedPosZ - zAddition);
                    }
                }
            }
            //top 2x2 leaves
            if (rand.nextBoolean()) {
                this.placeLeaves(worldIn, changedPosX, changedPosY + 2, changedPosZ);
                this.placeLeaves(worldIn, changedPosX + 1, changedPosY + 2, changedPosZ);
                this.placeLeaves(worldIn, changedPosX + 1, changedPosY + 2, changedPosZ + 1);
                this.placeLeaves(worldIn, changedPosX, changedPosY + 2, changedPosZ + 1);
            }
            //middle leaf layer
            for (int xMiddleAdd = -3; xMiddleAdd <= 4; ++xMiddleAdd) {
                for (int zMiddleAdd = -3; zMiddleAdd <= 4; ++zMiddleAdd) {
                    if ((xMiddleAdd != -3 || zMiddleAdd != -3) && (xMiddleAdd != -3 || zMiddleAdd != 4) && (xMiddleAdd != 4 || zMiddleAdd != -3) && (xMiddleAdd != 4 || zMiddleAdd != 4) && (Math.abs(xMiddleAdd) < 3 || Math.abs(zMiddleAdd) < 3)) {
                        this.placeLeaves(worldIn, changedPosX + xMiddleAdd, changedPosY, changedPosZ + zMiddleAdd);
                    }
                }
            }
            //making branches
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int branchXposAdd = -1; branchXposAdd <= 2; ++branchXposAdd) {
                for (int branchZposAdd = -1; branchZposAdd <= 2; ++branchZposAdd) {
                    if ((branchXposAdd < 0 || branchXposAdd > 1 || branchZposAdd < 0 || branchZposAdd > 1) && rand.nextInt(3) <= 0) {
                        int branchHeight = rand.nextInt(3) + 2;
                        for (int placingBranch = 0; placingBranch < branchHeight; ++placingBranch) {
                            this.placeTrunk(changedBlocks, worldIn, mutablePos.setPos(posX + branchXposAdd, changedPosY - placingBranch - 1, posZ + branchZposAdd), box);
                        }
                        //placing leaves on top
                        for (int leafXAdd = -1; leafXAdd <= 1; ++leafXAdd) {
                            for (int leafZAdd = -1; leafZAdd <= 1; ++leafZAdd) {
                                this.placeLeaves(worldIn, changedPosX + branchXposAdd + leafXAdd, changedPosY, changedPosZ + branchZposAdd + leafZAdd);
                            }
                        }
                        //placing leaves at end
                        for (int leafXAddBottom = -2; leafXAddBottom <= 2; ++leafXAddBottom) {
                            for (int leafZAddBottom = -2; leafZAddBottom <= 2; ++leafZAddBottom) {
                                if (Math.abs(leafXAddBottom) != 2 || Math.abs(leafZAddBottom) != 2) {
                                    this.placeLeaves(worldIn, changedPosX + branchXposAdd + leafXAddBottom, changedPosY - 1, changedPosZ + branchZposAdd + leafZAddBottom);
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void placeTrunk(Set<BlockPos> changedBlocks, IWorldGenerationReader iWorld, BlockPos pos, MutableBoundingBox box) {
        if (BlockSapling.canGrowInto(iWorld, pos)) {
            this.setLogState(changedBlocks, iWorld, pos, LOG, box);
        }
    }

    private void placeLeaves(IWorldGenerationReader iWorld, int posX, int posY, int posZ) {
        BlockPos leafPos = new BlockPos(posX, posY, posZ);
        if (((IBlockReader) iWorld).getBlockState(leafPos).isAir((IBlockReader) iWorld, leafPos)) {
            this.setBlockState(iWorld, leafPos, LEAVES);
        }
    }
}