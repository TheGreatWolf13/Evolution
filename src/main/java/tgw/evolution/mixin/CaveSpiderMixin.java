package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.Units;

@Mixin(CaveSpider.class)
public abstract class CaveSpiderMixin extends Spider implements ILivingEntityPatch<CaveSpider> {

    public CaveSpiderMixin(EntityType<? extends Spider> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public double getBaseAttackDamage() {
        return 10;
    }

    @Override
    public double getBaseHealth() {
        return 60;
    }

    @Override
    public double getBaseMass() {
        return 12;
    }

    @Override
    public double getBaseWalkForce() {
        return 520 * Units.NEWTON;
    }

    @Override
    public @Nullable HitboxEntity<CaveSpider> getHitboxes() {
        return LegacyEntityHitboxes.CAVE_SPIDER;
    }
}
