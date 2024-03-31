package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(TntBlock.class)
public abstract class Mixin_M_TntBlock extends Block {

    @Shadow @Final public static BooleanProperty UNSTABLE;

    public Mixin_M_TntBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static void explode(Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean isMoving) {
        if (level.hasNeighborSignal_(x, y, z)) {
            explode(level, new BlockPos(x, y, z));
            level.removeBlock_(x, y, z, false);
        }
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
        if (!oldState.is(state.getBlock())) {
            if (level.hasNeighborSignal_(x, y, z)) {
                explode(level, new BlockPos(x, y, z));
                level.removeBlock_(x, y, z, false);
            }
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
    public BlockState playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player, Direction face, double hitX, double hitY, double hitZ) {
        if (!level.isClientSide() && !player.isCreative() && state.getValue(UNSTABLE)) {
            explode(level, new BlockPos(x, y, z));
        }
        return super.playerWillDestroy_(level, x, y, z, state, player, face, hitX, hitY, hitZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
            return super.use_(state, level, x, y, z, player, hand, hitResult);
        }
        explode(level, new BlockPos(x, y, z), player);
        level.setBlock_(x, y, z, Blocks.AIR.defaultBlockState(), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD);
        Item item = stack.getItem();
        if (!player.isCreative()) {
            if (stack.is(Items.FLINT_AND_STEEL)) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            else {
                stack.shrink(1);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(item));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
