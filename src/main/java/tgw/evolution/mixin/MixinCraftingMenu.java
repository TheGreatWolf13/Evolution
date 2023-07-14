package tgw.evolution.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class MixinCraftingMenu extends RecipeBookMenu<CraftingContainer> {

    public MixinCraftingMenu(MenuType<?> p_40115_, int p_40116_) {
        super(p_40115_, p_40116_);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(
            "RETURN"))
    private void onConstructor(int p_39356_, Inventory inv, ContainerLevelAccess p_39358_, CallbackInfo ci) {
        this.setPlayer(inv.player);
    }
}
