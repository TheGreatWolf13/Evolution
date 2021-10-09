package tgw.evolution.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemIngot extends ItemEv implements IItemTemperature {

    public ItemIngot(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flags) {
        if (stack.hasTag()) {
            tooltip.add(new StringTextComponent("Temperature: " + this.getTemperature(stack) + "K"));
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        double currentTemperature = this.getTemperature(stack);
        if (player.isCrouching()) {
            currentTemperature -= 10;
        }
        else {
            currentTemperature += 10;
        }
        this.setTemperature(stack, currentTemperature);
        return ActionResult.success(stack);
    }
}
