package tgw.evolution.mixin;

import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

@Mixin(Creeper.class)
public abstract class CreeperMixin implements ILivingEntityPatch<Creeper> {

    @Nullable
    @Override
    public HitboxEntity<Creeper> getHitboxes() {
        return LegacyEntityHitboxes.CREEPER.get((Creeper) (Object) this);
    }
}
