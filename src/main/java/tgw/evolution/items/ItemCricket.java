package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCricket extends ItemFood {

    public ItemCricket(Item.Properties properties) {
        super(properties, new FoodProperties(69_420));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(EvolutionTexts.TOOLTIP_VERY_EFFICIENT);
    }

    @Override
    public int getTooltipLines() {
        return 1;
    }

    @Override
    protected ItemStack onItemConsume(LivingEntity entity, Level level, ItemStack stack) {
        if (!level.isClientSide) {
            entity.hurt(EvolutionDamage.EFFICIENCY, Float.MAX_VALUE);
        }
        return super.onItemConsume(entity, level, stack);
    }
}
