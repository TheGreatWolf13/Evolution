package tgw.evolution.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.NutrientHelper;
import tgw.evolution.util.RockVariant;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockGrass extends BlockGenericSlowable implements IRockVariant {

    private final RockVariant variant;

    public BlockGrass(RockVariant variant) {
        super(Properties.of(Material.GRASS).strength(3.0F, 0.6F).sound(SoundType.GRASS).randomTicks(), variant.getMass() / 4);
        this.variant = variant;
    }

    private static boolean canSustainGrass(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos posUp = pos.above();
        BlockState stateUp = world.getBlockState(posUp);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.getValue(SnowBlock.LAYERS) == 1) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockMolding/* || stateUp.getBlock() instanceof BlockShadowHound*/) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockPitKiln && stateUp.getValue(LAYERS_0_16) < 9) {
            return true;
        }
        if (BlockUtils.hasSolidSide(world, posUp, Direction.DOWN)) {
            return false;
        }
        int i = LightEngine.getLightBlockInto(world, state, pos, stateUp, posUp, Direction.UP, stateUp.getLightBlock(world, posUp));
        return i < world.getMaxLightLevel();
    }

    private static boolean canSustainGrassWater(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos posUp = pos.above();
        return canSustainGrass(state, world, pos) && !world.getFluidState(posUp).is(FluidTags.WATER);
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
        if (!world.isClientSide) {
            if (pos.above().equals(fromPos)) {
                if (BlockUtils.hasSolidSide(world, fromPos, Direction.DOWN) &&
                    !(world.getBlockState(fromPos)
                           .getBlock() instanceof BlockMolding/* || worldIn.getBlockState(fromPos).getBlock() instanceof BlockShadowHound*/) &&
                    !(world.getBlockState(fromPos).getBlock() instanceof BlockPitKiln && world.getBlockState(fromPos).getValue(LAYERS_0_16) < 9)) {
                    world.setBlockAndUpdate(pos,
                                            this == EvolutionBlocks.GRASS_PEAT.get() ?
                                            EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 4) :
                                            this.variant.getDirt().defaultBlockState());
                    CapabilityChunkStorage.addElements(world.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
                    for (Direction direction : DirectionUtil.HORIZ_NESW) {
                        BlockPos offset = pos.relative(direction);
                        Block block = world.getBlockState(offset).getBlock();
                        if (block instanceof BlockGrass) {
                            world.getBlockTicks().scheduleTick(offset, block, 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (isMoving) {
            return;
        }
        CapabilityChunkStorage.addElements(worldIn.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isClientSide) {
            if (!world.isAreaLoaded(pos, 3)) {
                return;
            }
            if (random.nextInt(4) == 0) {
                if (!canSustainGrass(state, world, pos)) {
                    world.setBlockAndUpdate(pos,
                                            this == EvolutionBlocks.GRASS_PEAT.get() ?
                                            EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 4) :
                                            this.variant.getDirt().defaultBlockState());
                }
                else {
                    if (world.getBrightness(LightType.SKY, pos.above()) >= 9) {
                        BlockState placeState = this.defaultBlockState();
                        for (int i = 0; i < 4; ++i) {
                            BlockPos randomPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                            BlockState stateAtPos = world.getBlockState(randomPos);
                            if ((stateAtPos.getBlock() instanceof BlockDirt ||
                                 stateAtPos.getBlock() instanceof BlockDryGrass ||
                                 stateAtPos.getBlock() instanceof BlockPeat) && canSustainGrassWater(placeState, world, randomPos)) {
                                if (stateAtPos.getBlock() instanceof BlockPeat) {
                                    if (stateAtPos.getValue(LAYERS_1_4) != 4) {
                                        return;
                                    }
                                }
                                if (CapabilityChunkStorage.removeElements(world.getChunkAt(randomPos), NutrientHelper.GROW_GRASS_BLOCK)) {
                                    if (stateAtPos.getBlock() instanceof BlockPeat) {
                                        //TODO proper snow
                                        world.setBlockAndUpdate(randomPos,
                                                                EvolutionBlocks.GRASS_PEAT.get()
                                                                                          .defaultBlockState()
                                                                                          .setValue(SNOWY,
                                                                                                    world.getBlockState(randomPos.above())
                                                                                                         .getBlock() == Blocks.SNOW));
                                    }
                                    else {
                                        //TODO proper snow
                                        world.setBlockAndUpdate(randomPos,
                                                                ((IRockVariant) stateAtPos.getBlock()).getVariant()
                                                                                                      .getGrass()
                                                                                                      .defaultBlockState()
                                                                                                      .setValue(SNOWY,
                                                                                                                world.getBlockState(randomPos.above())
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
                if (world.getBlockState(posUp).isAir()) {
                    if (CapabilityChunkStorage.removeElements(world.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS)) {
                        world.setBlock(posUp, EvolutionBlocks.GRASS.get().defaultBlockState(), BlockFlags.BLOCK_UPDATE);
                    }
                }
                else if (world.getBlockState(posUp).getBlock() instanceof BlockTallGrass) {
                    if (CapabilityChunkStorage.removeElements(world.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS_2)) {
                        world.setBlock(posUp,
                                       EvolutionBlocks.TALLGRASS.get().defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER),
                                       BlockFlags.BLOCK_UPDATE);
                        world.setBlock(pos.above(2),
                                       EvolutionBlocks.TALLGRASS.get().defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER),
                                       BlockFlags.BLOCK_UPDATE);
                    }
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if (this != EvolutionBlocks.GRASS_PEAT.get() || player.isCreative()) {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        world.setBlockAndUpdate(pos, EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 3));
        return true;
    }
}
