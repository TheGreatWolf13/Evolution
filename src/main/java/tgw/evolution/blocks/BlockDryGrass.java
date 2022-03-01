package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.SNOWY;

public class BlockDryGrass extends BlockGenericSlowable implements IRockVariant {

    private final RockVariant variant;

    public BlockDryGrass(RockVariant variant) {
        super(Properties.of(Material.GRASS).strength(3.0F, 0.6F).sound(SoundType.GRASS).randomTicks(), variant.getMass() / 4);
        this.variant = variant;
    }

    private static boolean canSustainGrass(BlockGetter level, BlockPos pos) {
        BlockPos posUp = pos.above();
        BlockState stateUp = level.getBlockState(posUp);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        return !BlockUtils.hasSolidSide(level, posUp, Direction.DOWN);
    }

    private static boolean canSustainGrassWater(BlockGetter level, BlockPos pos) {
        BlockPos posUp = pos.above();
        return canSustainGrass(level, pos) && !level.getFluidState(posUp).is(FluidTags.WATER);
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            if (pos.above().equals(fromPos)) {
                if (BlockUtils.hasSolidSide(level, fromPos, Direction.DOWN)) {
                    level.setBlockAndUpdate(pos, this.variant.getDirt().defaultBlockState());
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!level.isAreaLoaded(pos, 3)) {
            return;
        }
        if (random.nextInt(2) == 0) {
            if (!canSustainGrass(level, pos)) {
                level.setBlockAndUpdate(pos, this.variant.getDirt().defaultBlockState());
            }
            else {
                for (int i = 0; i < 4; ++i) {
                    BlockPos randomPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    Block blockAtPos = level.getBlockState(randomPos).getBlock();
                    if (blockAtPos instanceof BlockDirt && canSustainGrassWater(level, randomPos)) {
                        //TODO proper snow
                        level.setBlockAndUpdate(randomPos,
                                                ((IRockVariant) blockAtPos).getVariant()
                                                                           .getDryGrass()
                                                                           .defaultBlockState()
                                                                           .setValue(SNOWY,
                                                                                     level.getBlockState(randomPos.above()).getBlock() ==
                                                                                     Blocks.SNOW));
                    }
                }
            }
        }
    }
}
