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
import tgw.evolution.util.RockVariant;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.SNOWY;

public class BlockDryGrass extends BlockGenericSlowable implements IStoneVariant {

    private final RockVariant variant;

    public BlockDryGrass(RockVariant variant) {
        super(Block.Properties.create(Material.ORGANIC).hardnessAndResistance(3.0F, 0.6F).sound(SoundType.PLANT).tickRandomly(),
              variant.getMass() / 4);
        this.variant = variant;
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

    private static boolean canSustainGrassWater(IWorldReader world, BlockPos pos) {
        BlockPos posUp = pos.up();
        return canSustainGrass(world, pos) && !world.getFluidState(posUp).isTagged(FluidTags.WATER);
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
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
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public boolean isSolid(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        if (!world.isRemote) {
            if (pos.up().equals(fromPos)) {
                if (Block.hasSolidSide(world.getBlockState(fromPos), world, fromPos, Direction.DOWN)) {
                    world.setBlockState(pos, this.variant.getDirt().getDefaultState(), 3);
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isRemote) {
            if (!world.isAreaLoaded(pos, 3)) {
                return;
            }
            if (random.nextInt(2) == 0) {
                if (!canSustainGrass(world, pos)) {
                    world.setBlockState(pos, this.variant.getDirt().getDefaultState());
                }
                else {
                    for (int i = 0; i < 4; ++i) {
                        BlockPos randomPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                        Block blockAtPos = world.getBlockState(randomPos).getBlock();
                        if (blockAtPos instanceof BlockDirt && canSustainGrassWater(world, randomPos)) {
                            //TODO proper snow
                            world.setBlockState(randomPos,
                                                ((IStoneVariant) blockAtPos).getVariant()
                                                                            .getDryGrass()
                                                                            .getDefaultState()
                                                                            .with(SNOWY,
                                                                                  world.getBlockState(randomPos.up()).getBlock() == Blocks.SNOW));
                        }
                    }
                }
            }
        }
    }
}
