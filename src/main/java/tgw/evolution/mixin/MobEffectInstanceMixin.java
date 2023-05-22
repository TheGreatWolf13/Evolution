package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMobEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMobEffectInstancePatch;
import tgw.evolution.patches.IMobEffectPatch;

import java.util.ArrayList;
import java.util.List;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements IMobEffectInstancePatch, IForgeMobEffectInstance {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private boolean ambient;
    @Shadow
    private int amplifier;
    @Shadow
    private List<ItemStack> curativeItems;
    @Shadow
    private int duration;
    @Shadow
    @Final
    private MobEffect effect;
    @Shadow
    @Nullable
    private MobEffectInstance hiddenEffect;
    private boolean infinite;
    @Shadow
    private boolean showIcon;
    @Shadow
    private boolean visible;

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle infinite effects.
     */
    @Overwrite
    private static MobEffectInstance loadSpecifiedEffect(MobEffect effect, CompoundTag nbt) {
        int amplifier = Byte.toUnsignedInt(nbt.getByte("Amplifier"));
        int duration = nbt.getInt("Duration");
        boolean ambient = nbt.getBoolean("Ambient");
        boolean showParticles = true;
        if (nbt.contains("ShowParticles", Tag.TAG_BYTE)) {
            showParticles = nbt.getBoolean("ShowParticles");
        }
        boolean showIcon = showParticles;
        if (nbt.contains("ShowIcon", Tag.TAG_BYTE)) {
            showIcon = nbt.getBoolean("ShowIcon");
        }
        MobEffectInstance hiddenEffects = null;
        if (nbt.contains("HiddenEffect", Tag.TAG_COMPOUND)) {
            hiddenEffects = loadSpecifiedEffect(effect, nbt.getCompound("HiddenEffect"));
        }
        MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon, hiddenEffects);
        boolean infinite = nbt.getBoolean("Infinite");
        if (infinite) {
            ((IMobEffectInstancePatch) instance).setInfinite(infinite);
        }
        return readCurativeItems(instance, nbt);
    }

    @Nullable
    @Shadow
    private static MobEffectInstance readCurativeItems(MobEffectInstance effect, CompoundTag nbt) {
        return null;
    }

    @Shadow
    public abstract void applyEffect(LivingEntity pEntity);

    /**
     * @author TheGreatWolf
     * <p>
     * Overwrite to handle infinite effects.
     */
    @Override
    @Overwrite
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MobEffectInstance other)) {
            return false;
        }
        return this.duration == other.getDuration() &&
               this.amplifier == other.getAmplifier() &&
               this.ambient == other.isAmbient() &&
               this.infinite == ((IMobEffectInstancePatch) other).isInfinite() &&
               this.effect.equals(other.getEffect());
    }

    @Override
    public float getAbsoluteDuration() {
        if (this.infinite) {
            return Float.POSITIVE_INFINITY;
        }
        return this.duration;
    }

    @Shadow
    public abstract int getAmplifier();

    @Shadow
    public abstract int getDuration();

    @Nullable
    @Override
    public MobEffectInstance getHiddenEffect() {
        return this.hiddenEffect;
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Overwrite to handle infinite effects.
     */
    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    @Overwrite
    public int hashCode() {
        int i = this.effect.hashCode();
        i = 31 * i + this.duration;
        i = 31 * i + this.amplifier;
        i = 31 * i + (this.infinite ? 1 : 0);
        return 31 * i + (this.ambient ? 1 : 0);
    }

    @Shadow
    public abstract boolean isAmbient();

    @Override
    public boolean isInfinite() {
        return this.infinite;
    }

    @Shadow
    public abstract boolean isVisible();

    @Overwrite
    void setDetailsFrom(MobEffectInstance other) {
        this.duration = other.getDuration();
        this.amplifier = other.getAmplifier();
        this.ambient = other.isAmbient();
        this.visible = other.isVisible();
        this.showIcon = other.showIcon();
        this.curativeItems = other.getCurativeItems() == null ? null : new ArrayList(other.getCurativeItems());
        this.setInfinite(((IMobEffectInstancePatch) other).isInfinite());
        MobEffectInstance hiddenEffect = ((IMobEffectInstancePatch) other).getHiddenEffect();
        if (hiddenEffect != null) {
            this.hiddenEffect = new MobEffectInstance(hiddenEffect);
        }
    }

    @Override
    public void setHiddenEffect(@Nullable MobEffectInstance hiddenInstance) {
        this.hiddenEffect = hiddenInstance;
    }

    @Override
    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
        if (infinite) {
            this.duration = 20_000_000;
        }
    }

    @Shadow
    public abstract boolean showIcon();

    /**
     * @author TheGreatWolf
     * @reason When an effect expires, but has a hidden effect, its attribute modifiers are updated and readded. However, instead of removing the
     * modifiers based on the old instance of the effect (which has a higher amplifier), the original code removes and readds the attributes based
     * on the new instance (which obviously has the same amplifier), resulting in weird behaviours.
     */
    @Overwrite
    public boolean tick(LivingEntity entity, Runnable runnable) {
        if (this.duration > 0) {
            if (this.effect.isDurationEffectTick(this.duration, this.amplifier)) {
                this.applyEffect(entity);
            }
            this.tickDownDuration();
            if (this.duration == 0 && this.hiddenEffect != null) {
                this.effect.removeAttributeModifiers(entity, entity.getAttributes(), this.amplifier);
                this.setDetailsFrom(this.hiddenEffect);
                this.hiddenEffect = ((IMobEffectInstancePatch) this.hiddenEffect).getHiddenEffect();
                this.effect.addAttributeModifiers(entity, entity.getAttributes(), this.amplifier);
                entity.onEffectUpdated((MobEffectInstance) (Object) this, false, null);
            }
        }
        return this.duration > 0;
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle infinite effects.
     */
    @Overwrite
    private int tickDownDuration() {
        if (this.hiddenEffect != null) {
            ((IMobEffectInstancePatch) this.hiddenEffect).tickDownDurationPatch();
        }
        this.duration--;
        if (this.duration == 0 && this.infinite) {
            this.duration = 20_000_000;
        }
        return this.duration;
    }

    @Override
    public int tickDownDurationPatch() {
        return this.tickDownDuration();
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Overwrite to handle infinite effects.
     */
    @Overwrite
    public boolean update(MobEffectInstance other) {
        if (this.effect != other.getEffect()) {
            LOGGER.warn("This method should only be called for matching effects!");
            return false;
        }
        boolean changed = false;
        if (other.getAmplifier() > this.amplifier) {
            if (((IMobEffectInstancePatch) other).getAbsoluteDuration() < this.getAbsoluteDuration()) {
                MobEffectInstance hidden = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance((MobEffectInstance) (Object) this);
                ((IMobEffectInstancePatch) this.hiddenEffect).setHiddenEffect(hidden);
            }
            this.amplifier = other.getAmplifier();
            this.duration = other.getDuration();
            this.setInfinite(((IMobEffectInstancePatch) other).isInfinite());
            changed = true;
        }
        else if (((IMobEffectInstancePatch) other).getAbsoluteDuration() > this.getAbsoluteDuration()) {
            if (other.getAmplifier() == this.amplifier) {
                this.duration = other.getDuration();
                this.setInfinite(((IMobEffectInstancePatch) other).isInfinite());
                changed = true;
            }
            else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffectInstance(other);
            }
            else {
                this.hiddenEffect.update(other);
            }
        }
        if (!other.isAmbient() && this.ambient || changed) {
            this.ambient = other.isAmbient();
            changed = true;
        }
        if (other.isVisible() != this.visible) {
            this.visible = other.isVisible();
            changed = true;
        }
        if (other.showIcon() != this.showIcon) {
            this.showIcon = other.showIcon();
            return true;
        }
        return changed;
    }

    @Override
    public boolean updateWithEntity(MobEffectInstance other, LivingEntity entity) {
        if (this.effect != other.getEffect()) {
            LOGGER.warn("This method should only be called for matching effects!");
            return false;
        }
        boolean changed = false;
        if (other.getAmplifier() > this.amplifier) {
            if (((IMobEffectInstancePatch) other).getAbsoluteDuration() < this.getAbsoluteDuration()) {
                MobEffectInstance hidden = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance((MobEffectInstance) (Object) this);
                ((IMobEffectInstancePatch) this.hiddenEffect).setHiddenEffect(hidden);
            }
            ((IMobEffectPatch) this.effect).update(entity, this.amplifier, other.getAmplifier());
            this.amplifier = other.getAmplifier();
            this.duration = other.getDuration();
            this.setInfinite(((IMobEffectInstancePatch) other).isInfinite());
            changed = true;
        }
        else if (((IMobEffectInstancePatch) other).getAbsoluteDuration() > this.getAbsoluteDuration()) {
            if (other.getAmplifier() == this.amplifier) {
                this.duration = other.getDuration();
                this.setInfinite(((IMobEffectInstancePatch) other).isInfinite());
                changed = true;
            }
            else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffectInstance(other);
            }
            else {
                this.hiddenEffect.update(other);
            }
        }
        if (!other.isAmbient() && this.ambient || changed) {
            this.ambient = other.isAmbient();
            changed = true;
        }
        if (other.isVisible() != this.visible) {
            this.visible = other.isVisible();
            changed = true;
        }
        if (other.showIcon() != this.showIcon) {
            this.showIcon = other.showIcon();
            return true;
        }
        return changed;
    }

    @Overwrite
    private void writeDetailsTo(CompoundTag nbt) {
        nbt.putByte("Amplifier", (byte) this.getAmplifier());
        nbt.putInt("Duration", this.getDuration());
        nbt.putBoolean("Infinite", this.infinite);
        nbt.putBoolean("Ambient", this.isAmbient());
        nbt.putBoolean("ShowParticles", this.isVisible());
        nbt.putBoolean("ShowIcon", this.showIcon());
        if (this.hiddenEffect != null) {
            CompoundTag hidden = new CompoundTag();
            this.hiddenEffect.save(hidden);
            nbt.put("HiddenEffect", hidden);
        }
        this.writeCurativeItems(nbt);
    }
}
