package tgw.evolution.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(ShapedRecipe.class)
public abstract class Mixin_M_ShapedRecipe {

    @Shadow @Final int height;
    @Shadow @Final NonNullList<Ingredient> recipeItems;
    @Shadow @Final int width;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static Item itemFromJson(JsonObject json) {
        String id = GsonHelper.getAsString(json, "item");
        Item item = (Item) Registry.ITEM.getNullable(new ResourceLocation(id));
        if (item == null) {
            throw new JsonSyntaxException("Unknown item '" + id + "'");
        }
        if (item == Items.AIR) {
            throw new JsonSyntaxException("Invalid item: " + id);
        }
        return item;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static JsonSyntaxException method_17878(String par1) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private boolean matches(CraftingContainer container, int width, int height, boolean mirrored) {
        container.reset();
        for (int i = 0; i < container.getWidth(); ++i) {
            for (int j = 0; j < container.getHeight(); ++j) {
                int k = i - width;
                int l = j - height;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
                    if (mirrored) {
                        ingredient = this.recipeItems.get(this.width - k - 1 + l * this.width);
                    }
                    else {
                        ingredient = this.recipeItems.get(k + l * this.width);
                    }
                }
                int index = i + j * container.getWidth();
                if (!ingredient.test(container.getItem(index))) {
                    return false;
                }
                if (!ingredient.isEmpty()) {
                    container.put(index, ingredient.getItems()[0].getCount());
                }
            }
        }
        return true;
    }
}
