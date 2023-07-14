package tgw.evolution.mixin;

import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class MixinServerLevel_EntityCallbacks {

    @Shadow(aliases = "this$0") @Final ServerLevel field_26936;

    /**
     * @author TheGreatWolf
     * @reason Call onRemovedFromWorld on entities.
     */
    @Overwrite
    public void onTrackingEnd(Entity entity) {
        this.field_26936.getChunkSource().removeEntity(entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            this.field_26936.players().remove(serverPlayer);
            this.field_26936.updateSleepingPlayerList();
        }
        if (entity instanceof Mob mob) {
            if (this.field_26936.isUpdatingNavigations) {
                Util.logAndPauseIfInIde("onTrackingStart called during navigation iteration",
                                        new IllegalStateException("onTrackingStart called during navigation iteration"));
            }
            this.field_26936.navigatingMobs.remove(mob);
        }
        if (entity instanceof EnderDragon enderDragon) {
            EnderDragonPart[] subEntities = enderDragon.getSubEntities();
            for (EnderDragonPart enderDragonPart : subEntities) {
                this.field_26936.dragonParts.remove(enderDragonPart.getId());
            }
        }
        GameEventListenerRegistrar gameEventListenerRegistrar = entity.getGameEventListenerRegistrar();
        if (gameEventListenerRegistrar != null) {
            gameEventListenerRegistrar.onListenerRemoved(entity.level);
        }
        entity.onRemovedFromWorld();
    }
}
