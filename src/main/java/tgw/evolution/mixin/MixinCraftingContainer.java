package tgw.evolution.mixin;

import net.minecraft.world.inventory.CraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.patches.PatchCraftingContainer;
import tgw.evolution.util.collection.maps.I2IHashMap;
import tgw.evolution.util.collection.maps.I2IMap;

@Mixin(CraftingContainer.class)
public abstract class MixinCraftingContainer implements PatchCraftingContainer {

    @Unique private final I2IMap amountToRemove = new I2IHashMap();

    @Override
    public void getRemoveCounter(I2IMap counter) {
        counter.clear();
        counter.putAll(this.amountToRemove);
    }

    @Override
    public void put(int slot, int amount) {
        this.amountToRemove.put(slot, amount);
    }

    @Override
    public void reset() {
        this.amountToRemove.clear();
    }
}
