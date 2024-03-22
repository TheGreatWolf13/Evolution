package tgw.evolution.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Set;

@Mixin(CopyBlockState.Serializer.class)
public abstract class Mixin_M_CopyBlockState_Serializer extends LootItemConditionalFunction.Serializer<CopyBlockState> {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static void method_21902(Set par1, StateDefinition par2, JsonElement par3) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static IllegalArgumentException method_21903(ResourceLocation par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public CopyBlockState deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "block"));
        Block block = (Block) Registry.BLOCK.getNullable(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Can't find block " + resourceLocation);
        }
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
        OSet<Property<?>> set = new OHashSet<>();
        JsonArray jsonArray = GsonHelper.getAsJsonArray(json, "properties", null);
        if (jsonArray != null) {
            for (int i = 0, len = jsonArray.size(); i < len; ++i) {
                set.add(stateDefinition.getProperty(GsonHelper.convertToString(jsonArray.get(i), "property")));
            }
        }
        return new CopyBlockState(conditions, block, set);
    }
}
