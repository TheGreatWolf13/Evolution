package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class ChunkMap_TrackedEntityMixin {

    @Shadow
    @Final
    public Entity entity;
    @Shadow
    @Final
    ServerEntity serverEntity;
    @Shadow(aliases = "this$0")
    ChunkMap this$0;
    @Shadow
    @Final
    private Set<ServerPlayerConnection> seenBy;

    @Shadow
    protected abstract int getEffectiveRange();

    /**
     * @author TheGreatWolf
     * @reason Handle situations where the camera is in a different entity than the player itself.
     */
    @Overwrite
    public void updatePlayer(ServerPlayer player) {
        if (player != this.entity) {
            Entity camera = player.getCamera();
            if (this.entity == camera) {
                if (this.seenBy.add(player.connection)) {
                    this.serverEntity.addPairing(player);
                }
                return;
            }
            boolean broadcastToPlayer = this.entity.broadcastToPlayer(player);
            if (!broadcastToPlayer) {
                if (this.seenBy.remove(player.connection)) {
                    this.serverEntity.removePairing(player);
                }
                return;
            }
            Vec3 playerPos = player.position();
            Vec3 sentPos = this.serverEntity.sentPos();
            double dx = playerPos.x - sentPos.x;
            double dz = playerPos.z - sentPos.z;
            double viewRange = Math.min(this.getEffectiveRange(), (this.this$0.viewDistance - 1) * 16);
            double distSqr = dx * dx + dz * dz;
            double viewRangeSqr = viewRange * viewRange;
            boolean shouldSee = distSqr <= viewRangeSqr;
            if (shouldSee) {
                if (this.seenBy.add(player.connection)) {
                    this.serverEntity.addPairing(player);
                }
                return;
            }
            if (camera != player) {
                playerPos = camera.position();
                dx = playerPos.x - sentPos.x;
                dz = playerPos.z - sentPos.z;
                distSqr = dx * dx + dz * dz;
                shouldSee = distSqr <= viewRangeSqr;
                if (shouldSee) {
                    if (this.seenBy.add(player.connection)) {
                        this.serverEntity.addPairing(player);
                    }
                    return;
                }
                if (this.seenBy.remove(player.connection)) {
                    this.serverEntity.removePairing(player);
                }
                return;
            }
            if (this.seenBy.remove(player.connection)) {
                this.serverEntity.removePairing(player);
            }
        }
    }

    /**
     * Uses less memory, and will cache the returned iterator.
     */
    @Redirect(method = "<init>", require = 0, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newIdentityHashSet()" +
                                                                                  "Ljava/util/Set;", remap = false))
    private Set<ServerPlayerConnection> useFasterCollection() {
        return new ReferenceOpenHashSet<>();
    }
}
