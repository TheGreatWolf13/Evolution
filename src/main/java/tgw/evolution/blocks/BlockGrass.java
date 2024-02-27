package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.LAYERS_0_16;
import static tgw.evolution.init.EvolutionBStates.SNOWY;

public class BlockGrass extends BlockGenericSnowable implements IRockVariant {

    private final RockVariant variant;

    public BlockGrass(RockVariant variant) {
        super(Properties.of(Material.DIRT).color(MaterialColor.GRASS).strength(3.0F, 0.6F).sound(SoundType.GRASS).randomTicks(),
              variant.getMass() / 4);
        this.variant = variant;
    }

    private static boolean canSustainGrass(LevelReader level, int x, int y, int z) {
        BlockState stateUp = level.getBlockState_(x, y + 1, z);
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
        if (BlockUtils.hasSolidFace(level, x, y + 1, z, Direction.DOWN)) {
            return false;
        }
        return level.getMaxLocalRawBrightness_(x, y + 1, z) >= 9;
    }

    private static boolean canSustainGrassWater(LevelReader level, int x, int y, int z) {
        return canSustainGrass(level, x, y, z) && !level.getFluidState_(x, y + 1, z).is(FluidTags.WATER);
    }

//    @Override
//    public int beamSize() {
//        return 1;
//    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        if (state.getValue(SNOWY)) {
            return 0.5F;
        }
        return 0.6F;
    }

//    @Override

    @Override
    public double getMass(Level level, int x, int y, int z, BlockState state) {
        //TODO implementation
        return 0;
    }
//    public BlockState getStateForFalling(BlockState state) {
//        if (this == EvolutionBlocks.ALL_GRASS.get(RockVariant.PEAT).get()) {
//            return EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, 4);
//        }
//        return this.variant.getDirt().defaultBlockState();
//    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        super.neighborChanged_(state, level, x, y, z, oldBlock, fromX, fromY, fromZ, isMoving);
        if (!level.isClientSide) {
            if (x == fromX && z == fromZ && y + 1 == fromY) {
                BlockState stateUp = level.getBlockState_(x, y + 1, z);
                if (stateUp.isFaceSturdy_(level, x, y + 1, z, Direction.DOWN)) {
                    Block blockUp = stateUp.getBlock();
                    if (!(blockUp instanceof BlockMolding) && !(blockUp instanceof BlockPitKiln && stateUp.getValue(LAYERS_0_16) < 9)) {
                        level.setBlockAndUpdate_(x, y, z, /*this == EvolutionBlocks.GRASSES.get(RockVariant.PEAT) ?
                                                                       EvolutionBlocks.PEAT.defaultBlockState().setValue(LAYERS_1_4, 4) :*/
                                                 this.variant.get(EvolutionBlocks.DIRTS).defaultBlockState());
                        for (Direction direction : DirectionUtil.HORIZ_NESW) {
                            int ox = x + direction.getStepX();
                            int oz = z + direction.getStepZ();
                            Block blockAtSide = level.getBlockState_(ox, y, oz).getBlock();
                            if (blockAtSide instanceof BlockGrass) {
                                BlockUtils.scheduleBlockTick(level, ox, y, oz);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        //TODO
//        if (!level.isClientSide) {
//            if (random.nextInt(4) == 0) {
//                if (!canSustainGrass(level, x, y, z)) {
//                    level.setBlockAndUpdate_(x, y, z, this == EvolutionBlocks.GRASSES.get(RockVariant.PEAT) ?
//                                                      EvolutionBlocks.PEAT.defaultBlockState().setValue(LAYERS_1_4, 4) :
//                                                      this.variant.get(EvolutionBlocks.DIRTS).defaultBlockState());
//                }
//                else {
//                    if (level.getBrightness_(Lightlayer.SKY, BlockPos.asLong(x, y + 1, z)) >= 9) {
//                        BlockState placeState = this.defaultBlockState();
//                        for (int i = 0; i < 4; ++i) {
//                            BlockPos randomPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
//                            BlockState stateAtPos = level.getBlockState(randomPos);
//                            Block blockAtPos = stateAtPos.getBlock();
//                            if ((blockAtPos instanceof BlockDirt || blockAtPos instanceof BlockDryGrass || blockAtPos instanceof BlockPeat) &&
//                                canSustainGrassWater(level, randomPos.getX(), randomPos.getY(), randomPos.getZ())) {
//                                if (blockAtPos instanceof BlockPeat) {
//                                    if (stateAtPos.getValue(LAYERS_1_4) != 4) {
//                                        return;
//                                    }
//                                    //TODO proper snow
//                                    level.setBlockAndUpdate(randomPos, EvolutionBlocks.GRASSES.get(RockVariant.PEAT)
//                                                                                              .defaultBlockState()
//                                                                                              .setValue(SNOWY,
//                                                                                                        level.getBlockState(randomPos.above())
//                                                                                                             .getBlock() == Blocks.SNOW));
//                                }
//                                else {
//                                    //TODO proper snow
//                                    IRockVariant rockVariant = (IRockVariant) blockAtPos;
//                                    level.setBlockAndUpdate(randomPos, rockVariant.rockVariant()
//                                                                                  .get(EvolutionBlocks.GRASSES)
//                                                                                  .defaultBlockState()
//                                                                                  .setValue(SNOWY,
//                                                                                            level.getBlockState(randomPos.above()).getBlock() ==
//                                                                                            Blocks.SNOW));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            if (random.nextInt(200) == 0) {
//                BlockPos posUp = pos.above();
//                if (level.getBlockState(posUp).isAir()) {
//                    level.setBlock(posUp, EvolutionBlocks.TALLGRASS.defaultBlockState(), BlockFlags.BLOCK_UPDATE);
//                }
//                else if (level.getBlockState(posUp).getBlock() instanceof BlockTallGrass) {
//                    level.setBlock(posUp, EvolutionBlocks.TALLGRASS_HIGH.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER),
//                                   BlockFlags.BLOCK_UPDATE);
//                    level.setBlock(pos.above(2), EvolutionBlocks.TALLGRASS_HIGH.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER),
//                                   BlockFlags.BLOCK_UPDATE);
//                }
//            }
//        }
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }
}
