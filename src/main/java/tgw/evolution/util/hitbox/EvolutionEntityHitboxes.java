package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;

public final class EvolutionEntityHitboxes {

    public static final HitboxEntity<Creeper> CREEPER = new HitboxCreeper();
    public static final HitboxEntity<Player> PLAYER_STEVE = new HitboxPlayer(false);
    public static final HitboxEntity<Player> PLAYER_ALEX = new HitboxPlayer(true);
    public static final HitboxEntity<Zombie> ZOMBIE = new HitboxZombie();

    private EvolutionEntityHitboxes() {
    }
}
