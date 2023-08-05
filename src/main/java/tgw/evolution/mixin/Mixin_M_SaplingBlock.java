package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(SaplingBlock.class)
public abstract class Mixin_M_SaplingBlock extends BushBlock implements BonemealableBlock {

    @Shadow @Final protected static VoxelShape SHAPE;

    public Mixin_M_SaplingBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public abstract void advanceTree(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Random random);

    @Overwrite
    @DeleteMethod
    @Override
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
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getMaxLocalRawBrightness_(x, y + 1, z) >= 9 && random.nextInt(7) == 0) {
            this.advanceTree(level, new BlockPos(x, y, z), state, random);
        }
    }
}
