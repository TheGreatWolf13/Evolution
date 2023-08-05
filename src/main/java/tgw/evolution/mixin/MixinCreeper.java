package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

@Mixin(Creeper.class)
public abstract class MixinCreeper extends Monster {

    public MixinCreeper(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @Nullable HitboxEntity<Creeper> getHitboxes() {
        return LegacyEntityHitboxes.CREEPER.get((Creeper) (Object) this);
    }
}
