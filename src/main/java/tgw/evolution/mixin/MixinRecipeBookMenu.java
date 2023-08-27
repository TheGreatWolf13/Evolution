package tgw.evolution.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.inventory.RecipeCategory;
import tgw.evolution.patches.PatchRecipeBookMenu;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

@Mixin(RecipeBookMenu.class)
public abstract class MixinRecipeBookMenu<C extends Container> extends AbstractContainerMenu implements PatchRecipeBookMenu {

    @Unique private final OList<RecipeCategory> categories = new OArrayList<>();

    public MixinRecipeBookMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public void addRecipeCategories(OList<RecipeCategory> list) {
        list.add(RecipeCategory.UNKNOWN);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(MenuType menuType, int i, CallbackInfo ci) {
        this.addRecipeCategories(this.categories);
    }

    @Override
    public @UnmodifiableView OList<RecipeCategory> recipeCategories() {
        return this.categories.view();
    }
}
