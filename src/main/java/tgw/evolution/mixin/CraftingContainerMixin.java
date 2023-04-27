package tgw.evolution.mixin;

import net.minecraft.world.inventory.CraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ICraftingContainerPatch;
import tgw.evolution.util.collection.I2IMap;
import tgw.evolution.util.collection.I2IOpenHashMap;

@Mixin(CraftingContainer.class)
public abstract class CraftingContainerMixin implements ICraftingContainerPatch {

    private final I2IMap amountToRemove = new I2IOpenHashMap();

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
