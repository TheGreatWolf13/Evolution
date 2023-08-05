package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
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

@Mixin(MushroomBlock.class)
public abstract class Mixin_M_MushroomBlock extends BushBlock implements BonemealableBlock {

    @Shadow @Final protected static VoxelShape SHAPE;

    public Mixin_M_MushroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        if (stateBelow.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return true;
        }
        return level.getRawBrightness_(BlockPos.asLong(x, y, z), 0) < 13 && this.mayPlaceOn(stateBelow, level, new BlockPos(x, y - 1, z));
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (random.nextInt(25) == 0) {
            int count = 5;
            for (int dz = -4; dz <= 4; ++dz) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dx = -4; dx <= 4; ++dx) {
                        if (level.getBlockState_(x + dx, y + dy, z + dz).is(this)) {
                            --count;
                            if (count <= 0) {
                                return;
                            }
                        }
                    }
                }
            }
            int rx = x + random.nextInt(3) - 1;
            int ry = y + random.nextInt(2) - random.nextInt(2);
            int rz = z + random.nextInt(3) - 1;
            for (int k = 0; k < 4; ++k) {
                if (level.isEmptyBlock_(rx, ry, rz) && state.canSurvive_(level, rx, ry, rz)) {
                    x = rx;
                    y = ry;
                    z = rz;
                }
                rx = x + random.nextInt(3) - 1;
                ry = y + random.nextInt(2) - random.nextInt(2);
                rz = z + random.nextInt(3) - 1;
            }
            if (level.isEmptyBlock_(rx, ry, rz) && state.canSurvive_(level, rx, ry, rz)) {
                level.setBlock_(rx, ry, rz, state, BlockFlags.BLOCK_UPDATE);
            }
        }
    }
}
