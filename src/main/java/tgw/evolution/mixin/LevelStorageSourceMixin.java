package tgw.evolution.mixin;

import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.patches.ILevelSummaryPatch;
import tgw.evolution.util.math.MathHelper;

import java.io.File;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceMixin {

    @Inject(method = "getLevelList", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), locals =
            LocalCapture.CAPTURE_FAILHARD)
    private void onGetLevelList(CallbackInfoReturnable<List<LevelSummary>> cir,
                                List<LevelSummary> list,
                                File[] afile,
                                File[] var3,
                                int var4,
                                int var5,
                                File file1,
                                boolean flag,
                                LevelSummary worldsummary) {
        ((ILevelSummaryPatch) worldsummary).setSizeOnDisk(MathHelper.calculateSizeOnDisk(file1.toPath()));
    }
}
