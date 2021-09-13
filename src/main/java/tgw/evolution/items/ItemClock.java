package tgw.evolution.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import tgw.evolution.util.FullDate;

public class ItemClock extends ItemEv {

    public ItemClock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            FullDate date = new FullDate(world.getDayTime());
            player.displayClientMessage(date.getDisplayName(), true);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
