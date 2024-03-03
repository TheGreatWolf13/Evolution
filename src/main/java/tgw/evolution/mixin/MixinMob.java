package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Mob.class)
public abstract class MixinMob extends LivingEntity {

    public MixinMob(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean isSunBurnTick() {
        if (this.level.isDay() && !this.level.isClientSide) {
            float brightness = this.getBrightness();
            return brightness > 0.5F &&
                   this.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F &&
                   !(this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow) &&
                   this.level.canSeeSky_(BlockPos.asLong(this.getBlockX(), Mth.floor(this.getEyeY()), this.getBlockZ()));
        }
        return false;
    }
}
