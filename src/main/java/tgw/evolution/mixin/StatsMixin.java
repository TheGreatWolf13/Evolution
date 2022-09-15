package tgw.evolution.mixin;

import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.init.EvolutionStats;

@Mixin(Stats.class)
public abstract class StatsMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        //Hack the registry, since Forge doesn't give us any other options...

        //This simply forces our class to load, which forces our stats to register early (when the registry is not frozen),
        //since they're static and are initialized with the class.
        EvolutionStats.DEFAULT.format(1L);
    }
}
