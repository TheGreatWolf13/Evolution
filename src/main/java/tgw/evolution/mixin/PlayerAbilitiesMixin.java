package tgw.evolution.mixin;

import net.minecraft.entity.player.PlayerAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.util.PlayerHelper;

@Mixin(PlayerAbilities.class)
public abstract class PlayerAbilitiesMixin {

    @Shadow
    private float walkingSpeed;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void onConstructor(CallbackInfo ci) {
        this.walkingSpeed = (float) PlayerHelper.WALK_FORCE;
    }
}
