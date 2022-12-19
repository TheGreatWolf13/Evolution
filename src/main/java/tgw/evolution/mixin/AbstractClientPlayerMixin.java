package tgw.evolution.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements IEntityPatch {

    @Nullable
    @Override
    public HitboxEntity<? extends Entity> getHitboxes() {
        return "default".equals(this.getModelName()) ? EvolutionEntityHitboxes.PLAYER_STEVE : EvolutionEntityHitboxes.PLAYER_ALEX;
    }

    @Shadow
    public abstract String getModelName();
}
