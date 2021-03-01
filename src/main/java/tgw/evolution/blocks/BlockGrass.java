package tgw.evolution.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.lighting.LightEngine;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.NutrientHelper;
import tgw.evolution.util.RockVariant;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockGrass extends BlockGenericSlowable implements IStoneVariant {

    private final RockVariant variant;

    public BlockGrass(RockVariant variant) {
        super(Block.Properties.create(Material.ORGANIC).hardnessAndResistance(3.0F, 0.6F).sound(SoundType.PLANT).tickRandomly(),
              variant.getMass() / 4);
        this.variant = variant;
    }

    private static boolean canSustainGrass(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos posUp = pos.up();
        BlockState stateUp = world.getBlockState(posUp);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.get(SnowBlock.LAYERS) == 1) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockMolding/* || stateUp.getBlock() instanceof BlockShadowHound*/) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockPitKiln && stateUp.get(LAYERS_0_16) < 9) {
            return true;
        }
        if (Block.hasSolidSide(stateUp, world, posUp, Direction.DOWN)) {
            return false;
        }
        int i = LightEngine.func_215613_a(world, state, pos, stateUp, posUp, Direction.UP, stateUp.getOpacity(world, posUp));
        return i < world.getMaxLightLevel();
    }

    private static boolean canSustainGrassWater(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos posUp = pos.up();
        return canSustainGrass(state, worldIn, pos) && !worldIn.getFluidState(posUp).isTagged(FluidTags.WATER);
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
        if (state.get(SNOWY)) {
            return 0.25F;
        }
        return 0.35F;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        if (this == EvolutionBlocks.GRASS_PEAT.get()) {
            return EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS_1_4, 4);
        }
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
        if (!world.isRemote) {
            if (pos.up().equals(fromPos)) {
                if (Block.hasSolidSide(world.getBlockState(fromPos), world, fromPos, Direction.DOWN) &&
                    !(world.getBlockState(fromPos)
                           .getBlock() instanceof BlockMolding/* || worldIn.getBlockState(fromPos).getBlock() instanceof BlockShadowHound*/) &&
                    !(world.getBlockState(fromPos).getBlock() instanceof BlockPitKiln && world.getBlockState(fromPos).get(LAYERS_0_16) < 9)) {
                    world.setBlockState(pos,
                                        this == EvolutionBlocks.GRASS_PEAT.get() ?
                                        EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS_1_4, 4) :
                                        this.variant.getDirt().getDefaultState(),
                                        3);
                    ChunkStorageCapability.addElements(world.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
                    for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
                        BlockPos offset = pos.offset(direction);
                        Block block = world.getBlockState(offset).getBlock();
                        if (block instanceof BlockGrass) {
                            world.getPendingBlockTicks().scheduleTick(offset, block, 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (isMoving) {
            return;
        }
        ChunkStorageCapability.addElements(worldIn.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
    }

    @Override
    public void randomTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isRemote) {
            if (!world.isAreaLoaded(pos, 3)) {
                return;
            }
            if (random.nextInt(4) == 0) {
                if (!canSustainGrass(state, world, pos)) {
                    world.setBlockState(pos,
                                        this == EvolutionBlocks.GRASS_PEAT.get() ?
                                        EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS_1_4, 4) :
                                        this.variant.getDirt().getDefaultState());
                }
                else {
                    if (world.getLightFor(LightType.SKY, pos.up()) >= 9) {
                        BlockState placeState = this.getDefaultState();
                        for (int i = 0; i < 4; ++i) {
                            BlockPos randomPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                            BlockState stateAtPos = world.getBlockState(randomPos);
                            if ((stateAtPos.getBlock() instanceof BlockDirt ||
                                 stateAtPos.getBlock() instanceof BlockDryGrass ||
                                 stateAtPos.getBlock() instanceof BlockPeat) && canSustainGrassWater(placeState, world, randomPos)) {
                                if (stateAtPos.getBlock() instanceof BlockPeat) {
                                    if (stateAtPos.get(LAYERS_1_4) != 4) {
                                        return;
                                    }
                                }
                                if (ChunkStorageCapability.removeElements(world.getChunkAt(randomPos), NutrientHelper.GROW_GRASS_BLOCK)) {
                                    if (stateAtPos.getBlock() instanceof BlockPeat) {
                                        //TODO proper snow
                                        world.setBlockState(randomPos,
                                                            EvolutionBlocks.GRASS_PEAT.get()
                                                                                      .getDefaultState()
                                                                                      .with(SNOWY,
                                                                                            world.getBlockState(randomPos.up()).getBlock() ==
                                                                                            Blocks.SNOW));
                                    }
                                    else {
                                        //TODO proper snow
                                        world.setBlockState(randomPos,
                                                            ((IStoneVariant) stateAtPos.getBlock()).getVariant()
                                                                                                   .getGrass()
                                                                                                   .getDefaultState()
                                                                                                   .with(SNOWY,
                                                                                                         world.getBlockState(randomPos.up())
                                                                                                              .getBlock() == Blocks.SNOW));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (random.nextInt(200) == 0) {
                if (world.getBlockState(pos.up()).isAir()) {
                    if (ChunkStorageCapability.removeElements(world.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS)) {
                        world.setBlockState(pos.up(), EvolutionBlocks.GRASS.get().getDefaultState(), BlockFlags.BLOCK_UPDATE);
                    }
                }
                else if (world.getBlockState(pos.up()).getBlock() instanceof BlockTallGrass) {
                    if (ChunkStorageCapability.removeElements(world.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS_2)) {
                        world.setBlockState(pos.up(),
                                            EvolutionBlocks.TALLGRASS.get().getDefaultState().with(HALF, DoubleBlockHalf.LOWER),
                                            BlockFlags.BLOCK_UPDATE);
                        world.setBlockState(pos.up(2),
                                            EvolutionBlocks.TALLGRASS.get().getDefaultState().with(HALF, DoubleBlockHalf.UPPER),
                                            BlockFlags.BLOCK_UPDATE);
                    }
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        if (this != EvolutionBlocks.GRASS_PEAT.get() || player.isCreative()) {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS_1_4, BlockFlags.NOTIFY_AND_UPDATE));
        return true;
    }
}
