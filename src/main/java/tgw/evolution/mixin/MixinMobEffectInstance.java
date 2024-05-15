package tgw.evolution.mixin;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchMobEffect;
import tgw.evolution.patches.PatchMobEffectInstance;
import tgw.evolution.util.math.MathHelper;

@Mixin(MobEffectInstance.class)
public abstract class MixinMobEffectInstance implements PatchMobEffectInstance {

    @Shadow @Final private static Logger LOGGER;
    @Shadow private boolean ambient;
    @Shadow private int amplifier;
    @Shadow private int duration;
    @Shadow @Final private MobEffect effect;
    @Shadow private @Nullable MobEffectInstance hiddenEffect;
    @Unique private boolean infinite;
    @Unique private boolean shouldRemove;
    @Shadow private boolean showIcon;
    @Shadow private boolean visible;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static @Nullable MobEffectInstance load(CompoundTag compoundTag) {
        String id = compoundTag.getString("Id");
        try {
            MobEffect mobEffect = Registry.MOB_EFFECT.get(new ResourceLocation(id));
            return mobEffect == null ? null : loadSpecifiedEffect(mobEffect, compoundTag);
        }
        catch (ResourceLocationException e) {
            Evolution.warn(" Refusing to load MobEffect. Invalid id: ", e);
            return null;
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static MobEffectInstance loadSpecifiedEffect(MobEffect effect, CompoundTag nbt) {
        int amplifier = Byte.toUnsignedInt(nbt.getByte("Amplifier"));
        int duration = nbt.getInt("Duration");
        byte flags = nbt.getByte("Flags");
        MobEffectInstance hiddenEffects = null;
        if (nbt.contains("HiddenEffect", Tag.TAG_COMPOUND)) {
            hiddenEffects = loadSpecifiedEffect(effect, nbt.getCompound("HiddenEffect"));
        }
        MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier, (flags & 2) != 0, (flags & 4) != 0, (flags & 8) != 0, hiddenEffects);
        instance.setInfinite((flags & 1) != 0);
        return instance;
    }

    @Shadow
    public abstract void applyEffect(LivingEntity pEntity);

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle infinite effects.
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
               this.infinite == other.isInfinite() &&
               this.effect.equals(other.getEffect());
    }

    @Override
    @Unique
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

    @Override
    @Unique
    public @Nullable MobEffectInstance getHiddenEffect() {
        return this.hiddenEffect;
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle infinite effects.
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
    @Unique
    public boolean isInfinite() {
        return this.infinite;
    }

    @Shadow
    public abstract boolean isVisible();

    @Override
    public void markForRemoval() {
        this.shouldRemove = true;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public CompoundTag save(CompoundTag compoundTag) {
        //noinspection ConstantConditions
        compoundTag.putString("Id", Registry.MOB_EFFECT.getKey(this.effect).toString());
        this.writeDetailsTo(compoundTag);
        return compoundTag;
    }

    /**
     * @author TheGreatWolf
     * @reason Handle infinite effects
     */
    @Overwrite
    public void setDetailsFrom(MobEffectInstance other) {
        this.duration = other.getDuration();
        this.amplifier = other.getAmplifier();
        this.ambient = other.isAmbient();
        this.visible = other.isVisible();
        this.showIcon = other.showIcon();
        this.setInfinite(other.isInfinite());
        MobEffectInstance hiddenEffect = other.getHiddenEffect();
        if (hiddenEffect != null) {
            this.hiddenEffect = new MobEffectInstance(hiddenEffect);
        }
    }

    @Override
    @Unique
    public void setHiddenEffect(@Nullable MobEffectInstance hiddenInstance) {
        this.hiddenEffect = hiddenInstance;
    }

    @Override
    @Unique
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
                this.hiddenEffect = this.hiddenEffect.getHiddenEffect();
                this.effect.addAttributeModifiers(entity, entity.getAttributes(), this.amplifier);
                entity.onEffectUpdated((MobEffectInstance) (Object) this, false, null);
            }
        }
        if (this.shouldRemove) {
            return false;
        }
        return this.duration > 0;
    }

    @Override
    @Unique
    public int tickDownDurationPatch() {
        return this.tickDownDuration();
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle infinite effects.
     */
    @Overwrite
    public boolean update(MobEffectInstance other) {
        if (this.effect != other.getEffect()) {
            LOGGER.warn("This method should only be called for matching effects!");
            return false;
        }
        boolean changed = false;
        if (other.getAmplifier() > this.amplifier) {
            if (other.getAbsoluteDuration() < this.getAbsoluteDuration()) {
                MobEffectInstance hidden = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance((MobEffectInstance) (Object) this);
                this.hiddenEffect.setHiddenEffect(hidden);
            }
            this.amplifier = other.getAmplifier();
            this.duration = other.getDuration();
            this.setInfinite(other.isInfinite());
            changed = true;
        }
        else if (other.getAbsoluteDuration() > this.getAbsoluteDuration()) {
            if (other.getAmplifier() == this.amplifier) {
                this.duration = other.getDuration();
                this.setInfinite(other.isInfinite());
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
    @Unique
    public boolean updateWithEntity(MobEffectInstance other, LivingEntity entity) {
        if (this.effect != other.getEffect()) {
            LOGGER.warn("This method should only be called for matching effects!");
            return false;
        }
        boolean changed = false;
        if (other.getAmplifier() > this.amplifier) {
            if (other.getAbsoluteDuration() < this.getAbsoluteDuration()) {
                MobEffectInstance hidden = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance((MobEffectInstance) (Object) this);
                this.hiddenEffect.setHiddenEffect(hidden);
            }
            ((PatchMobEffect) this.effect).update(entity, this.amplifier, other.getAmplifier());
            this.amplifier = other.getAmplifier();
            this.duration = other.getDuration();
            this.setInfinite(other.isInfinite());
            changed = true;
        }
        else if (other.getAbsoluteDuration() > this.getAbsoluteDuration()) {
            if (other.getAmplifier() == this.amplifier) {
                this.duration = other.getDuration();
                this.setInfinite(other.isInfinite());
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

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle infinite effects.
     */
    @Overwrite
    private int tickDownDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.tickDownDurationPatch();
        }
        this.duration--;
        if (this.duration == 0 && this.infinite) {
            this.duration = 20_000_000;
        }
        return this.duration;
    }

    /**
     * @author TheGreatWolf
     * @reason Save infinite effects
     */
    @Overwrite
    private void writeDetailsTo(CompoundTag nbt) {
        nbt.putByte("Amplifier", (byte) this.getAmplifier());
        nbt.putInt("Duration", this.getDuration());
        nbt.putByte("Flags", MathHelper.makeFlags(this.infinite, this.isAmbient(), this.isVisible(), this.showIcon()));
        if (this.hiddenEffect != null) {
            CompoundTag hidden = new CompoundTag();
            this.hiddenEffect.save(hidden);
            nbt.put("HiddenEffect", hidden);
        }
    }
}
