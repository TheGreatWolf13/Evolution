package tgw.evolution.mixin;

import com.google.gson.*;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;

@Mixin(ItemPredicate.class)
public abstract class Mixin_M_ItemPredicate {

    @Shadow @Final public static ItemPredicate ANY;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static ItemPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject itemJson = GsonHelper.convertToJsonObject(jsonElement, "item");
            if (itemJson.has("data")) {
                throw new JsonParseException("Disallowed data tag found");
            }
            RSet<Item> items = null;
            JsonArray jsonArray = GsonHelper.getAsJsonArray(itemJson, "items", null);
            if (jsonArray != null) {
                items = new RHashSet<>();
                for (int i = 0, len = jsonArray.size(); i < len; ++i) {
                    ResourceLocation itemId = new ResourceLocation(GsonHelper.convertToString(jsonArray.get(i), "item"));
                    Item item = (Item) Registry.ITEM.getNullable(itemId);
                    if (item == null) {
                        throw new JsonSyntaxException("Unknown item id '" + itemId + "'");
                    }
                    items.add(item);
                }
                items = items.view();
            }
            TagKey<Item> tagKey = null;
            if (itemJson.has("tag")) {
                ResourceLocation tag = new ResourceLocation(GsonHelper.getAsString(itemJson, "tag"));
                tagKey = TagKey.create(Registry.ITEM_REGISTRY, tag);
            }
            Potion potion = null;
            if (itemJson.has("potion")) {
                ResourceLocation potionId = new ResourceLocation(GsonHelper.getAsString(itemJson, "potion"));
                potion = (Potion) Registry.POTION.getNullable(potionId);
                if (potion == null) {
                    throw new JsonSyntaxException("Unknown potion '" + potionId + "'");
                }
            }
            return new ItemPredicate(tagKey,
                                     items,
                                     MinMaxBounds.Ints.fromJson(itemJson.get("count")),
                                     MinMaxBounds.Ints.fromJson(itemJson.get("durability")),
                                     EnchantmentPredicate.fromJsonArray(itemJson.get("enchantments")),
                                     EnchantmentPredicate.fromJsonArray(itemJson.get("stored_enchantments")),
                                     potion,
                                     NbtPredicate.fromJson(itemJson.get("nbt"))
            );
        }
        return ANY;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static JsonSyntaxException method_17872(ResourceLocation par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static JsonSyntaxException method_33267(ResourceLocation par1) {
        throw new AbstractMethodError();
    }
}
