package tgw.evolution.mixin;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

    @Redirect(method = "shouldBeVisible", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/DisplayInfo;isHidden()Z"))
    private boolean shouldBeVisibleProxy(DisplayInfo displayInfo) {
        return false;
    }
}
