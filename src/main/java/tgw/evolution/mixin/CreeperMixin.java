package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxCreeper;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

@Mixin(Creeper.class)
public abstract class CreeperMixin implements IEntityPatch, INeckPosition {

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
        return EvolutionEntityHitboxes.CREEPER;
    }

    @Override
    public Vec3 getNeckPoint() {
        return HitboxCreeper.NECK_STANDING;
    }

    @Override
    public boolean hasHitboxes() {
        return true;
    }
}
