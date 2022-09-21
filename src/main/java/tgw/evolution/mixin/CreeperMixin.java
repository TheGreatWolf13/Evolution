package tgw.evolution.mixin;

import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxEntity;

@Mixin(Creeper.class)
public abstract class CreeperMixin implements ILivingEntityPatch<Creeper> {

    @Nullable
    @Override
    public HitboxEntity<Creeper> getHitboxes() {
        return EvolutionEntityHitboxes.CREEPER;
    }
}
