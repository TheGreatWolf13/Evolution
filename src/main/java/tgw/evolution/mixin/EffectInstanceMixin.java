package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IEffectInstancePatch;
import tgw.evolution.util.NBTTypes;

import javax.annotation.Nullable;

@Mixin(EffectInstance.class)
public abstract class EffectInstanceMixin implements IEffectInstancePatch {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private boolean ambient;
    @Shadow
    private int amplifier;
    @Shadow
    private int duration;
    @Shadow
    @Final
    private Effect effect;
    @Shadow
    @Nullable
    private EffectInstance hiddenEffect;
    private boolean infinite;
    @Shadow
    private boolean showIcon;
    @Shadow
    private boolean splash;
    @Shadow
    private boolean visible;

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle infinite effects.
     */
    @Overwrite
    private static EffectInstance loadSpecifiedEffect(Effect effect, CompoundNBT nbt) {
        int amplifier = nbt.getByte("Amplifier");
        int duration = nbt.getInt("Duration");
        boolean ambient = nbt.getBoolean("Ambient");
        boolean showParticles = true;
        if (nbt.contains("ShowParticles", NBTTypes.BYTE)) {
            showParticles = nbt.getBoolean("ShowParticles");
        }
        boolean showIcon = showParticles;
        if (nbt.contains("ShowIcon", NBTTypes.BYTE)) {
            showIcon = nbt.getBoolean("ShowIcon");
        }
        EffectInstance hiddenEffects = null;
        if (nbt.contains("HiddenEffect", NBTTypes.COMPOUND_NBT)) {
            hiddenEffects = loadSpecifiedEffect(effect, nbt.getCompound("HiddenEffect"));
        }
        EffectInstance instance = new EffectInstance(effect, duration, Math.max(amplifier, 0), ambient, showParticles, showIcon, hiddenEffects);
        boolean infinite = nbt.getBoolean("Infinite");
        if (infinite) {
            ((IEffectInstancePatch) instance).setInfinite(infinite);
        }
        return readCurativeItems(instance, nbt);
    }

    @Nullable
    @Shadow
    private static EffectInstance readCurativeItems(EffectInstance effect, CompoundNBT nbt) {
        return null;
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle infinite effects.
     */
    @Override
    @Overwrite
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EffectInstance)) {
            return false;
        }
        EffectInstance other = (EffectInstance) obj;
        return this.duration == other.getDuration() &&
               this.amplifier == other.getAmplifier() &&
               this.splash == ((IEffectInstancePatch) other).isSpashPatch() &&
               this.ambient == other.isAmbient() &&
               this.infinite == ((IEffectInstancePatch) other).isInfinite() &&
               this.effect.equals(other.getEffect());
    }

    @Override
    public int getAbsoluteDuration() {
        if (this.infinite) {
            return Integer.MAX_VALUE;
        }
        return this.duration;
    }

    @Nullable
    @Override
    public EffectInstance getHiddenEffect() {
        return this.hiddenEffect;
    }

    /**
     * @author MGSchultz
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
        i = 31 * i + (this.splash ? 1 : 0);
        i = 31 * i + (this.infinite ? 1 : 0);
        return 31 * i + (this.ambient ? 1 : 0);
    }

    @Override
    public boolean isInfinite() {
        return this.infinite;
    }

    @Override
    public boolean isSpashPatch() {
        return this.splash;
    }

    @Inject(method = "setDetailsFrom", at = @At(value = "TAIL"))
    private void onSetDetailsFrom(EffectInstance other, CallbackInfo ci) {
        this.setInfinite(((IEffectInstancePatch) other).isInfinite());
    }

    @Inject(method = "writeDetailsTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundNBT;putBoolean(Ljava/lang/String;Z)V",
            ordinal = 0))
    private void onWriteDetailsTo(CompoundNBT nbt, CallbackInfo ci) {
        nbt.putBoolean("Infinite", this.infinite);
    }

    @Override
    public void setHiddenEffect(@Nullable EffectInstance hiddenInstance) {
        this.hiddenEffect = hiddenInstance;
    }

    @Override
    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
        if (infinite) {
            this.duration = 20_000_000;
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle infinite effects.
     */
    @Overwrite
    private int tickDownDuration() {
        if (this.hiddenEffect != null) {
            ((IEffectInstancePatch) this.hiddenEffect).tickDownDurationPatch();
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
     * @author MGSchultz
     * <p>
     * Overwrite to handle infinite effects.
     */
    @Overwrite
    public boolean update(EffectInstance other) {
        if (this.effect != other.getEffect()) {
            LOGGER.warn("This method should only be called for matching effects!");
            return false;
        }
        boolean changed = false;
        if (other.getAmplifier() > this.amplifier) {
            if (((IEffectInstancePatch) other).getAbsoluteDuration() < this.getAbsoluteDuration()) {
                EffectInstance hidden = this.hiddenEffect;
                this.hiddenEffect = new EffectInstance((EffectInstance) (Object) this);
                ((IEffectInstancePatch) this.hiddenEffect).setHiddenEffect(hidden);
            }
            this.amplifier = other.getAmplifier();
            this.duration = other.getDuration();
            this.setInfinite(((IEffectInstancePatch) other).isInfinite());
            changed = true;
        }
        else if (((IEffectInstancePatch) other).getAbsoluteDuration() > this.getAbsoluteDuration()) {
            if (other.getAmplifier() == this.amplifier) {
                this.duration = other.getDuration();
                this.setInfinite(((IEffectInstancePatch) other).isInfinite());
                changed = true;
            }
            else if (this.hiddenEffect == null) {
                this.hiddenEffect = new EffectInstance(other);
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
}
