package tgw.evolution.mixin;

import net.minecraft.world.entity.player.Abilities;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.PlayerHelper;

@Mixin(Abilities.class)
public abstract class MixinAbilities {

    @Shadow private float walkingSpeed;

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;walkingSpeed:F", opcode =
            Opcodes.PUTFIELD))
    private void onInit(Abilities instance, float value) {
        this.walkingSpeed = (float) PlayerHelper.WALK_FORCE;
    }
}
