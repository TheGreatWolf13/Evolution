package tgw.evolution.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchRecipeManager;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.*;
import java.util.stream.Stream;

@Mixin(RecipeManager.class)
public abstract class Mixin_CF_RecipeManager extends SimpleJsonResourceReloadListener implements PatchRecipeManager, IKeyedReloadListener {

    @Unique private static final OList<ResourceLocation> DEPENDENCY = OList.of(ReloadListernerKeys.TAGS);
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static Gson GSON;
    @Shadow @DeleteField private Map<ResourceLocation, Recipe<?>> byName;
    @Unique private O2OMap<ResourceLocation, Recipe<?>> byName_;
    @Shadow private boolean hasErrors;
    @Shadow @DeleteField private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;
    @Unique private O2OMap<RecipeType<?>, O2OMap<ResourceLocation, Recipe<?>>> recipes_;

    @ModifyConstructor
    public Mixin_CF_RecipeManager() {
        super(GSON, "recipes");
        this.recipes_ = O2OMap.emptyMap();
        this.byName_ = O2OMap.emptyMap();
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
        O2OMap<RecipeType<?>, O2OMap<ResourceLocation, Recipe<?>>> recipes = new O2OHashMap<>(map.size());
        for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
            O2OMap<ResourceLocation, Recipe<?>> value = map.getIterationValue(it);
            value.trim();
            recipes.put(map.getIterationKey(it), value.view());
        }
        recipes.trim();
        this.recipes_ = recipes.view();
        byName.trim();
        this.byName_ = byName.view();
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
        return this.byName_.get(key);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public <C extends Container, T extends Recipe<C>> List<T> getAllRecipesFor(RecipeType<T> recipeType) {
        O2OMap<ResourceLocation, Recipe<?>> map = this.byType_(recipeType);
        OList<T> list = new OArrayList<>(map.size());
        for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
            list.add((T) map.getIterationValue(it));
        }
        return list;
    }

    @Override
    public OList<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.RECIPES;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> recipeType, C container, Level level) {
        O2OMap<ResourceLocation, Recipe<?>> map = this.byType_(recipeType);
        for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
            Recipe<C> recipe = (Recipe<C>) map.getIterationValue(it);
            Optional<T> result = recipeType.tryMatch(recipe, level, container);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public Stream<ResourceLocation> getRecipeIds() {
        return this.recipes_.values().stream().flatMap(map -> map.keySet().stream());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public Collection<Recipe<?>> getRecipes() {
        OSet<Recipe<?>> set = new OHashSet<>();
        O2OMap<RecipeType<?>, O2OMap<ResourceLocation, Recipe<?>>> recipes = this.recipes_;
        for (long it = recipes.beginIteration(); recipes.hasNextIteration(it); it = recipes.nextEntry(it)) {
            O2OMap<ResourceLocation, Recipe<?>> map = recipes.getIterationValue(it);
            for (long it2 = map.beginIteration(); map.hasNextIteration(it2); it2 = map.nextEntry(it2)) {
                set.add(map.getIterationValue(it2));
            }
        }
        return set;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> recipeType, C container, Level level) {
        O2OMap<ResourceLocation, Recipe<?>> map = this.byType_(recipeType);
        OList<T> list = new OArrayList<>();
        for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
            Recipe<C> recipe = (Recipe<C>) map.getIterationValue(it);
            Optional<T> result = recipeType.tryMatch(recipe, level, container);
            if (result.isPresent()) {
                list.add(result.get());
            }
        }
        list.sort(Comparator.comparing(recipe -> recipe.getResultItem().getDescriptionId()));
        return list;
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
        recipes.trim();
        this.recipes_ = recipes.view();
        byName.trim();
        this.byName_ = byName.view();
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> byType(RecipeType<T> recipeType) {
        throw new AbstractMethodError();
    }

    @Unique
    private <C extends Container, T extends Recipe<C>> O2OMap<ResourceLocation, Recipe<?>> byType_(RecipeType<T> recipeType) {
        return this.recipes_.getOrDefault(recipeType, O2OMap.emptyMap());
    }
}
