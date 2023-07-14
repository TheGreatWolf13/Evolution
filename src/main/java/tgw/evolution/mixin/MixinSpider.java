package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.SI;

@Mixin(Spider.class)
public abstract class MixinSpider extends Monster implements PatchLivingEntity {
    public MixinSpider(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public double getBaseAttackDamage() {
        return 10;
    }

    @Override
    public double getBaseHealth() {
        return 80;
    }

    @Override
    public double getBaseMass() {
        return 35;
    }

    @Override
    public double getBaseWalkForce() {
        return 1_500 * SI.NEWTON;
    }

    @Override
    public @Nullable HitboxEntity<Spider> getHitboxes() {
        return LegacyEntityHitboxes.SPIDER.get((Spider) (Object) this);
    }
}
