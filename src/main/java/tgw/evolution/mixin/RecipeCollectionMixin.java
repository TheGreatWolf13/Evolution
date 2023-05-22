package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.inventory.StackedContentsEv;
import tgw.evolution.util.collection.BiIIArrayList;
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
     * @reason Make use of {@link tgw.evolution.inventory.StackedContentsEv}
     */
    @Overwrite
    public void canCraft(StackedContents helper, int width, int height, RecipeBook book) {
        for (Recipe<?> recipe : this.recipes) {
            boolean flag = recipe.canCraftInDimensions(width, height) && book.contains(recipe);
            if (flag) {
                this.fitsDimensions.add(recipe);
            }
            else {
                this.fitsDimensions.remove(recipe);
            }
            if (flag && (helper instanceof StackedContentsEv s ? s.canCraft(recipe, (BiIIArrayList) null) : helper.canCraft(recipe, null))) {
                this.craftable.add(recipe);
            }
            else {
                this.craftable.remove(recipe);
            }
        }
    }

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

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeCollection;" +
                                                                    "known:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit(RecipeCollection instance, Set<Recipe<?>> value) {
        this.known = new ReferenceOpenHashSet<>();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;"), require = 3)
    private @Nullable HashSet onInit() {
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeCollection;" +
                                                                    "craftable:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit0(RecipeCollection instance, Set<Recipe<?>> value) {
        this.craftable = new ReferenceOpenHashSet<>();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeCollection;" +
                                                                    "fitsDimensions:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit1(RecipeCollection instance, Set<Recipe<?>> value) {
        this.fitsDimensions = new ReferenceOpenHashSet<>();
    }
}
