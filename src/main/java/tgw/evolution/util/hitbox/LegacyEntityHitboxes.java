package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import tgw.evolution.util.hitbox.hitboxes.*;

public final class LegacyEntityHitboxes {

    public static final HitboxEntity<CaveSpider> CAVE_SPIDER = new LegacyHitboxCaveSpider();
    public static final HitboxEntity<Creeper> CREEPER = new LegacyHitboxCreeper();
    public static final HitboxEntity<AbstractSkeleton> SKELETON = new LegacyHitboxSkeleton();
    public static final HitboxEntity<Spider> SPIDER = new LegacyHitboxSpider<>();
    public static final HitboxEntity<Villager> VILLAGER = new LegacyHitboxVillager();
    public static final HitboxEntity<Zombie> ZOMBIE = new LegacyHitboxZombie();

    private LegacyEntityHitboxes() {
    }
}
