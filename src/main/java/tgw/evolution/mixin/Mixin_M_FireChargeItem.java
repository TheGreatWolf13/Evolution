package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(FireChargeItem.class)
public abstract class Mixin_M_FireChargeItem extends Item {

    public Mixin_M_FireChargeItem(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void playSound(Level level, BlockPos blockPos);

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
        boolean used = false;
        if (!CampfireBlock.canLight(state) && !CandleBlock.canLight(state) && !CandleCakeBlock.canLight(state)) {
            Direction direction = hitResult.getDirection();
            BlockPos pos = new BlockPos(x, y, z).relative(direction);
            if (BaseFireBlock.canBePlacedAt(level, pos, player.getDirection())) {
                this.playSound(level, pos);
                level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
                used = true;
            }
        }
        else {
            BlockPos pos = new BlockPos(x, y, z);
            this.playSound(level, pos);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
            level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
            used = true;
        }
        if (used) {
            player.getItemInHand(hand).shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }
}
