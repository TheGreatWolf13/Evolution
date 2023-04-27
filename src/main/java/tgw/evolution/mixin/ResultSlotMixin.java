package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.ICraftingContainerPatch;
import tgw.evolution.util.collection.I2IMap;
import tgw.evolution.util.collection.I2IOpenHashMap;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin extends Slot {

    private final I2IMap removeCounter = new I2IOpenHashMap();
    @Shadow
    @Final
    private CraftingContainer craftSlots;
    @Shadow
    @Final
    private Player player;

    @Shadow
    private int removeCount;

    public ResultSlotMixin(Container pContainer, int pSlot, int pX, int pY) {
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
        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remainingItems = player.level.getRecipeManager()
                                                            .getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, player.level);
        ForgeHooks.setCraftingPlayer(null);
        ((ICraftingContainerPatch) this.craftSlots).getRemoveCounter(this.removeCounter);
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
