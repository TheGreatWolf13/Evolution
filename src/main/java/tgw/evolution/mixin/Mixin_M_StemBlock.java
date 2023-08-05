package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;

@Mixin(StemBlock.class)
public abstract class Mixin_M_StemBlock extends BushBlock implements BonemealableBlock {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final protected static VoxelShape[] SHAPE_BY_AGE;

    @Shadow @Final private StemGrownBlock fruit;

    public Mixin_M_StemBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getRawBrightness_(BlockPos.asLong(x, y, z), 0) >= 9) {
            float f = CropBlock.getGrowthSpeed(this, level, new BlockPos(x, y, z));
            if (random.nextInt((int) (25.0F / f) + 1) == 0) {
                int i = state.getValue(AGE);
                if (i < 7) {
                    state = state.setValue(AGE, i + 1);
                    level.setBlock_(x, y, z, state, BlockFlags.BLOCK_UPDATE);
                }
                else {
                    Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    int offX = x + dir.getStepX();
                    int offZ = z + dir.getStepZ();
                    if (level.getBlockState_(offX, y, offZ).isAir()) {
                        BlockState stateAtDirBelow = level.getBlockState_(offX, y - 1, offZ);
                        if (stateAtDirBelow.is(Blocks.FARMLAND) || stateAtDirBelow.is(BlockTags.DIRT)) {
                            level.setBlockAndUpdate_(offX, y, offZ, this.fruit.defaultBlockState());
                            level.setBlockAndUpdate_(x, y, z, this.fruit.getAttachedStem()
                                                                        .defaultBlockState()
                                                                        .setValue(HorizontalDirectionalBlock.FACING, dir));
                        }
                    }
                }
            }
        }
    }
}
