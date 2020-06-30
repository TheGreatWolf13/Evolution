package tgw.evolution.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;

import java.util.Random;

public class BlockDryGrass extends BlockSnowable implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public BlockDryGrass(EnumRockNames name) {
        super(Block.Properties.create(Material.ORGANIC).hardnessAndResistance(3F, 0.6F).sound(SoundType.PLANT).tickRandomly(), name.getMass() / 4);
        this.name = name;
    }

    private static boolean canSustainGrass(IWorldReader worldIn, BlockPos pos) {
        BlockPos posUp = pos.up();
        BlockState stateUp = worldIn.getBlockState(posUp);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.get(SnowBlock.LAYERS) == 1) {
            return true;
        }
        return !hasSolidSide(stateUp, worldIn, posUp, Direction.DOWN);
    }

    private static boolean canSustainGrassWater(IWorldReader worldIn, BlockPos pos) {
        BlockPos posUp = pos.up();
        return canSustainGrass(worldIn, pos) && !worldIn.getFluidState(posUp).isTagged(FluidTags.WATER);
    }

    @Override
    public EnumRockVariant getVariant() {
        return this.variant;
    }

    @Override
    public void setVariant(EnumRockVariant variant) {
        this.variant = variant;
    }

    @Override
    public EnumRockNames getStoneName() {
        return this.name;
    }

    @Override
    public boolean isSolid(BlockState state) {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        return this.variant.getDirt().getDefaultState();
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (!worldIn.isRemote) {
            if (pos.up().equals(fromPos)) {
                if (Block.hasSolidSide(worldIn.getBlockState(fromPos), worldIn, fromPos, Direction.DOWN)) {
                    worldIn.setBlockState(pos, this.variant.getDirt().getDefaultState(), 3);
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            if (!worldIn.isAreaLoaded(pos, 3)) {
                return;
            }
            if (random.nextInt(2) == 0) {
                if (!canSustainGrass(worldIn, pos)) {
                    worldIn.setBlockState(pos, this.variant.getDirt().getDefaultState());
                }
                else {
                    for (int i = 0; i < 4; ++i) {
                        BlockPos randomPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                        Block blockAtPos = worldIn.getBlockState(randomPos).getBlock();
                        if (blockAtPos instanceof BlockDirt && canSustainGrassWater(worldIn, randomPos)) {
                            //TODO proper snow
                            worldIn.setBlockState(randomPos, ((IStoneVariant) blockAtPos).getVariant().getDryGrass().getDefaultState().with(SNOWY, worldIn.getBlockState(randomPos.up()).getBlock() == Blocks.SNOW));
                        }
                    }
                }
            }
        }
    }
}
