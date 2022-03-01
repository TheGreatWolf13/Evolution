package tgw.evolution.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IAbstractContainerMenuPatch;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingContainer> {

    public InventoryMenuMixin(MenuType<?> p_40115_, int p_40116_) {
        super(p_40115_, p_40116_);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(Inventory inv, boolean equipmentslot, Player player, CallbackInfo ci) {
        ((IAbstractContainerMenuPatch) this).setPlayer(player);
    }
}
