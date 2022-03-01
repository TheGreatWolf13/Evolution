package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.hitbox.HitboxZombie;

import javax.annotation.Nullable;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster implements INeckPosition, IEntityPatch {

    public ZombieMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public float getCameraYOffset() {
        return 4 / 16.0f;
    }

    @Override
    public float getCameraZOffset() {
        return 4 / 16.0f;
    }

    @Nullable
    @Override
    public HitboxEntity<? extends Entity> getHitboxes() {
        return EvolutionEntityHitboxes.ZOMBIE;
    }

    @Override
    public Vec3 getNeckPoint() {
        return HitboxZombie.NECK_STANDING;
    }

    @Override
    public boolean hasHitboxes() {
        return true;
    }
}
