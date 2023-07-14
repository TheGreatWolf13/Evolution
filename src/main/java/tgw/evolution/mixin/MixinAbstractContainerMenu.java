package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.patches.PatchAbstractContainerMenu;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu implements PatchAbstractContainerMenu {

    @Shadow private ItemStack carried;
    @Unique private Player player;

    /**
     * @author TheGreatWolf
     * @reason Trigger advancements for carried stack
     */
    @Overwrite
    public void setCarried(ItemStack stack) {
        this.carried = stack;
        if (this.player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.INVENTORY_CHANGED.trigger(serverPlayer, this.player.getInventory(), stack);
        }
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }
}
