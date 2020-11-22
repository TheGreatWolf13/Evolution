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
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.NutrientHelper;

import java.util.Random;

public class BlockGrass extends BlockSnowable implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public BlockGrass(EnumRockNames name) {
        super(Block.Properties.create(Material.ORGANIC).hardnessAndResistance(3F, 0.6F).sound(SoundType.PLANT).tickRandomly(), name.getMass() / 4);
        this.name = name;
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
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        if (this != EvolutionBlocks.GRASS_PEAT.get() || player.isCreative()) {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(BlockPeat.LAYERS, 3));
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (!worldIn.isRemote) {
            if (pos.up().equals(fromPos)) {
                if (Block.hasSolidSide(worldIn.getBlockState(fromPos), worldIn, fromPos, Direction.DOWN) &&
                    !(worldIn.getBlockState(fromPos)
                             .getBlock() instanceof BlockMolding/* || worldIn.getBlockState(fromPos).getBlock() instanceof BlockShadowHound*/) &&
                    !(worldIn.getBlockState(fromPos).getBlock() instanceof BlockPitKiln &&
                      worldIn.getBlockState(fromPos).get(EvolutionBlockStateProperties.LAYERS_0_16) < 9)) {
                    worldIn.setBlockState(pos,
                                          this == EvolutionBlocks.GRASS_PEAT.get() ?
                                          EvolutionBlocks.PEAT.get().getDefaultState().with(BlockPeat.LAYERS, 4) :
                                          this.variant.getDirt().getDefaultState(),
                                          3);
                    ChunkStorageCapability.addElements(worldIn.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
                    for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
                        BlockPos offset = pos.offset(direction);
                        Block block = worldIn.getBlockState(offset).getBlock();
                        if (block instanceof BlockGrass) {
                            worldIn.getPendingBlockTicks().scheduleTick(offset, block, 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isSolid(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            if (!worldIn.isAreaLoaded(pos, 3)) {
                return;
            }
            if (random.nextInt(4) == 0) {
                if (!canSustainGrass(state, worldIn, pos)) {
                    worldIn.setBlockState(pos,
                                          this == EvolutionBlocks.GRASS_PEAT.get() ?
                                          EvolutionBlocks.PEAT.get().getDefaultState().with(BlockPeat.LAYERS, 4) :
                                          this.variant.getDirt().getDefaultState());
                }
                else {
                    if (worldIn.getLightFor(LightType.SKY, pos.up()) >= 9) {
                        BlockState placeState = this.getDefaultState();
                        for (int i = 0; i < 4; ++i) {
                            BlockPos randomPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                            BlockState stateAtPos = worldIn.getBlockState(randomPos);
                            if ((stateAtPos.getBlock() instanceof BlockDirt ||
                                 stateAtPos.getBlock() instanceof BlockDryGrass ||
                                 stateAtPos.getBlock() instanceof BlockPeat) && canSustainGrassWater(placeState, worldIn, randomPos)) {
                                if (stateAtPos.getBlock() instanceof BlockPeat) {
                                    if (stateAtPos.get(BlockPeat.LAYERS) != 4) {
                                        return;
                                    }
                                }
                                if (ChunkStorageCapability.removeElements(worldIn.getChunkAt(randomPos), NutrientHelper.GROW_GRASS_BLOCK)) {
                                    if (stateAtPos.getBlock() instanceof BlockPeat) {
                                        //TODO proper snow
                                        worldIn.setBlockState(randomPos,
                                                              EvolutionBlocks.GRASS_PEAT.get()
                                                                                        .getDefaultState()
                                                                                        .with(SNOWY,
                                                                                              worldIn.getBlockState(randomPos.up()).getBlock() ==
                                                                                              Blocks.SNOW));
                                    }
                                    else {
                                        //TODO proper snow
                                        worldIn.setBlockState(randomPos,
                                                              ((IStoneVariant) stateAtPos.getBlock()).getVariant()
                                                                                                     .getGrass()
                                                                                                     .getDefaultState()
                                                                                                     .with(SNOWY,
                                                                                                           worldIn.getBlockState(randomPos.up())
                                                                                                                  .getBlock() == Blocks.SNOW));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (random.nextInt(200) == 0) {
                if (worldIn.getBlockState(pos.up()).isAir()) {
                    if (ChunkStorageCapability.removeElements(worldIn.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS)) {
                        worldIn.setBlockState(pos.up(), EvolutionBlocks.GRASS.get().getDefaultState(), 2);
                    }
                }
                else if (worldIn.getBlockState(pos.up()).getBlock() instanceof BlockTallGrass) {
                    if (ChunkStorageCapability.removeElements(worldIn.getChunkAt(pos), NutrientHelper.GROW_TALL_GRASS_2)) {
                        worldIn.setBlockState(pos.up(),
                                              EvolutionBlocks.TALLGRASS.get().getDefaultState().with(BlockDoublePlant.HALF, DoubleBlockHalf.LOWER),
                                              2);
                        worldIn.setBlockState(pos.up(2),
                                              EvolutionBlocks.TALLGRASS.get().getDefaultState().with(BlockDoublePlant.HALF, DoubleBlockHalf.UPPER),
                                              2);
                    }
                }
            }
        }
    }

    private static boolean canSustainGrass(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos posUp = pos.up();
        BlockState stateUp = worldIn.getBlockState(posUp);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.get(SnowBlock.LAYERS) == 1) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockMolding/* || stateUp.getBlock() instanceof BlockShadowHound*/) {
            return true;
        }
        if (stateUp.getBlock() instanceof BlockPitKiln && stateUp.get(EvolutionBlockStateProperties.LAYERS_0_16) < 9) {
            return true;
        }
        if (Block.hasSolidSide(stateUp, worldIn, posUp, Direction.DOWN)) {
            return false;
        }
        int i = LightEngine.func_215613_a(worldIn, state, pos, stateUp, posUp, Direction.UP, stateUp.getOpacity(worldIn, posUp));
        return i < worldIn.getMaxLightLevel();
    }

    private static boolean canSustainGrassWater(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos posUp = pos.up();
        return canSustainGrass(state, worldIn, pos) && !worldIn.getFluidState(posUp).isTagged(FluidTags.WATER);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (isMoving) {
            return;
        }
        ChunkStorageCapability.addElements(worldIn.getChunkAt(pos), NutrientHelper.DECAY_GRASS_BLOCK);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        if (this == EvolutionBlocks.GRASS_PEAT.get()) {
            return EvolutionBlocks.PEAT.get().getDefaultState().with(BlockPeat.LAYERS, 4);
        }
        return this.variant.getDirt().getDefaultState();
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public int beamSize() {
        return 1;
    }
}
