package tgw.evolution.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IAbstractContainerMenuPatch;

@Mixin(AbstractFurnaceMenu.class)
public abstract class AbstractFurnaceMenuMixin extends RecipeBookMenu<Container> {

    public AbstractFurnaceMenuMixin(MenuType<?> p_40115_, int p_40116_) {
        super(p_40115_, p_40116_);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/item/crafting/RecipeType;" +
                     "Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;" +
                     "Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    private void onConstructor(MenuType<?> menuType,
                               RecipeType<? extends AbstractCookingRecipe> recipeType,
                               RecipeBookType recipeBookType,
                               int p_38969_,
                               Inventory inv,
                               Container container,
                               ContainerData containerData,
                               CallbackInfo ci) {
        ((IAbstractContainerMenuPatch) this).setPlayer(inv.player);
    }
}
