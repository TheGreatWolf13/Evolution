package tgw.evolution.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.entities.IEntityPatch;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxCreeper;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin implements IEntityPatch, INeckPosition {

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
    public Vector3d getNeckPoint() {
        return HitboxCreeper.NECK_STANDING;
    }

    @Override
    public boolean hasHitboxes() {
        return true;
    }
}
