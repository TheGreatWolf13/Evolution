package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
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

@Mixin(CropBlock.class)
public abstract class Mixin_M_CropBlock extends BushBlock implements BonemealableBlock {

    @Shadow @Final private static VoxelShape[] SHAPE_BY_AGE;

    public Mixin_M_CropBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static float getGrowthSpeed(Block block, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        long pos = BlockPos.asLong(x, y, z);
        return (level.getRawBrightness_(pos, 0) >= 8 || level.canSeeSky_(pos)) && super.canSurvive_(state, level, x, y, z);
    }

    @Shadow
    protected abstract int getAge(BlockState blockState);

    @Shadow
    public abstract IntegerProperty getAgeProperty();

    @Shadow
    public abstract int getMaxAge();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE_BY_AGE[state.getValue(this.getAgeProperty())];
    }

    @Shadow
    public abstract BlockState getStateForAge(int i);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getRawBrightness_(BlockPos.asLong(x, y, z), 0) >= 9) {
            int i = this.getAge(state);
            if (i < this.getMaxAge()) {
                float f = getGrowthSpeed(this, level, new BlockPos(x, y, z));
                if (random.nextInt((int) (25.0F / f) + 1) == 0) {
                    level.setBlock_(x, y, z, this.getStateForAge(i + 1), BlockFlags.BLOCK_UPDATE);
                }
            }
        }
    }
}
