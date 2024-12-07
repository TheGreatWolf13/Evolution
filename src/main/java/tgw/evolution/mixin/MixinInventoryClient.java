package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.EvolutionClient;
import tgw.evolution.patches.PatchMinecraft;

@Mixin(Inventory.class)
public abstract class MixinInventoryClient {

    @Shadow public int selected;

    /**
     * @author TheGreatWolf
     * @reason Prevent swapping when paused or special attacking
     */
    @Overwrite
    public void swapPaint(double dir) {
        PatchMinecraft instance = Minecraft.getInstance();
        instance.resetUseHeld();
        if (instance.isMultiplayerPaused() || EvolutionClient.shouldRenderSpecialAttack()) {
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
