package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.SI;

@Mixin(Spider.class)
public abstract class Mixin_M_Spider extends Monster {

    public Mixin_M_Spider(EntityType<? extends Monster> pEntityType, Level pLevel) {
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
    public @Nullable HitboxEntity<? extends Spider> getHitboxes() {
        return LegacyEntityHitboxes.SPIDER.get((Spider) (Object) this);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    public void playStepSound(int x, int y, int z, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }
}
