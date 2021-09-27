package tgw.evolution.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import tgw.evolution.util.EarthHelper;
import tgw.evolution.util.Metric;

public class ItemSextant extends ItemEv {

    public ItemSextant(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            player.displayClientMessage(new StringTextComponent(Metric.LATITUDE_FORMAT.format(EarthHelper.calculateLatitude(player.getZ()))), true);
        }
        return new ActionResult<>(ActionResultType.CONSUME, stack);
    }
}
