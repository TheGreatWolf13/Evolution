package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(SignBlock.class)
public abstract class Mixin_M_SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final protected static VoxelShape SHAPE;

    public Mixin_M_SignBlock(Properties properties) {
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
        return SHAPE;
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
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(new BlockPos(x, y, z), Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

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
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        boolean isDye = item instanceof DyeItem;
        boolean isGlowInk = stack.is(Items.GLOW_INK_SAC);
        boolean isInkSac = stack.is(Items.INK_SAC);
        boolean canModify = (isGlowInk || isDye || isInkSac) && player.getAbilities().mayBuild;
        if (level.isClientSide) {
            return canModify ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity_(x, y, z) instanceof SignBlockEntity tile)) {
            return InteractionResult.PASS;
        }
        boolean hasGlowingText = tile.hasGlowingText();
        if (isGlowInk && hasGlowingText || isInkSac && !hasGlowingText) {
            return InteractionResult.PASS;
        }
        if (canModify) {
            boolean modified;
            if (isGlowInk) {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                modified = tile.setHasGlowingText(true);
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, new BlockPos(x, y, z), stack);
            }
            else if (isInkSac) {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                modified = tile.setHasGlowingText(false);
            }
            else {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                modified = tile.setColor(((DyeItem) item).getDyeColor());
            }

            if (modified) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                player.awardStat(Stats.ITEM_USED.get(item));
            }
        }
        return tile.executeClickCommands((ServerPlayer) player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
