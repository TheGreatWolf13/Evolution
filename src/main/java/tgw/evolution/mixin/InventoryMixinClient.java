package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IMinecraftPatch;

@Mixin(Inventory.class)
public abstract class InventoryMixinClient {

    @Shadow
    public int selected;

    /**
     * @author TheGreatWolf
     * @reason Prevent swapping when paused or special attacking
     */
    @Overwrite
    public void swapPaint(double dir) {
        IMinecraftPatch instance = (IMinecraftPatch) Minecraft.getInstance();
        instance.resetUseHeld();
        if (instance.isMultiplayerPaused() || ClientEvents.getInstance().shouldRenderSpecialAttack()) {
            return;
        }
        if (dir > 0) {
            --this.selected;
            if (this.selected < 0) {
                this.selected = 8;
            }
        }
        else {
            ++this.selected;
            if (this.selected >= 9) {
                this.selected = 0;
            }
        }
    }
}
