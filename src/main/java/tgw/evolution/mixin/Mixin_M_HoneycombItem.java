package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Optional;

@Mixin(HoneycombItem.class)
public abstract class Mixin_M_HoneycombItem extends Item {

    public Mixin_M_HoneycombItem(Properties properties) {
        super(properties);
    }

    @Shadow
    public static Optional<BlockState> getWaxed(BlockState blockState) {
        throw new AbstractMethodError();
    }

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
        BlockState state = level.getBlockState_(x, y, z);
        Optional<BlockState> waxed = getWaxed(state);
        if (waxed.isPresent()) {
            ItemStack stack = player.getItemInHand(hand);
            if (player instanceof ServerPlayer p) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger_(p, x, y, z, stack);
            }
            stack.shrink(1);
            level.setBlock(new BlockPos(x, y, z), waxed.get(), 11);
            level.levelEvent_(player, LevelEvent.PARTICLES_AND_SOUND_WAX_ON, x, y, z, 0);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
