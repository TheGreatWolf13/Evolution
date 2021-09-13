package tgw.evolution.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.RockVariant;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.SNOWY;

public class BlockDryGrass extends BlockGenericSlowable implements IRockVariant {

    private final RockVariant variant;

    public BlockDryGrass(RockVariant variant) {
        super(Properties.of(Material.GRASS).strength(3.0F, 0.6F).sound(SoundType.GRASS).randomTicks(), variant.getMass() / 4);
        this.variant = variant;
    }

    private static boolean canSustainGrass(IWorldReader world, BlockPos pos) {
        BlockPos posUp = pos.above();
        BlockState stateUp = world.getBlockState(posUp);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.getValue(SnowBlock.LAYERS) == 1) {
            return true;
        }
        return !BlockUtils.hasSolidSide(world, posUp, Direction.DOWN);
    }

    private static boolean canSustainGrassWater(IWorldReader world, BlockPos pos) {
        BlockPos posUp = pos.above();
        return canSustainGrass(world, pos) && !world.getFluidState(posUp).is(FluidTags.WATER);
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.45f;
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        return this.variant.getDirt().defaultBlockState();
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        if (!world.isClientSide) {
            if (pos.above().equals(fromPos)) {
                if (BlockUtils.hasSolidSide(world, fromPos, Direction.DOWN)) {
                    world.setBlockAndUpdate(pos, this.variant.getDirt().defaultBlockState());
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isAreaLoaded(pos, 3)) {
            return;
        }
        if (random.nextInt(2) == 0) {
            if (!canSustainGrass(world, pos)) {
                world.setBlockAndUpdate(pos, this.variant.getDirt().defaultBlockState());
            }
            else {
                for (int i = 0; i < 4; ++i) {
                    BlockPos randomPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    Block blockAtPos = world.getBlockState(randomPos).getBlock();
                    if (blockAtPos instanceof BlockDirt && canSustainGrassWater(world, randomPos)) {
                        //TODO proper snow
                        world.setBlockAndUpdate(randomPos,
                                                ((IRockVariant) blockAtPos).getVariant()
                                                                           .getDryGrass()
                                                                           .defaultBlockState()
                                                                           .setValue(SNOWY,
                                                                                     world.getBlockState(randomPos.above()).getBlock() ==
                                                                                     Blocks.SNOW));
                    }
                }
            }
        }
    }
}
