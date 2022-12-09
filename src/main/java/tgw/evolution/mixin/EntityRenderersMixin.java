package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.client.renderer.entities.RendererPlayer;

import java.util.Map;

@Mixin(EntityRenderers.class)
public abstract class EntityRenderersMixin {

    @Mutable
    @Shadow
    @Final
    private static Map<String, EntityRendererProvider<AbstractClientPlayer>> PLAYER_PROVIDERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        PLAYER_PROVIDERS = ImmutableMap.of("default", c -> new RendererPlayer(c, false), "slim",
                                           c -> new RendererPlayer(c, true));
    }
}
