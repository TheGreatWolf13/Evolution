package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.NutrientHelper;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockGrass extends BlockGenericSlowable implements IRockVariant {

    private final RockVariant variant;

    public BlockGrass(RockVariant variant) {
        super(Properties.of(Material.GRASS).strength(3.0F, 0.6F).sound(SoundType.GRASS).randomTicks(), variant.getMass() / 4);
        this.variant = variant;
    }

    private static boolean canSustainGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos posUp = pos.above();
        BlockState stateUp = level.getBlockState(posUp);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockMolding/* || stateUp.getBlock() instanceof BlockShadowHound*/) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockPitKiln && stateUp.getValue(LAYERS_0_16) < 9) {
            return true;
        }
        if (BlockUtils.hasSolidSide(level, posUp, Direction.DOWN)) {
            return false;
        }
        return level.getMaxLocalRawBrightness(posUp) >= 9;
    }

    private static boolean canSustainGrassWater(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos posUp = pos.above();
        return canSustainGrass(state, level, pos) && !level.getFluidState(posUp).is(FluidTags.WATER);
    }

    @Override
    public int beamSize() {
        return 1;
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        if (state.getValue(SNOWY)) {
            return 0.25F;
        }
        return 0.35F;
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        if (this == EvolutionBlocks.GRASS_PEAT.get()) {
            return EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 4);
        }
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
                if (BlockUtils.hasSolidSide(level, fromPos, Direction.DOWN) &&
                    !(level.getBlockState(fromPos)
                           .getBlock() instanceof BlockMolding/* || worldIn.getBlockState(fromPos).getBlock() instanceof BlockShadowHound*/) &&
                    !(level.getBlockState(fromPos).getBlock() instanceof BlockPitKiln && level.getBlockState(fromPos).getValue(LAYERS_0_16) < 9)) {
                    level.setBlockAndUpdate(pos,
                                            this == EvolutionBlocks.GRASS_PEAT.get() ?
                                            EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 4) :
                                            this.variant.getDirt().defaultBlockState());
                    CapabilityChunkStorage.addElements(level.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
                    for (Direction direction : DirectionUtil.HORIZ_NESW) {
                        BlockPos offset = pos.relative(direction);
                        Block blockAtOffset = level.getBlockState(offset).getBlock();
                        if (blockAtOffset instanceof BlockGrass) {
                            level.scheduleTick(offset, blockAtOffset, 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (this != EvolutionBlocks.GRASS_PEAT.get() || player.isCreative()) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
        level.setBlockAndUpdate(pos, EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 3));
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (isMoving) {
            return;
        }
        CapabilityChunkStorage.addElements(level.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!level.isClientSide) {
            if (!level.isAreaLoaded(pos, 3)) {
                return;
            }
            if (random.nextInt(4) == 0) {
                if (!canSustainGrass(state, level, pos)) {
                    level.setBlockAndUpdate(pos,
                                            this == EvolutionBlocks.GRASS_PEAT.get() ?
                                            EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 4) :
                                            this.variant.getDirt().defaultBlockState());
                }
                else {
                    if (level.getBrightness(LightLayer.SKY, pos.above()) >= 9) {
                        BlockState placeState = this.defaultBlockState();
                        for (int i = 0; i < 4; ++i) {
                            BlockPos randomPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                            BlockState stateAtPos = level.getBlockState(randomPos);
                            if ((stateAtPos.getBlock() instanceof BlockDirt ||
                                 stateAtPos.getBlock() instanceof BlockDryGrass ||
                                 stateAtPos.getBlock() instanceof BlockPeat) && canSustainGrassWater(placeState, level, randomPos)) {
                                if (stateAtPos.getBlock() instanceof BlockPeat) {
                                    if (stateAtPos.getValue(LAYERS_1_4) != 4) {
                                        return;
                                    }
                                }
                                if (CapabilityChunkStorage.removeElements(level.getChunkAt(randomPos), NutrientHelper.GROW_GRASS_BLOCK)) {
                                    if (stateAtPos.getBlock() instanceof BlockPeat) {
                                        //TODO proper snow
                                        level.setBlockAndUpdate(randomPos,
                                                                EvolutionBlocks.GRASS_PEAT.get()
                                                                                          .defaultBlockState()
                                                                                          .setValue(SNOWY,
                                                                                                    level.getBlockState(randomPos.above())
                                                                                                         .getBlock() == Blocks.SNOW));
                                    }
                                    else {
                                        //TODO proper snow
                                        level.setBlockAndUpdate(randomPos,
                                                                ((IRockVariant) stateAtPos.getBlock()).getVariant()
                                                                                                      .getGrass()
                                                                                                      .defaultBlockState()
                                                                                                      .setValue(SNOWY,
                                                                                                                level.getBlockState(randomPos.above())
                                                                                                                     .getBlock() == Blocks.SNOW));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (random.nextInt(200) == 0) {
                BlockPos posUp = pos.above();
                if (level.getBlockState(posUp).isAir()) {
                    if (CapabilityChunkStorage.removeElements(level.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS)) {
                        level.setBlock(posUp, EvolutionBlocks.GRASS.get().defaultBlockState(), BlockFlags.BLOCK_UPDATE);
                    }
                }
                else if (level.getBlockState(posUp).getBlock() instanceof BlockTallGrass) {
                    if (CapabilityChunkStorage.removeElements(level.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS_2)) {
                        level.setBlock(posUp,
                                       EvolutionBlocks.TALLGRASS.get().defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER),
                                       BlockFlags.BLOCK_UPDATE);
                        level.setBlock(pos.above(2),
                                       EvolutionBlocks.TALLGRASS.get().defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER),
                                       BlockFlags.BLOCK_UPDATE);
                    }
                }
            }
        }
    }
}
