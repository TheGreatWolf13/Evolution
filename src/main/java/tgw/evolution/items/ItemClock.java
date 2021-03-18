package tgw.evolution.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import tgw.evolution.util.FullDate;

public class ItemClock extends ItemEv {

    public ItemClock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            FullDate date = new FullDate(world.getDayTime());
            player.sendStatusMessage(new StringTextComponent(date.getFullString()), true);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
