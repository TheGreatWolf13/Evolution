package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.SI;

@Mixin(CaveSpider.class)
public abstract class MixinCaveSpider extends Spider implements PatchLivingEntity {

    public MixinCaveSpider(EntityType<? extends Spider> pEntityType, Level pLevel) {
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
        return 520 * SI.NEWTON;
    }

    @Override
    public @Nullable HitboxEntity<? extends CaveSpider> getHitboxes() {
        return LegacyEntityHitboxes.CAVE_SPIDER.get((CaveSpider) (Object) this);
    }
}
