package tgw.evolution.mixin;

import net.minecraft.network.protocol.Packet;
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
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;

import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ChunkMap.TrackedEntity.class)
public abstract class MixinChunkMap_TrackedEntity {

    @Shadow @Final public Entity entity;
    @Shadow(aliases = "this$0") @Final ChunkMap field_18245;
    @Shadow @Final ServerEntity serverEntity;
    @Shadow @Final private Set<ServerPlayerConnection> seenBy;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void broadcast(Packet<?> packet) {
        RSet<ServerPlayerConnection> seenBy = (RSet<ServerPlayerConnection>) this.seenBy;
        for (long it = seenBy.beginIteration(); (it & 0xFFFF_FFFFL) != 0; it = seenBy.nextEntry(it)) {
            seenBy.getIteration(it).send(packet);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void broadcastRemoved() {
        RSet<ServerPlayerConnection> seenBy = (RSet<ServerPlayerConnection>) this.seenBy;
        for (long it = seenBy.beginIteration(); (it & 0xFFFF_FFFFL) != 0; it = seenBy.nextEntry(it)) {
            this.serverEntity.removePairing(seenBy.getIteration(it).getPlayer());
        }
    }

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
            double viewRange = Math.min(this.getEffectiveRange(), (this.field_18245.viewDistance - 1) * 16);
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

    @Shadow
    protected abstract int getEffectiveRange();
    
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newIdentityHashSet()Ljava/util/Set;", remap = false))
    private Set<ServerPlayerConnection> onInit() {
        return new RHashSet<>();
    }
}
