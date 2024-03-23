package tgw.evolution.init;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Contract;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchMobEffect;
import tgw.evolution.potion.*;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.EffectHolder;
import tgw.evolution.util.collection.lists.OList;

public final class EvolutionEffects {

    public static final EffectGeneric ANAEMIA;
    public static final EffectDehydration DEHYDRATION;
    public static final EffectGeneric DISORIENTED;
    public static final EffectDizziness DIZZINESS;
    public static final EffectHydration HYDRATION;
    public static final EffectGeneric OVEREAT;
    public static final EffectSaturation SATURATION;
    public static final EffectShivering SHIVERING;
    public static final EffectGeneric STARVATION;
    public static final EffectSweating SWEATING;
    public static final EffectThirst THIRST;
    public static final EffectWaterIntoxication WATER_INTOXICATION;

    static {
        ANAEMIA = register("anaemia", new EffectGeneric(MobEffectCategory.HARMFUL, 0xdd_dd00));
        DEHYDRATION = register("dehydration", new EffectDehydration());
        DISORIENTED = register("disoriented", new EffectGeneric(MobEffectCategory.HARMFUL, 0xed_a677));
        DIZZINESS = register("dizziness", new EffectDizziness());
        HYDRATION = register("hydration", new EffectHydration());
        OVEREAT = register("overeat", new EffectGeneric(MobEffectCategory.HARMFUL, 0));
        SATURATION = register("saturation", new EffectSaturation());
        SHIVERING = register("shivering", new EffectShivering());
        STARVATION = register("starvation", new EffectGeneric(MobEffectCategory.HARMFUL, 0));
        SWEATING = register("sweating", new EffectSweating());
        THIRST = register("thirst", new EffectThirst());
        WATER_INTOXICATION = register("water_intoxication", new EffectWaterIntoxication());
    }

    private EvolutionEffects() {
    }

    public static void getEffectDescription(OList<Component> tooltips, MobEffect effect, int lvl) {
        tooltips.add(getEffectComp(effect, lvl));
        PatchMobEffect patch = (PatchMobEffect) effect;
        boolean addedCausesHeader = false;
        if (patch.disablesNaturalRegen()) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.TOOLTIP_EFFECT_DISABLE_REGEN, false);
        }
        if (patch.disablesSprint()) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.TOOLTIP_EFFECT_DISABLE_SPRINT, addedCausesHeader);
        }
        float f = patch.absorption(lvl);
        if (f > 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectAbsorption(f), addedCausesHeader);
        }
        f = patch.health(lvl);
        if (f != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectHealth(f), addedCausesHeader);
        }
        f = patch.instantHP(lvl);
        if (f != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectInstaHP(f), addedCausesHeader);
        }
        f = patch.resistance(lvl);
        if (f > 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectResist(f), addedCausesHeader);
        }
        f = patch.meleeDmg(lvl);
        if (f != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectMeleeDmg(f), addedCausesHeader);
        }
        f = patch.attackSpeed(lvl);
        if (f != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectAttSpeed(f), addedCausesHeader);
        }
        f = patch.miningSpeed(lvl);
        if (f != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectMining(f), addedCausesHeader);
        }
        f = patch.hungerMod(lvl);
        if (f > 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectHunger(f), addedCausesHeader);
        }
        f = patch.thirstMod(lvl);
        if (f > 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectThirst(f), addedCausesHeader);
        }
        double d = patch.tempMod();
        if (d != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectTemperature(d), addedCausesHeader);
        }
        f = patch.moveSpeedMod(lvl);
        if (f != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectSpeed(f), addedCausesHeader);
        }
        f = patch.jumpMod(lvl);
        if (f != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectJump(f), addedCausesHeader);
        }
        int i = patch.luck(lvl);
        if (i != 0) {
            addedCausesHeader = addCause(tooltips, EvolutionTexts.effectLuck(i), addedCausesHeader);
        }
        OList<EffectHolder> causes = patch.causesEffect();
        for (int index = 0, l = causes.size(); index < l; index++) {
            MobEffectInstance instance = causes.get(index).getInstance(lvl);
            if (instance != null) {
                addedCausesHeader = addCause(tooltips, EvolutionTexts.effect(instance), addedCausesHeader);
            }
        }
        boolean mayCauseAnything = patch.mayCauseAnything();
        if (mayCauseAnything) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.TOOLTIP_EFFECT_MAY_CAUSE);
            OList<ChanceEffectHolder> mayCause = patch.mayCauseEffect();
            for (int index = 0, l = mayCause.size(); index < l; index++) {
                MobEffectInstance instance = mayCause.get(index).getInstance(lvl);
                if (instance != null) {
                    tooltips.add(EvolutionTexts.effect(instance));
                }
            }
        }
        if (patch.causesRegen(lvl)) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.effectRegen(patch.regen(lvl), patch.tickInterval(lvl), addedCausesHeader || mayCauseAnything));
        }
        else if (patch.causesDmg(lvl)) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.effectDmg(-patch.regen(lvl), patch.tickInterval(lvl), addedCausesHeader || mayCauseAnything));
        }
    }

    @Contract("_, _, _, _, _ -> new")
    public static MobEffectInstance infiniteOf(MobEffect effect, int amplifier, boolean isAmbient, boolean showParticles, boolean showIcon) {
        MobEffectInstance instance = new MobEffectInstance(effect, 10, amplifier, isAmbient, showParticles, showIcon);
        instance.setInfinite(true);
        return instance;
    }

    public static void register() {
        //Effects are registered via class-loading.
    }

    @Contract("_, _, _ -> true")
    private static boolean addCause(OList<Component> tooltips, Component text, boolean addedCausesHeader) {
        if (!addedCausesHeader) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.TOOLTIP_EFFECT_CAUSES);
        }
        tooltips.add(text);
        return true;
    }

    private static Component getEffectComp(MobEffect effect, int lvl) {
        return ((PatchMobEffect) effect).getDescription(lvl);
    }

    private static <E extends MobEffect> E register(String name, E effect) {
        return Registry.register(Registry.MOB_EFFECT, Evolution.getResource(name), effect);
    }
}
