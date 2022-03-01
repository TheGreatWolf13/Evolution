package tgw.evolution.items;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tgw.evolution.util.earth.EarthHelper;
import tgw.evolution.util.math.Metric;

public class ItemSextant extends ItemEv {

    public ItemSextant(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(new TextComponent(Metric.LATITUDE_FORMAT.format(EarthHelper.calculateLatitude(player.getZ()))), true);
        }
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }
}
