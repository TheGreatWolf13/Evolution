package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(ComposterBlock.class)
public abstract class Mixin_M_ComposterBlock extends Block implements WorldlyContainerHolder {

    @Shadow @Final public static IntegerProperty LEVEL;
    @Shadow @Final public static Object2FloatMap<ItemLike> COMPOSTABLES;
    @Shadow @Final private static VoxelShape[] SHAPES;
    @Shadow @Final private static VoxelShape OUTER_SHAPE;

    public Mixin_M_ComposterBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    static BlockState addItem(BlockState blockState,
                              LevelAccessor levelAccessor,
                              BlockPos blockPos,
                              ItemStack itemStack) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static BlockState extractProduce(BlockState blockState, Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPES[0];
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getInteractionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return OUTER_SHAPE;
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
        return SHAPES[state.getValue(LEVEL)];
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (state.getValue(LEVEL) == 7) {
            level.scheduleTick(new BlockPos(x, y, z), state.getBlock(), 20);
        }
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
        if (state.getValue(LEVEL) == 7) {
            level.setBlockAndUpdate_(x, y, z, state.cycle(LEVEL));
            level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
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
        int i = state.getValue(LEVEL);
        ItemStack itemStack = player.getItemInHand(hand);
        if (i < 8 && COMPOSTABLES.containsKey(itemStack.getItem())) {
            if (i < 7 && !level.isClientSide) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState newState = addItem(state, level, pos, itemStack);
                level.levelEvent(LevelEvent.COMPOSTER_FILL, pos, state != newState ? 1 : 0);
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (i == 8) {
            extractProduce(state, level, new BlockPos(x, y, z));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
