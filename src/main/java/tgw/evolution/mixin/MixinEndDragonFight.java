package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.DragonRespawnAnimation;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.UUID;

@Mixin(EndDragonFight.class)
public abstract class MixinEndDragonFight {

    @Shadow @Final private ServerBossEvent dragonEvent;
    @Shadow private boolean dragonKilled;
    @Shadow private @Nullable UUID dragonUUID;
    @Shadow @Final private ServerLevel level;
    @Shadow private boolean needsStateScanning;
    @Shadow private @Nullable BlockPos portalLocation;
    @Shadow private @Nullable List<EndCrystal> respawnCrystals;
    @Shadow private @Nullable DragonRespawnAnimation respawnStage;
    @Shadow private int respawnTime;
    @Shadow private int ticksSinceCrystalsScanned;
    @Shadow private int ticksSinceDragonSeen;
    @Shadow private int ticksSinceLastPlayerScan;

    @Shadow
    protected abstract void findOrCreateDragon();

    @Shadow
    protected abstract boolean isArenaLoaded();

    @Shadow
    protected abstract void scanState();

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void tick() {
        this.dragonEvent.setVisible(!this.dragonKilled);
        if (++this.ticksSinceLastPlayerScan >= 20) {
            this.updatePlayers();
            this.ticksSinceLastPlayerScan = 0;
        }
        if (!this.dragonEvent.getPlayers().isEmpty()) {
            this.level.getChunkSource().addRegionTicket_(TicketType.DRAGON, 0, 9, 0);
            boolean arenaLoaded = this.isArenaLoaded();
            if (this.needsStateScanning && arenaLoaded) {
                this.scanState();
                this.needsStateScanning = false;
            }
            if (this.respawnStage != null) {
                if (this.respawnCrystals == null && arenaLoaded) {
                    this.respawnStage = null;
                    this.tryRespawn();
                }
                //noinspection DataFlowIssue
                this.respawnStage.tick(this.level, (EndDragonFight) (Object) this, this.respawnCrystals, this.respawnTime++, this.portalLocation);
            }
            if (!this.dragonKilled) {
                if ((this.dragonUUID == null || ++this.ticksSinceDragonSeen >= 1_200) && arenaLoaded) {
                    this.findOrCreateDragon();
                    this.ticksSinceDragonSeen = 0;
                }
                if (++this.ticksSinceCrystalsScanned >= 100 && arenaLoaded) {
                    this.updateCrystalCount();
                    this.ticksSinceCrystalsScanned = 0;
                }
            }
        }
        else {
            this.level.getChunkSource().removeRegionTicket_(TicketType.DRAGON, 0, 9, 0);
        }
    }

    @Shadow
    public abstract void tryRespawn();

    @Shadow
    protected abstract void updateCrystalCount();

    @Shadow
    protected abstract void updatePlayers();
}
