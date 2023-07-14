package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.PatchCraftingContainer;
import tgw.evolution.util.collection.maps.I2IHashMap;
import tgw.evolution.util.collection.maps.I2IMap;

@Mixin(ResultSlot.class)
public abstract class MixinResultSlot extends Slot {

    @Unique private final I2IMap removeCounter = new I2IHashMap();
    @Shadow @Final private CraftingContainer craftSlots;
    @Shadow @Final private Player player;

    public MixinResultSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    /**
     * @author TheGreatWolf
     * @reason Make count work
     */
    @Overwrite
    @Override
    public void onTake(Player player, ItemStack stack) {
        this.checkTakeAchievements(stack);
        NonNullList<ItemStack> remainingItems = player.level.getRecipeManager()
                                                            .getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, player.level);
        ((PatchCraftingContainer) this.craftSlots).getRemoveCounter(this.removeCounter);
        for (int i = 0; i < remainingItems.size(); ++i) {
            ItemStack gridStack = this.craftSlots.getItem(i);
            ItemStack remainingStack = remainingItems.get(i);
            if (!gridStack.isEmpty()) {
                this.craftSlots.removeItem(i, this.removeCounter.getOrDefault(i, 1));
                gridStack = this.craftSlots.getItem(i);
            }
            if (!remainingStack.isEmpty()) {
                if (gridStack.isEmpty()) {
                    this.craftSlots.setItem(i, remainingStack);
                }
                else if (ItemStack.isSame(gridStack, remainingStack) && ItemStack.tagMatches(gridStack, remainingStack)) {
                    remainingStack.grow(gridStack.getCount());
                    this.craftSlots.setItem(i, remainingStack);
                }
                else if (!this.player.getInventory().add(remainingStack)) {
                    this.player.drop(remainingStack, false);
                }
            }
        }
    }
}
