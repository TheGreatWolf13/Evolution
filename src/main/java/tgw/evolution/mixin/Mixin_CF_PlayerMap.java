package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.UnmodifiableView;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchPlayerMap;
import tgw.evolution.util.collection.maps.O2ZHashMap;
import tgw.evolution.util.collection.maps.O2ZMap;

import java.util.Set;

@Mixin(PlayerMap.class)
public abstract class Mixin_CF_PlayerMap implements PatchPlayerMap {

    @Unique private final O2ZMap<ServerPlayer> players_;
    @Shadow @Final @DeleteField private Object2BooleanMap<ServerPlayer> players;

    @ModifyConstructor
    public Mixin_CF_PlayerMap() {
        this.players_ = new O2ZHashMap<>();
    }

    @Overwrite
    public void addPlayer(long l, ServerPlayer player, boolean ignore) {
        this.players_.put(player, ignore);
    }

    @Override
    public @UnmodifiableView O2ZMap<ServerPlayer> getPlayerMap() {
        return this.players_.view();
    }

    @Overwrite
    public Set<ServerPlayer> getPlayers(long l) {
        return this.players_.keySet();
    }

    @Overwrite
    public void ignorePlayer(ServerPlayer player) {
        this.players_.replace(player, true);
    }

    @Overwrite
    public boolean ignored(ServerPlayer player) {
        return this.players_.getBoolean(player);
    }

    @Overwrite
    public boolean ignoredOrUnknown(ServerPlayer player) {
        return this.players_.getOrDefault(player, true);
    }

    @Overwrite
    public void removePlayer(long l, ServerPlayer player) {
        this.players_.removeBoolean(player);
    }

    @Overwrite
    public void unIgnorePlayer(ServerPlayer player) {
        this.players_.replace(player, false);
    }
}
