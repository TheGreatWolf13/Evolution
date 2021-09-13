package tgw.evolution.mixin;

import net.minecraft.stats.ServerStatisticsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(ServerStatisticsManager.class)
public abstract class ServerStatisticsManagerMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean constructorProxy(File file) {
        return false;
    }
}
