package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.player.Player;
import tgw.evolution.util.hitbox.hitboxes.HitboxHolder;
import tgw.evolution.util.hitbox.hitboxes.HitboxPlayer;

public final class EvolutionEntityHitboxes {

    public static final HitboxHolder<Player> PLAYER_ALEX = new HitboxHolder<>(() -> new HitboxPlayer(true));
    public static final HitboxHolder<Player> PLAYER_STEVE = new HitboxHolder(() -> new HitboxPlayer(false));

    private EvolutionEntityHitboxes() {
    }
}
