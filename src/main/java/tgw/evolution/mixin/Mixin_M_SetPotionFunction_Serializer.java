package tgw.evolution.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(SetPotionFunction.Serializer.class)
public abstract class Mixin_M_SetPotionFunction_Serializer extends LootItemConditionalFunction.Serializer<SetPotionFunction> {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static JsonSyntaxException method_38931(String par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public SetPotionFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
        String id = GsonHelper.getAsString(json, "id");
        Potion potion = (Potion) Registry.POTION.getNullable(ResourceLocation.tryParse(id));
        if (potion == null) {
            throw new JsonSyntaxException("Unknown potion '" + id + "'");
        }
        return new SetPotionFunction(conditions, potion);
    }
}
