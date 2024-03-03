package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.items.ItemUtils;

@Mixin(SlabBlock.class)
public abstract class Mixin_M_SlabBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static EnumProperty<SlabType> TYPE;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final protected static VoxelShape TOP_AABB;
    @Shadow @Final protected static VoxelShape BOTTOM_AABB;

    public Mixin_M_SlabBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        SlabType slabType = state.getValue(TYPE);
        if (slabType != SlabType.DOUBLE && stack.is(this.asItem())) {
            if (context.replacingClickedOnBlock()) {
                boolean clickedTopPart = context.getHitResult().y() - context.getClickedPos().getY() > 0.5;
                Direction direction = context.getClickedFace();
                if (slabType == SlabType.BOTTOM) {
                    return direction == Direction.UP || clickedTopPart && direction.getAxis().isHorizontal();
                }
                return direction == Direction.DOWN || !clickedTopPart && direction.getAxis().isHorizontal();
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemUtils.RepeatedUse getRepeatedUse() {
        return ItemUtils.RepeatedUse.NOT_ON_FIRST_TICK;
    }

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
        return switch (state.getValue(TYPE)) {
            case DOUBLE -> Shapes.block();
            case TOP -> TOP_AABB;
            case BOTTOM -> BOTTOM_AABB;
        };
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockState state = level.getBlockState_(x, y, z);
        if (state.is(this)) {
            return state.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, false);
        }
        FluidState fluid = level.getFluidState_(x, y, z);
        BlockState stateForPlace = this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
        Direction direction = context.getClickedFace();
        return direction != Direction.DOWN && (direction == Direction.UP || !(context.getHitResult().y() - y > 0.5)) ? stateForPlace : stateForPlace.setValue(TYPE, SlabType.TOP);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state, Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(new BlockPos(x, y, z), Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
