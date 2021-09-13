package tgw.evolution.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    public ClientWorldMixin(ISpawnWorldInfo worldInfo,
                            RegistryKey<World> dimension,
                            DimensionType dimensionType,
                            Supplier<IProfiler> profiler,
                            boolean isRemote,
                            boolean isDebug,
                            long seed) {
        super(worldInfo, dimension, dimensionType, profiler, isRemote, isDebug, seed);
    }

    @Inject(method = "getStarBrightness", at = @At(value = "HEAD"), cancellable = true)
    private void onGetStarBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (ClientEvents.getInstance().getDimension() != null) {
            cir.setReturnValue(ClientEvents.getInstance().getDimension().getSunBrightness(partialTicks));
        }
    }
}
