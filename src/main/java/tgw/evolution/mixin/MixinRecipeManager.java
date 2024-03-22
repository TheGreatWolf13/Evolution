package tgw.evolution.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchRecipeManager;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager extends SimpleJsonResourceReloadListener implements PatchRecipeManager, IKeyedReloadListener {

    @Unique private static final OList<ResourceLocation> DEPENDENCY = OList.of(ReloadListernerKeys.TAGS);
    @Shadow @Final private static Logger LOGGER;
    @Shadow private Map<ResourceLocation, Recipe<?>> byName;
    @Shadow private boolean hasErrors;
    @Shadow private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    public MixinRecipeManager(Gson p_10768_, String p_10769_) {
        super(p_10768_, p_10769_);
    }

    @Shadow
    public static Recipe<?> fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Disable vanilla recipes, use faster collections
     */
    @Override
    @Overwrite
    public void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.hasErrors = false;
        O2OMap<RecipeType<?>, O2OMap<ResourceLocation, Recipe<?>>> map = new O2OHashMap<>();
        O2OMap<ResourceLocation, Recipe<?>> byName = new O2OHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();
            if ("minecraft".equals(location.getNamespace())) {
                continue;
            }
            if (location.getPath().startsWith("_")) {
                continue;
            }
            try {
                Recipe<?> recipe = fromJson(location, GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                if (recipe == null) {
                    LOGGER.info("Skipping loading recipe {} as it's serializer returned null", location);
                    continue;
                }
                O2OMap<ResourceLocation, Recipe<?>> m = map.get(recipe.getType());
                if (m == null) {
                    m = new O2OHashMap<>();
                    map.put(recipe.getType(), m);
                }
                m.put(location, recipe);
                byName.put(location, recipe);
            }
            catch (IllegalArgumentException | JsonParseException e) {
                LOGGER.error("Parsing error loading recipe {}", location, e);
            }
        }
        O2OMap<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = new O2OHashMap<>(map.size());
        for (O2OMap.Entry<RecipeType<?>, O2OMap<ResourceLocation, Recipe<?>>> e = map.fastEntries(); e != null; e = map.fastEntries()) {
            O2OMap<ResourceLocation, Recipe<?>> value = e.value();
            value.trimCollection();
            recipes.put(e.key(), Object2ObjectMaps.unmodifiable(value));
        }
        recipes.trimCollection();
        this.recipes = Object2ObjectMaps.unmodifiable(recipes);
        byName.trimCollection();
        this.byName = Object2ObjectMaps.unmodifiable(byName);
        LOGGER.info("Loaded {} recipes", map.size());
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-Optional version
     */
    @Overwrite
    public Optional<? extends Recipe<?>> byKey(ResourceLocation key) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(this.byKey_(key));
    }

    @Override
    public @Nullable Recipe<?> byKey_(ResourceLocation key) {
        return this.byName.get(key);
    }

    @Override
    public OList<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.RECIPES;
    }

    @Override
    public void replaceRecipes(List<Recipe<?>> list) {
        this.hasErrors = false;
        O2OMap<RecipeType<?>, O2OMap<ResourceLocation, Recipe<?>>> recipes = new O2OHashMap<>();
        O2OMap<ResourceLocation, Recipe<?>> byName = new O2OHashMap<>();
        for (int i = 0, len = list.size(); i < len; ++i) {
            Recipe<?> recipe = list.get(i);
            O2OMap<ResourceLocation, Recipe<?>> map = recipes.get(recipe.getType());
            if (map == null) {
                map = new O2OHashMap<>();
                recipes.put(recipe.getType(), map);
            }
            ResourceLocation resourceLocation = recipe.getId();
            Recipe<?> oldRecipe = map.put(resourceLocation, recipe);
            byName.put(resourceLocation, recipe);
            //noinspection VariableNotUsedInsideIf
            if (oldRecipe != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + resourceLocation);
            }
        }
        recipes.trimCollection();
        this.recipes = Object2ObjectMaps.unmodifiable(recipes);
        byName.trimCollection();
        this.byName = Object2ObjectMaps.unmodifiable(byName);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    public void replaceRecipes(Iterable<Recipe<?>> iterable) {
        throw new AbstractMethodError();
    }
}
