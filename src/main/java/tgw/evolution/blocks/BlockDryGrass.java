package tgw.evolution.blocks;

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
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.SNOWY;

public class BlockDryGrass extends BlockGenericSnowable implements IRockVariant, IFillable, IFallable {

    private final RockVariant variant;

    public BlockDryGrass(RockVariant variant) {
        super(Properties.of(Material.DIRT).strength(3.0F, 0.6F).sound(SoundType.GRASS).randomTicks());
        this.variant = variant;
    }

    private static boolean canSustainGrass(BlockGetter level, int x, int y, int z) {
        BlockState stateUp = level.getBlockState_(x, y + 1, z);
        //TODO proper snow
        if (stateUp.getBlock() == Blocks.SNOW && stateUp.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        return !stateUp.isFaceSturdy_(level, x, y + 1, z, Direction.DOWN);
    }

    private static boolean canSustainGrassWater(BlockGetter level, int x, int y, int z) {
        return canSustainGrass(level, x, y, z) && !level.getFluidState_(x, y + 1, z).is(FluidTags.WATER);
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.61f;
    }

    @Override
    public BlockState getStateForPhysicsChange(BlockState state) {
        return this.variant.get(EvolutionBlocks.DIRTS).defaultBlockState();
    }

    @Override
    public void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean isMoving) {
        super.neighborChanged_(state, level, x, y, z, oldBlock, fromX, fromY, fromZ, isMoving);
        if (!level.isClientSide) {
            if (x == fromX && z == fromZ && y + 1 == fromY) {
                if (BlockUtils.hasSolidFace(level, x, y + 1, z, Direction.DOWN)) {
                    level.setBlockAndUpdate_(x, y, z, this.variant.get(EvolutionBlocks.DIRTS).defaultBlockState());
                }
            }
        }
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (random.nextInt(2) == 0) {
            if (!canSustainGrass(level, x, y, z)) {
                level.setBlockAndUpdate_(x, y, z, this.variant.get(EvolutionBlocks.DIRTS).defaultBlockState());
            }
            else {
                for (int i = 0; i < 4; ++i) {
                    int rx = x + random.nextInt(3) - 1;
                    int ry = y + random.nextInt(5) - 3;
                    int rz = z + random.nextInt(3) - 1;
                    Block blockAtPos = level.getBlockState_(rx, ry, rz).getBlock();
                    if (blockAtPos instanceof BlockDirt dirt && canSustainGrassWater(level, rx, ry, rz)) {
                        //TODO proper snow
                        level.setBlockAndUpdate_(rx, ry, rz, dirt.rockVariant()
                                                                 .get(EvolutionBlocks.DRY_GRASSES)
                                                                 .defaultBlockState()
                                                                 .setValue(SNOWY, level.getBlockState_(rx, ry + 1, rz).getBlock() == Blocks.SNOW));
                    }
                }
            }
        }
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }
}
