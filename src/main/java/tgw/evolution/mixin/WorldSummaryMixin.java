package tgw.evolution.mixin;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.storage.WorldSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.patches.IWorldSummaryPatch;
import tgw.evolution.util.Metric;

@Mixin(WorldSummary.class)
public abstract class WorldSummaryMixin implements IWorldSummaryPatch {

    private long size;

    @Inject(method = "createInfo", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCreateInfo(CallbackInfoReturnable<ITextComponent> cir,
                              IFormattableTextComponent info,
                              IFormattableTextComponent versionName,
                              IFormattableTextComponent versionTransl) {
        info.append(" (" + Metric.bytes(this.size, 1) + ")");
    }

    @Override
    public void setSizeOnDisk(long size) {
        this.size = size;
    }
}
