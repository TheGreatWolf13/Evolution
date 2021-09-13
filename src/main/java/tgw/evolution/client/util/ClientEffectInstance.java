package tgw.evolution.client.util;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import tgw.evolution.patches.IEffectInstancePatch;

public class ClientEffectInstance implements Comparable<ClientEffectInstance> {

    private final Effect effect;
    private int amplifier;
    private int duration;
    private ClientEffectInstance hiddenInstance;
    private boolean isAmbient;
    private boolean isInfinite;
    private boolean isShowIcon;

    public ClientEffectInstance(Effect effect) {
        this.effect = effect;
    }

    public ClientEffectInstance(Effect effect, int amplifier, int duration) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public ClientEffectInstance(Effect effect, int duration) {
        this.effect = effect;
        this.duration = duration;
    }

    public ClientEffectInstance(Effect effect, int amplifier, boolean isInfinite) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.setInfinite(isInfinite);
    }

    public ClientEffectInstance(EffectInstance instance) {
        this.effect = instance.getEffect();
        this.amplifier = instance.getAmplifier();
        this.duration = instance.getDuration();
        this.isAmbient = instance.isAmbient();
        this.setInfinite(((IEffectInstancePatch) instance).isInfinite());
        this.isShowIcon = instance.showIcon();
        this.setHiddenInstance(((IEffectInstancePatch) instance).getHiddenEffect());
    }

    @Override
    public int compareTo(ClientEffectInstance o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.effect.getDisplayName().getString(), o.effect.getDisplayName().getString());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClientEffectInstance)) {
            return false;
        }
        ClientEffectInstance other = (ClientEffectInstance) o;
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

    public Effect getEffect() {
        return this.effect;
    }

    public ClientEffectInstance getHiddenInstance() {
        return this.hiddenInstance;
    }

    public boolean hasHiddenInstance() {
        return this.hiddenInstance != null;
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

    public void setHiddenInstance(ClientEffectInstance hiddenInstance) {
        if (!this.isInfinite) {
            this.hiddenInstance = hiddenInstance;
        }
    }

    public void setHiddenInstance(EffectInstance hiddenInstance) {
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
