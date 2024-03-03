package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(WritableBookItem.class)
public abstract class Mixin_M_WritableBookItem extends Item {

    public Mixin_M_WritableBookItem(Properties properties) {
        super(properties);
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
        if (state.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(player, level, new BlockPos(x, y, z), state, player.getItemInHand(hand)) ?
                   InteractionResult.sidedSuccess(level.isClientSide) :
                   InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
}
