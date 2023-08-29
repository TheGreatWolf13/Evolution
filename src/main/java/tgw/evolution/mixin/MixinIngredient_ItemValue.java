package tgw.evolution.mixin;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Ingredient.ItemValue.class)
public abstract class MixinIngredient_ItemValue implements Ingredient.Value {

    @Shadow @Final private ItemStack item;

    @Override
    @Overwrite
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("item", Registry.ITEM.getKey(this.item.getItem()).toString());
        int count = this.item.getCount();
        if (count > 1) {
            if (count > this.item.getMaxStackSize()) {
                throw new IllegalArgumentException("Count for item " + this.item + " is greater than its stack size: " + this.item.getMaxStackSize());
            }
            json.addProperty("count", count);
        }
        return json;
    }
}
