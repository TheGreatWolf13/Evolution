package tgw.evolution.init;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import tgw.evolution.Evolution;
import tgw.evolution.patches.IMobEffectInstancePatch;
import tgw.evolution.patches.IMobEffectPatch;
import tgw.evolution.potion.*;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.EffectHolder;

import java.util.List;

public final class EvolutionEffects {

    public static final RegistryObject<EffectGeneric> ANAEMIA;
    public static final RegistryObject<EffectDehydration> DEHYDRATION;
    public static final RegistryObject<EffectGeneric> DISORIENTED;
    public static final RegistryObject<EffectDizziness> DIZZINESS;
    public static final RegistryObject<EffectHydration> HYDRATION;
    public static final RegistryObject<EffectGeneric> OVEREAT;
    public static final RegistryObject<EffectSaturation> SATURATION;
    public static final RegistryObject<EffectShivering> SHIVERING;
    public static final RegistryObject<EffectGeneric> STARVATION;
    public static final RegistryObject<EffectSweating> SWEATING;
    public static final RegistryObject<EffectThirst> THIRST;
    public static final RegistryObject<EffectWaterIntoxication> WATER_INTOXICATION;
    //
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Evolution.MODID);

    static {
        ANAEMIA = EFFECTS.register("anaemia", () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0xdd_dd00));
        DEHYDRATION = EFFECTS.register("dehydration", EffectDehydration::new);
        DISORIENTED = EFFECTS.register("disoriented", () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0xed_a677));
        DIZZINESS = EFFECTS.register("dizziness", EffectDizziness::new);
        HYDRATION = EFFECTS.register("hydration", EffectHydration::new);
        OVEREAT = EFFECTS.register("overeat", () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0));
        SATURATION = EFFECTS.register("saturation", EffectSaturation::new);
        SHIVERING = EFFECTS.register("shivering", EffectShivering::new);
        STARVATION = EFFECTS.register("starvation", () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0));
        SWEATING = EFFECTS.register("sweating", EffectSweating::new);
        THIRST = EFFECTS.register("thirst", EffectThirst::new);
        WATER_INTOXICATION = EFFECTS.register("water_intoxication", EffectWaterIntoxication::new);
    }

    private EvolutionEffects() {
    }

    @Contract("_, _, _ -> true")
    private static boolean addCause(List<Component> tooltips, Component text, boolean addedCausesHeader) {
        if (!addedCausesHeader) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.TOOLTIP_EFFECT_CAUSES);
        }
        tooltips.add(text);
        return true;
    }

    private static Component getEffectComp(MobEffect effect, int lvl) {
        return ((IMobEffectPatch) effect).getDescription(lvl);
    }

    public static void getEffectDescription(List<Component> tooltips, MobEffect effect, int lvl) {
        tooltips.add(getEffectComp(effect, lvl));
        IMobEffectPatch patch = (IMobEffectPatch) effect;
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
        ObjectList<EffectHolder> causes = patch.causesEffect();
        if (!causes.isEmpty()) {
            for (int index = 0, l = causes.size(); index < l; index++) {
                MobEffectInstance instance = causes.get(index).getInstance(lvl);
                if (instance != null) {
                    addedCausesHeader = addCause(tooltips, EvolutionTexts.effect(instance), addedCausesHeader);
                }
            }
        }
        boolean mayCauseAnything = patch.mayCauseAnything();
        if (mayCauseAnything) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.TOOLTIP_EFFECT_MAY_CAUSE);
            ObjectList<ChanceEffectHolder> mayCause = patch.mayCauseEffect();
            if (!mayCause.isEmpty()) {
                for (int index = 0, l = mayCause.size(); index < l; index++) {
                    MobEffectInstance instance = mayCause.get(index).getInstance(lvl);
                    if (instance != null) {
                        tooltips.add(EvolutionTexts.effect(instance));
                    }
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
        ((IMobEffectInstancePatch) instance).setInfinite(true);
        return instance;
    }

    public static void register() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
