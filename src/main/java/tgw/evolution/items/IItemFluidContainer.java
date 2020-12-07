package tgw.evolution.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;

public interface IItemFluidContainer {

    static ItemStack getStack(IItemFluidContainer container, int amount) {
        return container.getStack(amount);
    }

    /**
     * @return The amount of fluid carried by this container, in units of 10 mL.
     */
    int getAmount(ItemStack stack);

    Fluid getFluid();

    default ItemStack getFullStack() {
        return this.getStack(this.getMaxAmount());
    }

    /**
     * @return The maximum amount of fluid that this container can carry.
     */
    int getMaxAmount();

    default int getMissingAmount(ItemStack stack) {
        return this.getMaxAmount() - this.getAmount(stack);
    }

    ItemStack getStack(int amount);

    ItemStack getStackAfterPlacement(PlayerEntity player, ItemStack filledBucket, int amountPlaced);

    default boolean isEmpty(ItemStack stack) {
        return this.getFluid() == Fluids.EMPTY || this.getAmount(stack) == 0;
    }

    default boolean isFull(ItemStack stack) {
        return this.getAmount(stack) == this.getMaxAmount();
    }
}
