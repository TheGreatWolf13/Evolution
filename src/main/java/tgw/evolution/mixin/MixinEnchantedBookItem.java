package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnchantedBookItem.class)
public abstract class MixinEnchantedBookItem extends Item {

    public MixinEnchantedBookItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("Contract")
    @Contract(value = "_ -> _", pure = true)
    @Shadow
    public static ItemStack createForEnchantment(EnchantmentInstance enchantmentInstance) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (creativeModeTab == CreativeModeTab.TAB_SEARCH) {
            for (long it = Registry.ENCHANTMENT.beginIteration(); Registry.ENCHANTMENT.hasNextIteration(it); it = Registry.ENCHANTMENT.nextEntry(it)) {
                Enchantment enchantment = (Enchantment) Registry.ENCHANTMENT.getIteration(it);
                //noinspection ConstantValue
                if (enchantment.category == null) {
                    continue;
                }
                for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
                    //noinspection ObjectAllocationInLoop
                    nonNullList.add(createForEnchantment(new EnchantmentInstance(enchantment, i)));
                }
            }
        }
        else if (creativeModeTab.getEnchantmentCategories().length != 0) {
            for (long it = Registry.ENCHANTMENT.beginIteration(); Registry.ENCHANTMENT.hasNextIteration(it); it = Registry.ENCHANTMENT.nextEntry(it)) {
                Enchantment enchantment = (Enchantment) Registry.ENCHANTMENT.getIteration(it);
                if (creativeModeTab.hasEnchantmentCategory(enchantment.category)) {
                    //noinspection ObjectAllocationInLoop
                    nonNullList.add(createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel())));
                }
            }
        }
    }
}
