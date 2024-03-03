package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(ShulkerBoxBlock.class)
public abstract class Mixin_M_ShulkerBoxBlock extends BaseEntityBlock {

    public Mixin_M_ShulkerBoxBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static boolean canOpen(BlockState blockState,
                                   Level level,
                                   BlockPos blockPos,
                                   ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static ItemStack getColoredItemStack(@Nullable DyeColor dyeColor) {
        throw new AbstractMethodError();
    }

    @Shadow
    public abstract @Nullable DyeColor getColor();

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
        if (level.getBlockEntity_(x, y, z) instanceof ShulkerBoxBlockEntity s) {
            return Shapes.create(s.getBoundingBox(state));
        }
        return Shapes.block();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity_(x, y, z) instanceof ShulkerBoxBlockEntity) {
                level.updateNeighbourForOutputSignal_(x, y, z, state.getBlock());
            }
            super.onRemove_(state, level, x, y, z, newState, isMoving);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
        if (level.getBlockEntity_(x, y, z) instanceof ShulkerBoxBlockEntity tile) {
            if (!level.isClientSide && player.isCreative() && !tile.isEmpty()) {
                ItemStack stack = getColoredItemStack(this.getColor());
                tile.saveToItem(stack);
                if (tile.hasCustomName()) {
                    stack.setHoverName(tile.getCustomName());
                }
                ItemEntity itemEntity = new ItemEntity(level, x + 0.5, y + 0.5, z + 0.5, stack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
            else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy_(level, x, y, z, state, player);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity_(x, y, z) instanceof ShulkerBoxBlockEntity tile) {
            if (canOpen(state, level, new BlockPos(x, y, z), tile)) {
                player.openMenu(tile);
                player.awardStat(Stats.OPEN_SHULKER_BOX);
                PiglinAi.angerNearbyPiglins(player, true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
