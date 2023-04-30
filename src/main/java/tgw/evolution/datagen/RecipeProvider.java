package tgw.evolution.datagen;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.NBTIngredient;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

public class RecipeProvider implements EvolutionDataProvider<FinishedRecipe> {

    protected final DataGenerator generator;
    private final Collection<Path> existingPaths;

    public RecipeProvider(GenBundle bundle) {
        this.generator = bundle.generator();
        this.existingPaths = bundle.existingPaths();
    }

    protected static InventoryChangeTrigger.TriggerInstance has(ItemLike itemLike) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
    }

    protected static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints count, ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(item).withCount(count).build());
    }

    protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    protected static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                                                          MinMaxBounds.Ints.ANY, predicates);
    }

    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        for (WoodVariant variant : WoodVariant.VALUES) {
            Item log = variant.get(EvolutionItems.LOGS);
            //noinspection ObjectAllocationInLoop
            ShapelessRecipeBuilder.shapeless(variant.get(EvolutionItems.CHOPPING_BLOCKS))
                                  .requires(log)
                                  .unlockedBy("has_log", has(log))
                                  .group("evolution:chopping_blocks")
                                  .save(consumer);
        }
        for (RockVariant variant : RockVariant.VALUES_STONE) {
            Item rock = variant.get(EvolutionItems.ROCKS);
            //noinspection ObjectAllocationInLoop
            ShapedRecipeBuilder.shaped(variant.get(EvolutionItems.COBBLESTONES))
                               .define('#', NBTIngredient.of(new ItemStack(rock, 2)))
                               .pattern("##")
                               .pattern("##")
                               .unlockedBy("has_rock", has(rock))
                               .group("evolution:cobblestones")
                               .save(consumer);
        }
        ShapedRecipeBuilder.shaped(EvolutionItems.CLAY.get())
                           .define('#', NBTIngredient.of(new ItemStack(EvolutionItems.CLAYBALL.get(), 2)))
                           .pattern("##")
                           .pattern("##")
                           .unlockedBy("has_clay", has(EvolutionItems.CLAYBALL.get()))
                           .save(consumer);
    }

    private Path createPath(FinishedRecipe id) {
        return this.generator.getOutputFolder().resolve(this.makePath(id));
    }

    @Override
    public Collection<Path> existingPaths() {
        return this.existingPaths;
    }

    @Override
    public String getName() {
        return "Evolution Recipes";
    }

    @Override
    public String makePath(FinishedRecipe id) {
        return "data/" + id.getId().getNamespace() + "/recipes/" + id.getId().getPath() + ".json";
    }

    @Override
    public void run(HashCache cache) {
        ObjectSet<ResourceLocation> set = new ObjectOpenHashSet<>();
        this.buildCraftingRecipes(r -> {
            if (!set.add(r.getId())) {
                throw new IllegalStateException("Duplicate recipe " + r.getId());
            }
            Path path = this.createPath(r);
            this.save(cache, r.serializeRecipe(), path, r);
            JsonObject advJson = r.serializeAdvancement();
            if (advJson != null) {
                //noinspection ConstantConditions
                Path advPath = this.generator.getOutputFolder()
                                             .resolve("data/" +
                                                      r.getId().getNamespace() +
                                                      "/advancements/" +
                                                      r.getAdvancementId().getPath() +
                                                      ".json");
                //noinspection ConstantConditions
                this.save(cache, advJson, advPath, r,
                          n -> "data/" + n.getId().getNamespace() + "/advancements/" + n.getAdvancementId().getPath() + ".json");
            }
        });
    }

    @Override
    public String type() {
        return "Recipe";
    }
}
