package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin extends SimpleJsonResourceReloadListener {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private Map<ResourceLocation, Recipe<?>> byName;
    @Shadow
    @Final
    private ICondition.IContext context;
    @Shadow
    private boolean hasErrors;
    @Shadow
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    public RecipeManagerMixin(Gson p_10768_, String p_10769_) {
        super(p_10768_, p_10769_);
    }

    @Shadow
    public static Recipe<?> fromJson(ResourceLocation pRecipeId,
                                     JsonObject pJson,
                                     ICondition.IContext context) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Disable vanilla recipes, use faster collections
     */
    @Override
    @Overwrite
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.hasErrors = false;
        Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map = new Object2ObjectOpenHashMap<>();
        ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();
            if ("minecraft".equals(location.getNamespace())) {
                continue;
            }
            if (location.getPath().startsWith("_")) {
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            }
            try {
                if (entry.getValue().isJsonObject() &&
                    !CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions", this.context)) {
                    LOGGER.debug("Skipping loading recipe {} as it's conditions were not met", location);
                    continue;
                }
                Recipe<?> recipe = fromJson(location, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), this.context);
                if (recipe == null) {
                    LOGGER.info("Skipping loading recipe {} as it's serializer returned null", location);
                    continue;
                }
                ImmutableMap.Builder<ResourceLocation, Recipe<?>> m = map.get(recipe.getType());
                if (m == null) {
                    m = ImmutableMap.builder();
                    map.put(recipe.getType(), m);
                }
                m.put(location, recipe);
                builder.put(location, recipe);
            }
            catch (IllegalArgumentException | JsonParseException e) {
                LOGGER.error("Parsing error loading recipe {}", location, e);
            }
        }
        //noinspection RedundantOperationOnEmptyContainer
        this.recipes = map.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, m -> m.getValue().build()));
        this.byName = builder.build();
        LOGGER.info("Loaded {} recipes", map.size());
    }
}
