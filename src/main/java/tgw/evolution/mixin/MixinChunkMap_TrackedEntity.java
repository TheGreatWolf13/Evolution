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
import tgw.evolution.util.physics.EarthHelper;

import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ChunkMap.TrackedEntity.class)
public abstract class MixinChunkMap_TrackedEntity {

    @Shadow @Final public Entity entity;
    @Shadow(aliases = "this$0") @Final ChunkMap field_18245;
    @Shadow @Final private Set<ServerPlayerConnection> seenBy;
    @Shadow @Final public ServerEntity serverEntity;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void broadcast(Packet<?> packet) {
        RSet<ServerPlayerConnection> seenBy = (RSet<ServerPlayerConnection>) this.seenBy;
        for (long it = seenBy.beginIteration(); seenBy.hasNextIteration(it); it = seenBy.nextEntry(it)) {
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
        for (long it = seenBy.beginIteration(); seenBy.hasNextIteration(it); it = seenBy.nextEntry(it)) {
            this.serverEntity.removePairing(seenBy.getIteration(it).getPlayer());
        }
    }

    @Shadow
    protected abstract int getEffectiveRange();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newIdentityHashSet()Ljava/util/Set;", remap = false))
    private Set<ServerPlayerConnection> onInit() {
        return new RHashSet<>();
    }

    /**
     * @author TheGreatWolf
     * @reason Handle situations where the camera is in a different entity than the player itself.
     */
    @Overwrite
    public void updatePlayer(ServerPlayer player) {
        if (player == this.entity) {
            return;
        }
        Entity camera = player.getCamera();
        if (this.entity == camera) {
            if (this.seenBy.add(player.connection)) {
                this.serverEntity.addPairing(player);
            }
            return;
        }
        if (!this.entity.broadcastToPlayer(player)) {
            if (this.seenBy.remove(player.connection)) {
                this.serverEntity.removePairing(player);
            }
            return;
        }
        Vec3 playerPos = player.position();
        Vec3 sentPos = this.serverEntity.sentPos();
        double dx = EarthHelper.absDeltaBlockCoordinate(playerPos.x, sentPos.x);
        double dz = EarthHelper.absDeltaBlockCoordinate(playerPos.z, sentPos.z);
        double viewRange = Math.min(this.getEffectiveRange(), (this.field_18245.viewDistance - 1) * 16);
        double viewRangeSqr = viewRange * viewRange;
        if (dx * dx + dz * dz <= viewRangeSqr) {
            if (this.seenBy.add(player.connection)) {
                this.serverEntity.addPairing(player);
            }
            return;
        }
        if (camera != player) {
            playerPos = camera.position();
            dx = EarthHelper.absDeltaBlockCoordinate(playerPos.x, sentPos.x);
            dz = EarthHelper.absDeltaBlockCoordinate(playerPos.z, sentPos.z);
            if (dx * dx + dz * dz <= viewRangeSqr) {
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
