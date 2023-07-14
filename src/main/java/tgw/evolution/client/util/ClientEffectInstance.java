package tgw.evolution.client.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.patches.PatchMobEffectInstance;

public class ClientEffectInstance implements Comparable<ClientEffectInstance> {

    private final MobEffect effect;
    private int amplifier;
    private int duration;
    private @Nullable ClientEffectInstance hiddenInstance;
    private boolean isAmbient;
    private boolean isInfinite;
    private boolean isShowIcon;

    public ClientEffectInstance(MobEffect effect) {
        this.effect = effect;
    }

    public ClientEffectInstance(MobEffect effect, int amplifier, int duration) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public ClientEffectInstance(MobEffect effect, int duration) {
        this.effect = effect;
        this.duration = duration;
    }

    public ClientEffectInstance(MobEffect effect, int amplifier, boolean isInfinite) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.setInfinite(isInfinite);
    }

    public ClientEffectInstance(MobEffectInstance instance) {
        this.effect = instance.getEffect();
        this.amplifier = instance.getAmplifier();
        this.duration = instance.getDuration();
        this.isAmbient = instance.isAmbient();
        this.setInfinite(((PatchMobEffectInstance) instance).isInfinite());
        this.isShowIcon = instance.showIcon();
        this.setHiddenInstance(((PatchMobEffectInstance) instance).getHiddenEffect());
    }

    @Override
    public int compareTo(ClientEffectInstance o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.effect.getDisplayName().getString(), o.effect.getDisplayName().getString());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClientEffectInstance other)) {
            return false;
        }
        if (!this.effect.equals(other.effect)) {
            return false;
        }
        if (this.isInfinite != other.isInfinite) {
            return false;
        }
        if (this.amplifier != other.amplifier) {
            return false;
        }
        if (this.isAmbient != other.isAmbient) {
            return false;
        }
        return this.duration == other.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public int getDuration() {
        return this.duration;
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public @Nullable ClientEffectInstance getHiddenInstance() {
        return this.hiddenInstance;
    }

    @Override
    public int hashCode() {
        return this.effect.hashCode();
    }

    public boolean isAmbient() {
        return this.isAmbient;
    }

    public boolean isInfinite() {
        return this.isInfinite;
    }

    public boolean isShowIcon() {
        return this.isShowIcon;
    }

    public void setAmbient(boolean ambient) {
        this.isAmbient = ambient;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setHiddenInstance(@Nullable ClientEffectInstance hiddenInstance) {
        if (!this.isInfinite) {
            this.hiddenInstance = hiddenInstance;
        }
    }

    public void setHiddenInstance(@Nullable MobEffectInstance hiddenInstance) {
        if (!this.isInfinite && hiddenInstance != null) {
            this.hiddenInstance = new ClientEffectInstance(hiddenInstance);
        }
    }

    public void setInfinite(boolean infinite) {
        this.isInfinite = infinite;
        if (infinite) {
            this.duration = 600;
        }
    }

    public void setShowIcon(boolean showIcon) {
        this.isShowIcon = showIcon;
    }

    public void tick() {
        if (!this.isInfinite && this.duration > 0) {
            this.duration--;
            if (this.hiddenInstance != null) {
                this.hiddenInstance.tick();
            }
        }
        if (this.duration == 0) {
            if (this.hiddenInstance != null) {
                ClientEffectInstance hidden = this.hiddenInstance.hiddenInstance;
                this.duration = this.hiddenInstance.duration;
                this.amplifier = this.hiddenInstance.amplifier;
                this.isShowIcon = this.hiddenInstance.isShowIcon;
                this.isAmbient = this.hiddenInstance.isAmbient;
                this.setInfinite(this.hiddenInstance.isInfinite);
                this.setHiddenInstance(hidden);
            }
        }
    }

    @Override
    public String toString() {
        if (this.amplifier > 0) {
            return this.effect.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + (this.isInfinite ? "Infinite" : this.duration);
        }
        return this.effect.getDescriptionId() + ", Duration: " + (this.isInfinite ? "Infinite" : this.duration);
    }
}
