package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
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

@Mixin(GrowingPlantBlock.class)
public abstract class Mixin_M_GrowingPlantBlock extends Block {

    @Shadow @Final protected Direction growthDirection;
    @Shadow @Final protected VoxelShape shape;

    public Mixin_M_GrowingPlantBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract boolean canAttachTo(BlockState blockState);

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
        switch (this.growthDirection.getOpposite()) {
            case UP -> ++y;
            case DOWN -> --y;
            case EAST -> ++x;
            case WEST -> --x;
            case NORTH -> --z;
            case SOUTH -> ++z;
        }
        BlockState stateAtSide = level.getBlockState_(x, y, z);
        if (!this.canAttachTo(stateAtSide)) {
            return false;
        }
        return stateAtSide.is(this.getHeadBlock()) ||
               stateAtSide.is(this.getBodyBlock()) ||
               stateAtSide.isFaceSturdy_(level, x, y, z, this.growthDirection);
    }

    @Shadow
    protected abstract Block getBodyBlock();

    @Shadow
    protected abstract GrowingPlantHeadBlock getHeadBlock();

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
        return this.shape;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (!state.canSurvive_(level, x, y, z)) {
            level.destroyBlock_(x, y, z, true);
        }
    }
}
