package tgw.evolution.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.init.EvolutionAttributes;

@Mixin(Attributes.class)
public abstract class MixinAttributes {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        //This simply forces our class to load, which forces our stats to register early (when the registry is not frozen),
        //since they're static and are initialized with the class.
        EvolutionAttributes.register();
    }
}
