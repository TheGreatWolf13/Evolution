package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.inventory.StackedContentsEv;
import tgw.evolution.util.RecipeMatcher;
import tgw.evolution.util.collection.lists.*;

@Mixin(ShapelessRecipe.class)
public abstract class MixinShapelessRecipe implements CraftingRecipe {

    @Shadow @Final NonNullList<Ingredient> ingredients;
    @Unique private boolean isSimple;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean matches(CraftingContainer container, Level level) {
        container.reset();
        StackedContentsEv helper = new StackedContentsEv();
        OList<ItemStack> inputs = null;
        IList indices = null;
        if (!this.isSimple) {
            inputs = new OArrayList<>();
            indices = new IArrayList();
        }
        int count = 0;
        for (int i = 0, len = container.getContainerSize(); i < len; ++i) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                ++count;
                if (this.isSimple) {
                    helper.accountStack(stack, 1);
                }
                else {
                    assert inputs != null;
                    inputs.add(stack);
                    indices.add(i);
                }
            }
        }
        if (count != this.ingredients.size()) {
            return false;
        }
        if (this.isSimple) {
            return helper.canCraft(this, (BiIIArrayList) null);
        }
        assert inputs != null;
        int[] matches = RecipeMatcher.findMatches(inputs, this.ingredients);
        if (matches == null) {
            return false;
        }
        for (int num = 0; num < count; num++) {
            container.put(indices.getInt(num), this.ingredients.get(matches[num]).getItems()[0].getCount());
        }
        return true;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients, CallbackInfo ci) {
        boolean simple = true;
        for (int i = 0, len = ingredients.size(); i < len; ++i) {
            if (!ingredients.get(i).isSimple()) {
                simple = false;
                break;
            }
        }
        this.isSimple = simple;
    }
}
