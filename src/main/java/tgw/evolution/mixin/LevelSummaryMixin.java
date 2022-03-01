package tgw.evolution.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.patches.ILevelSummaryPatch;
import tgw.evolution.util.math.Metric;

@Mixin(LevelSummary.class)
public abstract class LevelSummaryMixin implements ILevelSummaryPatch {

    private long size;

    @Inject(method = "createInfo", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCreateInfo(CallbackInfoReturnable<Component> cir,
                              MutableComponent info,
                              MutableComponent versionName,
                              MutableComponent versionTransl) {
        info.append(" (" + Metric.bytes(this.size, 1) + ")");
    }

    @Override
    public void setSizeOnDisk(long size) {
        this.size = size;
    }
}
