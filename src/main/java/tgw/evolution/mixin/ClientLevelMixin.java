package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
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

    public ClientLevelMixin(WritableLevelData p_46450_,
                            ResourceKey<Level> p_46451_,
                            DimensionType p_46452_,
                            Supplier<ProfilerFiller> p_46453_,
                            boolean p_46454_,
                            boolean p_46455_,
                            long p_46456_) {
        super(p_46450_, p_46451_, p_46452_, p_46453_, p_46454_, p_46455_, p_46456_);
    }

    @Inject(method = "getStarBrightness", at = @At(value = "HEAD"), cancellable = true)
    private void onGetStarBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (ClientEvents.getInstance().getDimension() != null) {
            cir.setReturnValue(ClientEvents.getInstance().getDimension().getSkyBrightness(partialTicks));
        }
    }
}
