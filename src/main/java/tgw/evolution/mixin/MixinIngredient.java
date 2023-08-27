package tgw.evolution.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchIngredient;

import java.util.function.Predicate;

@Mixin(Ingredient.class)
public abstract class MixinIngredient implements Predicate<ItemStack>, PatchIngredient {

    @Shadow private ItemStack @Nullable [] itemStacks;

    @Contract("_ -> new")
    @Overwrite
    private static Ingredient.Value valueFromJson(JsonObject json) {
        boolean hasItem = json.has("item");
        boolean hasTag = json.has("tag");
        if (hasItem && hasTag) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        }
        if (hasItem) {
            Item item = ShapedRecipe.itemFromJson(json);
            int count = 1;
            if (json.has("count")) {
                count = json.getAsJsonPrimitive("count").getAsInt();
            }
            return new Ingredient.ItemValue(new ItemStack(item, count));
        }
        if (hasTag) {
            return new Ingredient.TagValue(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(GsonHelper.getAsString(json, "tag"))));
        }
        throw new JsonParseException("An ingredient entry needs either a tag or an item");
    }

    @Shadow
    protected abstract void dissolve();

    @Shadow
    public abstract ItemStack[] getItems();

    @Override
    public boolean isSimple() {
        for (ItemStack item : this.getItems()) {
            if (item.getCount() != 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Overwrite
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        this.dissolve();
        assert this.itemStacks != null;
        if (this.itemStacks.length == 0) {
            return stack.isEmpty();
        }
        Item item = stack.getItem();
        int count = stack.getCount();
        for (ItemStack s : this.itemStacks) {
            assert s != null;
            if (s.is(item)) {
                if (s.getCount() <= count) {
                    return true;
                }
            }
        }
        return false;
    }
}
