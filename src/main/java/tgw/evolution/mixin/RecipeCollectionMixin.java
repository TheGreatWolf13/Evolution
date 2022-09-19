package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.util.collection.RArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(RecipeCollection.class)
public abstract class RecipeCollectionMixin {

    @Mutable
    @Shadow
    @Final
    private Set<Recipe<?>> craftable;

    @Mutable
    @Shadow
    @Final
    private Set<Recipe<?>> fitsDimensions;

    @Mutable
    @Shadow
    @Final
    private Set<Recipe<?>> known;

    @Shadow
    @Final
    private List<Recipe<?>> recipes;

    /**
     * @author TheGreatWolf
     * @reason Use faster list
     */
    @Overwrite
    public List<Recipe<?>> getDisplayRecipes(boolean onlyCraftable) {
        List<Recipe<?>> list = new RArrayList<>();
        for (Recipe<?> recipe : this.recipes) {
            if (this.fitsDimensions.contains(recipe) && this.craftable.contains(recipe) == onlyCraftable) {
                list.add(recipe);
            }
        }
        return list;
    }

    /**
     * @author TheGreatWolf
     * @reason Use faster list
     */
    @Overwrite
    public List<Recipe<?>> getRecipes(boolean onlyCraftable) {
        List<Recipe<?>> list = new RArrayList<>();
        Set<Recipe<?>> set = onlyCraftable ? this.craftable : this.fitsDimensions;
        for (Recipe<?> recipe : this.recipes) {
            if (set.contains(recipe)) {
                list.add(recipe);
            }
        }
        return list;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(List recipes, CallbackInfo ci) {
        this.craftable = new ReferenceOpenHashSet<>();
        this.fitsDimensions = new ReferenceOpenHashSet<>();
        this.known = new ReferenceOpenHashSet<>();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;"), require = 3)
    private @Nullable HashSet proxyInit() {
        return null;
    }
}
