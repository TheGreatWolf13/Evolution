package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(DebugStickItem.class)
public abstract class Mixin_M_DebugStickItem extends Item {

    public Mixin_M_DebugStickItem(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canAttackBlock_(BlockState state, Level level, int x, int y, int z, Player player) {
        if (!level.isClientSide) {
            this.handleInteraction(player, state, level, new BlockPos(x, y, z), false, player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        return false;
    }

    @Shadow
    protected abstract boolean handleInteraction(Player player,
                                                 BlockState blockState,
                                                 LevelAccessor levelAccessor,
                                                 BlockPos blockPos, boolean bl, ItemStack itemStack);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (!this.handleInteraction(player, level.getBlockState_(x, y, z), level, new BlockPos(x, y, z), true, player.getItemInHand(hand))) {
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
