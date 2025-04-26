package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PotionItem.class)
public abstract class MixinPotionItem extends Item {

    public MixinPotionItem(Properties properties) {
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
                if (potion != Potions.EMPTY) {
                    //noinspection ObjectAllocationInLoop
                    nonNullList.add(PotionUtils.setPotion(new ItemStack(this), potion));
                }
            }
        }
    }
}
