package tgw.evolution.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.tooltip.TooltipManager;

@Mixin(ClientTooltipComponent.class)
public interface MixinClientTooltipComponent {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    static ClientTooltipComponent create(TooltipComponent component) {
        if (component instanceof BundleTooltip bundle) {
            return new ClientBundleTooltip(bundle);
        }
        ClientTooltipComponent result = TooltipManager.getClientTooltipComponent(component);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Unknown TooltipComponent");
    }
}
