package tgw.evolution.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tgw.evolution.util.time.FullDate;

public class ItemClock extends ItemEv {

    public ItemClock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            FullDate date = new FullDate(level.getDayTime());
            player.displayClientMessage(date.getDisplayName(), true);
        }
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }
}
