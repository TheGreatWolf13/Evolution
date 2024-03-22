package tgw.evolution.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LootItemBlockStatePropertyCondition.Serializer.class)
public abstract class Mixin_M_LootItemBlockStatePropertyCondition_Serializer implements Serializer<LootItemBlockStatePropertyCondition> {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static IllegalArgumentException method_17937(ResourceLocation par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public LootItemBlockStatePropertyCondition deserialize(JsonObject json, JsonDeserializationContext context) {
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "block"));
        Block block = (Block) Registry.BLOCK.getNullable(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Can't find block " + resourceLocation);
        }
        StatePropertiesPredicate properties = StatePropertiesPredicate.fromJson(json.get("properties"));
        properties.checkState(block.getStateDefinition(), string -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + string);
        });
        return new LootItemBlockStatePropertyCondition(block, properties);
    }
}
