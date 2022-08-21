package tgw.evolution.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMobEffectPatch;

@Mixin(MobEffect.class)
public abstract class MobEffectMixin extends ForgeRegistryEntry<MobEffect> implements IMobEffectPatch {

    @Override
    public float absorption(int lvl) {
        if ((Object) this == MobEffects.ABSORPTION) {
            return 4 * (lvl + 1);
        }
        return 0;
    }

    @Shadow
    public abstract void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier);

    @Override
    public float attackSpeed(int lvl) {
        if ((Object) this == MobEffects.DIG_SPEED) {
            return 0.1f * (lvl + 1);
        }
        if ((Object) this == MobEffects.DIG_SLOWDOWN) {
            return -0.1f * (lvl + 1);
        }
        return 0;
    }

    @Override
    public boolean disablesNaturalRegen() {
        return false;
    }

    @Override
    public boolean disablesSprint() {
        return MobEffects.BLINDNESS == (Object) this;
    }

    @Shadow
    public abstract String getDescriptionId();

    @Override
    public String getKey() {
        return this.getDescriptionId();
    }

    @Override
    public float health(int lvl) {
        if ((Object) this == MobEffects.HEALTH_BOOST) {
            return 4 * (lvl + 1);
        }
        return 0;
    }

    @Override
    public float hungerMod(int lvl) {
        if ((Object) this == MobEffects.HUNGER) {
            return 0.1f * (lvl + 1);
        }
        return 0;
    }

    @Override
    public float instantHP(int lvl) {
        if ((Object) this == MobEffects.HEAL) {
            return 2 * (0b1 << lvl + 1);
        }
        if ((Object) this == MobEffects.HARM) {
            return -3 * (0b1 << lvl + 1);
        }
        return 0;
    }

    @Override
    public float jumpMod(int lvl) {
        if ((Object) this == MobEffects.JUMP) {
            return 0.1f * (lvl + 1);
        }
        return 0;
    }

    @Override
    public int luck(int lvl) {
        if ((Object) this == MobEffects.LUCK) {
            return lvl + 1;
        }
        if ((Object) this == MobEffects.UNLUCK) {
            return -lvl - 1;
        }
        return 0;
    }

    @Override
    public float meleeDmg(int lvl) {
        if ((Object) this == MobEffects.DAMAGE_BOOST) {
            return 3 * (lvl + 1);
        }
        if ((Object) this == MobEffects.WEAKNESS) {
            return -4 * (lvl + 1);
        }
        return 0;
    }

    @Override
    public float miningSpeed(int lvl) {
        if ((Object) this == MobEffects.DIG_SPEED) {
            return 0.2f * (lvl + 1);
        }
        if ((Object) this == MobEffects.DIG_SLOWDOWN) {
            return (float) -(1 - Math.pow(0.3, lvl + 1));
        }
        return 0;
    }

    @Override
    public float moveSpeedMod(int lvl) {
        if ((Object) this == MobEffects.MOVEMENT_SLOWDOWN) {
            return Math.max(-0.15f * (lvl + 1), -1);
        }
        if ((Object) this == MobEffects.MOVEMENT_SPEED) {
            return 0.2f * (lvl + 1);
        }
        return 0;
    }

    @Override
    public float regen(int lvl) {
        if ((Object) this == MobEffects.POISON || (Object) this == MobEffects.WITHER) {
            return -1;
        }
        if ((Object) this == MobEffects.REGENERATION) {
            return 1;
        }
        return 0;
    }

    @Shadow
    public abstract void removeAttributeModifiers(LivingEntity pLivingEntity,
                                                  AttributeMap pAttributeMap, int pAmplifier);

    @Override
    public float resistance(int lvl) {
        if ((Object) this == MobEffects.DAMAGE_RESISTANCE) {
            return Math.min(1, 0.2f * (lvl + 1));
        }
        return 0;
    }

    @Override
    public boolean shouldDelayUpdate() {
        return (Object) this == MobEffects.ABSORPTION;
    }

    @Override
    public float staminaMod() {
        return 0;
    }

    @Override
    public double tempMod() {
        return 0;
    }

    @Override
    public float thirstMod(int lvl) {
        return 0;
    }

    @Override
    public int tickInterval(int lvl) {
        if ((Object) this == MobEffects.POISON) {
            return Math.max(1, 25 >> lvl);
        }
        if ((Object) this == MobEffects.REGENERATION) {
            return Math.max(1, 50 >> lvl);
        }
        if ((Object) this == MobEffects.WITHER) {
            return Math.max(1, 40 >> lvl);
        }
        return 0;
    }

    @Override
    public void update(LivingEntity entity, int oldLvl, int newLvl) {
        if (this.shouldDelayUpdate()) {
            this.addAttributeModifiers(entity, entity.getAttributes(), newLvl);
            this.removeAttributeModifiers(entity, entity.getAttributes(), oldLvl);
        }
        this.removeAttributeModifiers(entity, entity.getAttributes(), oldLvl);
        this.addAttributeModifiers(entity, entity.getAttributes(), newLvl);
    }
}
