package tgw.evolution.patches;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.maps.O2ZMap;

public interface PatchPlayerMap {

    default @UnmodifiableView O2ZMap<ServerPlayer> getPlayerMap() {
        throw new AbstractMethodError();
    }
}
