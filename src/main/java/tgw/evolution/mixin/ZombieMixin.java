package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxEntity;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster implements IEntityPatch {

    public ZombieMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Nullable
    @Override
    public HitboxEntity<Zombie> getHitboxes() {
        return EvolutionEntityHitboxes.ZOMBIE;
    }
}
