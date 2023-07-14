package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.inventory.StackedContentsEv;
import tgw.evolution.patches.PatchCraftingContainer;
import tgw.evolution.util.collection.lists.BiIIArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.OList;

@Mixin(ShapelessRecipe.class)
public abstract class MixinShapelessRecipe implements CraftingRecipe {

    @Shadow @Final NonNullList<Ingredient> ingredients;
    //TODO
//    @Shadow @Final private boolean isSimple;

    /**
     * @author TheGreatWolf
     * @reason Use {@link StackedContentsEv}
     */
    @Override
    @Overwrite
    public boolean matches(CraftingContainer container, Level level) {
        PatchCraftingContainer patch = (PatchCraftingContainer) container;
        patch.reset();
        StackedContentsEv helper = new StackedContentsEv();
        OList<ItemStack> inputs = null;
        IList indices = null;
//        if (!this.isSimple) {
//            inputs = new OArrayList<>();
//            indices = new IArrayList();
//        }
        int i = 0;
        for (int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack stack = container.getItem(j);
            if (!stack.isEmpty()) {
                ++i;
//                if (this.isSimple) {
                helper.accountStack(stack, 1);
//                }
//                else {
//                    assert inputs != null;
//                    inputs.add(stack);
//                    indices.add(j);
//                }
            }
        }
        if (i != this.ingredients.size()) {
            return false;
        }
        //TODO
//        if (this.isSimple) {
        return helper.canCraft(this, (BiIIArrayList) null);
//        }
//        assert inputs != null;
//        int[] matches = RecipeMatcher.findMatches(inputs, this.ingredients);
//        if (matches == null) {
//            return false;
//        }
//        for (int num = 0; num < i; num++) {
//            patch.put(indices.getInt(num), this.ingredients.get(matches[num]).getItems()[0].getCount());
//        }
//        return true;
    }
}
