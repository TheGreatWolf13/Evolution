package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemIngot extends ItemEv implements IItemTemperature {

    public ItemIngot(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        if (stack.hasTag()) {
            tooltip.add(new TextComponent("Temperature: " + this.getTemperature(stack) + "K"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        double currentTemperature = this.getTemperature(stack);
        if (player.isCrouching()) {
            currentTemperature -= 10;
        }
        else {
            currentTemperature += 10;
        }
        this.setTemperature(stack, currentTemperature);
        return InteractionResultHolder.success(stack);
    }
}
