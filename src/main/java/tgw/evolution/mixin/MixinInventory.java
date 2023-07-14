package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Inventory.class)
public abstract class MixinInventory {

    @Shadow @Final private List<NonNullList<ItemStack>> compartments;

    @Overwrite
    public ItemStack getItem(int index) {
        List<NonNullList<ItemStack>> compartments = this.compartments;
        for (int i = 0, len = compartments.size(); i < len; ++i) {
            NonNullList<ItemStack> list = compartments.get(i);
            int size = list.size();
            if (index < size) {
                return list.get(index);
            }
            index -= size;
        }
        return ItemStack.EMPTY;
    }
}
