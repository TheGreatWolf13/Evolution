package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public interface PatchRecipeManager {

    @Nullable Recipe<?> byKey_(ResourceLocation key);
}
