package tgw.evolution.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(GsonHelper.class)
public abstract class Mixin_M_GsonHelper {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static Item convertToItem(JsonElement json, String string) {
        if (json.isJsonPrimitive()) {
            String readString = json.getAsString();
            Item item = (Item) Registry.ITEM.getNullable(new ResourceLocation(readString));
            if (item == null) {
                throw new JsonSyntaxException("Expected " + string + " to be an item, was unknown string '" + readString + "'");
            }
            return item;
        }
        throw new JsonSyntaxException("Expected " + string + " to be an item, was " + getType(json));
    }

    @Shadow
    public static String getType(@Nullable JsonElement jsonElement) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static JsonSyntaxException method_17995(String par1, String par2) {
        throw new AbstractMethodError();
    }
}
