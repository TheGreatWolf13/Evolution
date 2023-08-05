package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(FlintAndSteelItem.class)
public abstract class Mixin_M_FlintAndSteelItem extends Item {

    public Mixin_M_FlintAndSteelItem(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        if (!CampfireBlock.canLight(state) && !CandleBlock.canLight(state) && !CandleCakeBlock.canLight(state)) {
            Direction direction = hitResult.getDirection();
            BlockPos sidePos = new BlockPos(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
            if (BaseFireBlock.canBePlacedAt(level, sidePos, player.getDirection())) {
                level.playSound(player, sidePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                                level.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState stateAtSide = BaseFireBlock.getState(level, sidePos);
                level.setBlock(sidePos, stateAtSide, 11);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, sidePos);
                ItemStack stack = player.getItemInHand(hand);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, sidePos, stack);
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return InteractionResult.FAIL;
        }
        BlockPos pos = new BlockPos(x, y, z);
        level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                        level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 11);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
        player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
