package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TippedArrowItem.class)
public abstract class MixinTippedArrowItem extends ArrowItem {

    public MixinTippedArrowItem(Properties properties) {
        super(properties);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (this.allowdedIn(creativeModeTab)) {
            for (long it = Registry.POTION.beginIteration(); Registry.POTION.hasNextIteration(it); it = Registry.POTION.nextEntry(it)) {
                Potion potion = (Potion) Registry.POTION.getIteration(it);
                if (!potion.getEffects().isEmpty()) {
                    //noinspection ObjectAllocationInLoop
                    nonNullList.add(PotionUtils.setPotion(new ItemStack(this), potion));
                }
            }
        }
    }
}
