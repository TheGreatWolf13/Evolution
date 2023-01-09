package tgw.evolution.patches;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.EffectHolder;

public interface IMobEffectPatch {

    float absorption(int lvl);

    float attackSpeed(int lvl);

    default boolean causesDmg(int lvl) {
        return this.regen(lvl) < 0;
    }

    @NotNull
    default ObjectList<EffectHolder> causesEffect() {
        return ObjectLists.emptyList();
    }

    default boolean causesRegen(int lvl) {
        return this.regen(lvl) > 0;
    }

    default int customDescriptionUntil() {
        return 0;
    }

    boolean disablesNaturalRegen();

    boolean disablesSprint();

    default DamageSource dmgSource() {
        return DamageSource.GENERIC;
    }

    default Component getDescription(int lvl) {
        int until = this.customDescriptionUntil();
        if (until == 0) {
            return new TranslatableComponent(this.getKey() + ".desc");
        }
        return new TranslatableComponent(this.getKey() + ".desc." + Math.min(until, lvl));
    }

    String getKey();

    float health(int lvl);

    float hungerMod(int lvl);

    float instantHP(int lvl);

    float jumpMod(int lvl);

    int luck(int lvl);

    default boolean mayCauseAnything() {
        return !this.mayCauseEffect().isEmpty();
    }

    @NotNull
    default ObjectList<ChanceEffectHolder> mayCauseEffect() {
        return ObjectLists.emptyList();
    }

    float meleeDmg(int lvl);

    float miningSpeed(int lvl);

    float moveSpeedMod(int lvl);

    float regen(int lvl);

    float resistance(int lvl);

    default boolean shouldDelayUpdate() {
        return false;
    }

    default boolean shouldHurt(int lvl) {
        return this.causesDmg(lvl);
    }

    float staminaMod();

    double tempMod();

    float thirstMod(int lvl);

    int tickInterval(int lvl);

    void update(LivingEntity entity, int oldLvl, int newLvl);
}
