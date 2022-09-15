package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    public ClientLevelMixin(WritableLevelData pLevelData,
                            ResourceKey<Level> pDimension,
                            Holder<DimensionType> pDimensionTypeRegistration,
                            Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed) {
        super(pLevelData, pDimension, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed);
    }

    @Inject(method = "getStarBrightness", at = @At(value = "HEAD"), cancellable = true)
    private void onGetStarBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (ClientEvents.getInstance().getDimension() != null) {
            cir.setReturnValue(ClientEvents.getInstance().getDimension().getSkyBrightness(partialTicks));
        }
    }
}
