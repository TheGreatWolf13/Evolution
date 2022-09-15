package tgw.evolution.mixin;

import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.util.collection.R2ROpenHashMap;
import tgw.evolution.util.collection.RArrayList;

import java.util.List;
import java.util.Map;

@Mixin(OverlayRegistry.class)
public abstract class OverlayRegistryMixin {

    @Mutable
    @Shadow
    @Final
    private static Map<IIngameOverlay, OverlayRegistry.OverlayEntry> overlays;

    @Mutable
    @Shadow
    @Final
    private static List<OverlayRegistry.OverlayEntry> overlaysOrdered;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        overlays = new R2ROpenHashMap<>();
        overlaysOrdered = new RArrayList<>();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public static List<OverlayRegistry.OverlayEntry> orderedEntries() {
        return overlaysOrdered;
    }
}
