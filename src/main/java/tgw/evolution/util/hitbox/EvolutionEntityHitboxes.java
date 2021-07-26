package tgw.evolution.util.hitbox;

import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class EvolutionEntityHitboxes {

    public static final HitboxEntity<CreeperEntity> CREEPER = new HitboxCreeper();
    public static final HitboxEntity<PlayerEntity> PLAYER_STEVE = new HitboxPlayer(false);
    public static final HitboxEntity<PlayerEntity> PLAYER_ALEX = new HitboxPlayer(true);

    private EvolutionEntityHitboxes() {
    }
}
