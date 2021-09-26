package tgw.evolution.mixin;

import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.WorldSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.patches.IWorldSummaryPatch;
import tgw.evolution.util.MathHelper;

import java.io.File;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(SaveFormat.class)
public abstract class SaveFormatMixin {

    @Inject(method = "getLevelList", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), locals =
            LocalCapture.CAPTURE_FAILHARD)
    private void onGetLevelList(CallbackInfoReturnable<List<WorldSummary>> cir,
                                List<WorldSummary> list,
                                File[] afile,
                                File[] var3,
                                int var4,
                                int var5,
                                File file1,
                                boolean flag,
                                WorldSummary worldsummary) {
        ((IWorldSummaryPatch) worldsummary).setSizeOnDisk(MathHelper.calculateSizeOnDisk(file1.toPath()));
    }
}
