package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(RecordItem.class)
public abstract class Mixin_M_RecordItem extends Item {

    public Mixin_M_RecordItem(Properties properties) {
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
        if (state.is(Blocks.JUKEBOX) && !state.getValue(JukeboxBlock.HAS_RECORD)) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide) {
                ((JukeboxBlock) Blocks.JUKEBOX).setRecord(level, new BlockPos(x, y, z), state, stack);
                level.levelEvent_(null, LevelEvent.SOUND_PLAY_RECORDING, x, y, z, Item.getId(this));
                stack.shrink(1);
                player.awardStat(Stats.PLAY_RECORD);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
