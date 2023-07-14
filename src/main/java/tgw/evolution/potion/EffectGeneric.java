package tgw.evolution.potion;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import tgw.evolution.patches.PatchMobEffect;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.EffectHolder;

public class EffectGeneric extends MobEffect implements PatchMobEffect {

    public EffectGeneric(MobEffectCategory category, int liquidColor) {
        super(category, liquidColor);
    }

    @Override
    public float absorption(int lvl) {
        return 0;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        ObjectList<EffectHolder> causes = this.causesEffect();
        for (int i = 0, l = causes.size(); i < l; i++) {
            EffectHolder holder = causes.get(i);
            if (holder.shouldApply(amplifier, entity)) {
                holder.apply(amplifier, entity);
            }
        }
        super.addAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int lvl) {
        if (entity.level.isClientSide) {
            return;
        }
        if (this.causesRegen(lvl)) {
            entity.heal(this.regen(lvl));
        }
        else if (this.causesDmg(lvl) && this.shouldHurt(lvl)) {
            entity.hurt(this.dmgSource(), -this.regen(lvl));
        }
        ObjectList<EffectHolder> causes = this.causesEffect();
        for (int i = 0, l = causes.size(); i < l; i++) {
            EffectHolder holder = causes.get(i);
            if (holder.shouldReapply(lvl, entity)) {
                holder.apply(lvl, entity);
            }
        }
        ObjectList<ChanceEffectHolder> mayCause = this.mayCauseEffect();
        for (int i = 0, l = mayCause.size(); i < l; i++) {
            ChanceEffectHolder holder = mayCause.get(i);
            if (holder.shouldReapply(lvl, entity)) {
                holder.apply(lvl, entity);
            }
        }
    }

    @Override
    public float attackSpeed(int lvl) {
        return 0;
    }

    @Override
    public boolean disablesNaturalRegen() {
        return false;
    }

    @Override
    public boolean disablesSprint() {
        return false;
    }

    @Override
    public final String getKey() {
        return this.getDescriptionId();
    }

    @Override
    public float health(int lvl) {
        return 0;
    }

    @Override
    public float hungerMod(int lvl) {
        return 0;
    }

    @Override
    public float instantHP(int lvl) {
        return 0;
    }

    @Override
    public final boolean isDurationEffectTick(int duration, int amplifier) {
        int tickInterval = this.tickInterval(amplifier);
        if (tickInterval == 0) {
            return false;
        }
        if (tickInterval == 1) {
            return true;
        }
        return duration % tickInterval == 0;
    }

    @Override
    public float jumpMod(int lvl) {
        return 0;
    }

    @Override
    public int luck(int lvl) {
        return 0;
    }

    @Override
    public float meleeDmg(int lvl) {
        return 0;
    }

    @Override
    public float miningSpeed(int lvl) {
        return 0;
    }

    @Override
    public float moveSpeedMod(int lvl) {
        return 0;
    }

    @Override
    public float regen(int lvl) {
        return 0;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        ObjectList<EffectHolder> causes = this.causesEffect();
        for (int i = 0, l = causes.size(); i < l; i++) {
            causes.get(i).remove(amplifier, entity);
        }
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public float resistance(int lvl) {
        return 0;
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
        return 0;
    }

    @Override
    public final void update(LivingEntity entity, int oldLvl, int newLvl) {
        if (this.shouldDelayUpdate()) {
            this.addAttributeModifiers(entity, entity.getAttributes(), newLvl);
            this.removeAttributeModifiers(entity, entity.getAttributes(), oldLvl);
        }
        this.removeAttributeModifiers(entity, entity.getAttributes(), oldLvl);
        this.addAttributeModifiers(entity, entity.getAttributes(), newLvl);
    }
}
