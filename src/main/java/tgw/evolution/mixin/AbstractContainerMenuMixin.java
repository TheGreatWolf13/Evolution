package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IAbstractContainerMenuPatch;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IAbstractContainerMenuPatch {

    private Player player;

    @Inject(method = "setCarried", at = @At("RETURN"))
    private void onSetCarried(ItemStack stack, CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.INVENTORY_CHANGED.trigger(serverPlayer, this.player.getInventory(), stack);
        }
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }
}
