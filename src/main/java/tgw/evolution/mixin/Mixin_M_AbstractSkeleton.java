package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

@Mixin(AbstractSkeleton.class)
public abstract class Mixin_M_AbstractSkeleton extends Monster {

    public Mixin_M_AbstractSkeleton(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public @Nullable HitboxEntity<AbstractSkeleton> getHitboxes() {
        return LegacyEntityHitboxes.SKELETON.get((AbstractSkeleton) (Object) this);
    }

    @Shadow
    abstract SoundEvent getStepSound();

    @Override
    @Overwrite
    @DeleteMethod
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    public void playStepSound(int x, int y, int z, BlockState state) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
}
