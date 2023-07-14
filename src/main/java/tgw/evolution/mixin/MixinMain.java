package tgw.evolution.mixin;

import net.minecraft.server.Main;
import net.minecraft.world.level.DataPackConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.resources.ModResourcePackUtil;

@Mixin(Main.class)
public abstract class MixinMain {

    @Redirect(method = "method_40372", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/DataPackConfig;" +
                                                                          "DEFAULT:Lnet/minecraft/world/level/DataPackConfig;"))
    private static DataPackConfig replaceDefaultDataPackSettings() {
        return ModResourcePackUtil.createDefaultDataPackSettings();
    }
}
