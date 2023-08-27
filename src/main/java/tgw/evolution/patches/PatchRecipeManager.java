package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PatchRecipeManager {

    default @Nullable Recipe<?> byKey_(ResourceLocation key) {
        throw new AbstractMethodError();
    }

    default void replaceRecipes(List<Recipe<?>> list) {
        throw new AbstractMethodError();
    }
}
