package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.player.Player;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.hitbox.hitboxes.HitboxPlayer;

public final class EvolutionEntityHitboxes {

    public static final HitboxEntity<Player> PLAYER_ALEX = new HitboxPlayer(true);
    public static final HitboxEntity<Player> PLAYER_STEVE = new HitboxPlayer(false);

    private EvolutionEntityHitboxes() {
    }
}
