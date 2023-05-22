package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.renderer.entities.RendererPlayer;

import java.util.Map;

@Mixin(EntityRenderers.class)
public abstract class EntityRenderersMixin {

    @Mutable
    @Shadow
    @Final
    private static Map<String, EntityRendererProvider<AbstractClientPlayer>> PLAYER_PROVIDERS;

    @Redirect(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;" +
                                                                      "PLAYER_PROVIDERS:Ljava/util/Map;", opcode = Opcodes.PUTSTATIC))
    private static void onClinit(Map<String, EntityRendererProvider<AbstractClientPlayer>> value) {
        PLAYER_PROVIDERS = ImmutableMap.of("default", c -> new RendererPlayer(c, false), "slim", c -> new RendererPlayer(c, true));
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;of(Ljava/lang/Object;" +
                                                                       "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)" +
                                                                       "Lcom/google/common/collect/ImmutableMap;"))
    private static @Nullable <K, V>

    ImmutableMap<String, EntityRendererProvider<AbstractClientPlayer>> onClinit(K k1, V v1, K k2, V v2) {
        return null;
    }
}
