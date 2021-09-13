package tgw.evolution.mixin;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.entities.IEntityPatch;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin implements IEntityPatch {

    @Nullable
    @Override
    public HitboxEntity<? extends Entity> getHitboxes() {
        return "default".equals(this.getModelName()) ? EvolutionEntityHitboxes.PLAYER_STEVE : EvolutionEntityHitboxes.PLAYER_ALEX;
    }

    @Shadow
    public abstract String getModelName();
}
