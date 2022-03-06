package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class ChunkMap_TrackedEntityMixin {

    /**
     * Uses less memory, and will cache the returned iterator.
     */
    @Redirect(method = "<init>", require = 0, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newIdentityHashSet()" +
                                                                                  "Ljava/util/Set;", remap = false))
    private Set<ServerPlayerConnection> useFasterCollection() {
        return new ReferenceOpenHashSet<>();
    }
}
