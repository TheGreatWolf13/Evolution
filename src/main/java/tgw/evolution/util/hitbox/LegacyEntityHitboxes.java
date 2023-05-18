package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import tgw.evolution.util.hitbox.hitboxes.*;

public final class LegacyEntityHitboxes {

    public static final HitboxHolder<CaveSpider> CAVE_SPIDER = new HitboxHolder<>(LegacyHitboxCaveSpider::new);
    public static final HitboxHolder<Creeper> CREEPER = new HitboxHolder<>(LegacyHitboxCreeper::new);
    public static final HitboxHolder<AbstractSkeleton> SKELETON = new HitboxHolder<>(LegacyHitboxSkeleton::new);
    public static final HitboxHolder<Spider> SPIDER = new HitboxHolder<>(LegacyHitboxSpider::new);
    public static final HitboxHolder<Villager> VILLAGER = new HitboxHolder<>(LegacyHitboxVillager::new);
    public static final HitboxHolder<Zombie> ZOMBIE = new HitboxHolder<>(LegacyHitboxZombie::new);

    private LegacyEntityHitboxes() {
    }
}
