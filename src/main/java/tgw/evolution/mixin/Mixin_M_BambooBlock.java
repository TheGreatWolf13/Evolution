package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(BambooBlock.class)
public abstract class Mixin_M_BambooBlock extends Block implements BonemealableBlock {

    @Shadow @Final public static EnumProperty<BambooLeaves> LEAVES;
    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final public static IntegerProperty STAGE;
    @Shadow @Final protected static VoxelShape COLLISION_SHAPE;
    @Shadow @Final protected static VoxelShape LARGE_SHAPE;
    @Shadow @Final protected static VoxelShape SMALL_SHAPE;

    public Mixin_M_BambooBlock(Properties properties) {
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
        return level.getBlockState_(x, y - 1, z).is(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.moveShapeByOffset(COLLISION_SHAPE, x, z);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public float getDestroyProgress_(BlockState state, Player player, BlockGetter level, int x, int y, int z) {
        return player.getMainHandItem().getItem() instanceof SwordItem ? 1.0F : super.getDestroyProgress_(state, player, level, x, y, z);
    }

    @Shadow
    protected abstract int getHeightBelowUpToMax(BlockGetter blockGetter, BlockPos blockPos);

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.moveShapeByOffset(state.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE, x, z);
    }

    @Shadow
    protected abstract void growBamboo(BlockState blockState, Level level, BlockPos blockPos, Random random, int i);

    @Override
    @Overwrite
    @DeleteMethod
    public boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean isCollisionShapeFullBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        return false;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
        return true;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (state.getValue(STAGE) == 0) {
            if (random.nextInt(3) == 0 && level.isEmptyBlock_(x, y + 1, z) && level.getRawBrightness_(BlockPos.asLong(x, y + 1, z), 0) >= 9) {
                BlockPos pos = new BlockPos(x, y, z);
                int i = this.getHeightBelowUpToMax(level, pos) + 1;
                if (i < 16) {
                    this.growBamboo(state, level, pos, random, i);
                }
            }
        }
    }

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

    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        if (!state.canSurvive_(level, x, y, z)) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        if (from == Direction.UP && fromState.is(Blocks.BAMBOO) && fromState.getValue(AGE) > state.getValue(AGE)) {
            level.setBlock(new BlockPos(x, y, z), state.cycle(AGE), 2);
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
