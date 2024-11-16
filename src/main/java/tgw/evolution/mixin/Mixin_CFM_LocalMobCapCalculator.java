package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.L2OLinkedHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.List;
import java.util.Map;

@Mixin(LocalMobCapCalculator.class)
public abstract class Mixin_CFM_LocalMobCapCalculator {

    @Mutable @Shadow @Final @RestoreFinal private ChunkMap chunkMap;
    @DeleteField @Shadow @Final private Map<ServerPlayer, LocalMobCapCalculator.MobCounts> playerMobCounts;
    @Unique private final O2OMap<ServerPlayer, LocalMobCapCalculator.MobCounts> playerMobCounts_;
    @DeleteField @Shadow @Final private Long2ObjectMap<List<ServerPlayer>> playersNearChunk;
    @Unique private final L2OMap<OList<ServerPlayer>> playersNearChunk_;

    @ModifyConstructor
    public Mixin_CFM_LocalMobCapCalculator(ChunkMap chunkMap) {
        this.playerMobCounts_ = new O2OHashMap<>();
        this.playersNearChunk_ = new L2OLinkedHashMap<>();
        this.chunkMap = chunkMap;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void addMob(ChunkPos chunkPos, MobCategory mobCategory) {
        OList<ServerPlayer> playersNear = this.getPlayersNear(chunkPos.x, chunkPos.z);
        for (int i = 0, len = playersNear.size(); i < len; ++i) {
            ServerPlayer player = playersNear.get(i);
            LocalMobCapCalculator.MobCounts mobCounts = this.playerMobCounts_.get(player);
            if (mobCounts == null) {
                mobCounts = new LocalMobCapCalculator.MobCounts();
                this.playerMobCounts_.put(player, mobCounts);
            }
            mobCounts.add(mobCategory);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean canSpawn(MobCategory mobCategory, ChunkPos chunkPos) {
        OList<ServerPlayer> list = this.getPlayersNear(chunkPos.x, chunkPos.z);
        for (int i = 0, len = list.size(); i < len; ++i) {
            ServerPlayer player = list.get(i);
            LocalMobCapCalculator.MobCounts mobCounts = this.playerMobCounts_.get(player);
            if (mobCounts == null || mobCounts.canSpawn(mobCategory)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private List<ServerPlayer> getPlayersNear(ChunkPos chunkPos) {
        throw new AbstractMethodError();
    }

    @Unique
    private OList<ServerPlayer> getPlayersNear(int chunkX, int chunkZ) {
        long pos = ChunkPos.asLong(chunkX, chunkZ);
        OList<ServerPlayer> list = this.playersNearChunk_.get(pos);
        if (list == null) {
            list = this.chunkMap.getPlayersCloseForSpawning(chunkX, chunkZ);
            this.playersNearChunk_.put(pos, list);
        }
        return list;
    }
}
