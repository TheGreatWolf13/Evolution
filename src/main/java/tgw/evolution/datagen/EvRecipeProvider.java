package tgw.evolution.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.NBTIngredient;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import java.util.function.Consumer;

public class EvRecipeProvider extends RecipeProvider {

    public EvRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        for (WoodVariant variant : WoodVariant.VALUES) {
            Item log = variant.get(EvolutionItems.LOGS);
            //noinspection ObjectAllocationInLoop
            ShapelessRecipeBuilder.shapeless(variant.get(EvolutionItems.CHOPPING_BLOCKS))
                                  .requires(log)
                                  .unlockedBy("has_log", inventoryTrigger(ItemPredicate.Builder.item().of(log).build()))
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
                               .unlockedBy("has_rock", inventoryTrigger(ItemPredicate.Builder.item().of(rock).build()))
                               .group("evolution:cobblestones")
                               .save(consumer);
        }
        ShapedRecipeBuilder.shaped(EvolutionItems.CLAY.get())
                           .define('#', NBTIngredient.of(new ItemStack(EvolutionItems.CLAYBALL.get(), 2)))
                           .pattern("##")
                           .pattern("##")
                           .unlockedBy("has_clay", inventoryTrigger(ItemPredicate.Builder.item().of(EvolutionItems.CLAYBALL.get()).build()))
                           .save(consumer);
    }
}
