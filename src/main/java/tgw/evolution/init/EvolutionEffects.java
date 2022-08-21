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
import tgw.evolution.Evolution;
import tgw.evolution.patches.IMobEffectInstancePatch;
import tgw.evolution.patches.IMobEffectPatch;
import tgw.evolution.potion.*;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.EffectHolder;

import java.util.List;

public final class EvolutionEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Evolution.MODID);

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

//    private static final RSet<MobEffect> DISABLE_NATURAL_REGEN = new ROpenHashSet<>();
//    private static final RSet<MobEffect> DISABLE_SPRINT_EFFECTS = new ROpenHashSet<>();

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

//    public static boolean canNaturalRegen(LivingEntity entity) {
//        return Collections.disjoint(entity.getActiveEffectsMap().keySet(), DISABLE_NATURAL_REGEN);
//    }
//
//    public static boolean canSprint(LivingEntity entity) {
//        return Collections.disjoint(entity.getActiveEffectsMap().keySet(), DISABLE_SPRINT_EFFECTS);
//    }

//    public static void finishRegisters() {
//        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
//            IMobEffectPatch patch = (IMobEffectPatch) effect;
//            if (patch.disablesNaturalRegen()) {
//                DISABLE_NATURAL_REGEN.add(effect);
//            }
//            if (patch.disablesSprint()) {
//                DISABLE_SPRINT_EFFECTS.add(effect);
//            }
//        }
//        DISABLE_NATURAL_REGEN.trimCollection();
//        DISABLE_SPRINT_EFFECTS.trimCollection();
//    }

    private static Component getEffectComp(MobEffect effect, int lvl) {
        return ((IMobEffectPatch) effect).getDescription(lvl);
    }

    public static void getEffectDescription(List<Component> tooltips, MobEffect effect, int lvl) {
        tooltips.add(getEffectComp(effect, lvl));
        IMobEffectPatch patch = (IMobEffectPatch) effect;
        boolean causesAnything = patch.causesAnything(lvl);
        if (causesAnything) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.TOOLTIP_EFFECT_CAUSES);
            if (patch.disablesNaturalRegen()) {
                tooltips.add(EvolutionTexts.TOOLTIP_EFFECT_DISABLE_REGEN);
            }
            if (patch.disablesSprint()) {
                tooltips.add(EvolutionTexts.TOOLTIP_EFFECT_DISABLE_SPRINT);
            }
            float f = patch.absorption(lvl);
            if (f > 0) {
                tooltips.add(EvolutionTexts.effectAbsorption(f));
            }
            f = patch.health(lvl);
            if (f != 0) {
                tooltips.add(EvolutionTexts.effectHealth(f));
            }
            f = patch.instantHP(lvl);
            if (f != 0) {
                tooltips.add(EvolutionTexts.effectInstaHP(f));
            }
            f = patch.resistance(lvl);
            if (f > 0) {
                tooltips.add(EvolutionTexts.effectResist(f));
            }
            f = patch.meleeDmg(lvl);
            if (f != 0) {
                tooltips.add(EvolutionTexts.effectMeleeDmg(f));
            }
            f = patch.attackSpeed(lvl);
            if (f != 0) {
                tooltips.add(EvolutionTexts.effectAttSpeed(f));
            }
            f = patch.miningSpeed(lvl);
            if (f != 0) {
                tooltips.add(EvolutionTexts.effectMining(f));
            }
            f = patch.hungerMod(lvl);
            if (f > 0) {
                tooltips.add(EvolutionTexts.effectHunger(f));
            }
            f = patch.thirstMod(lvl);
            if (f > 0) {
                tooltips.add(EvolutionTexts.effectThirst(f));
            }
            double d = patch.tempMod();
            if (d != 0) {
                tooltips.add(EvolutionTexts.effectTemperature(d));
            }
            f = patch.moveSpeedMod(lvl);
            if (f != 0) {
                tooltips.add(EvolutionTexts.effectSpeed(f));
            }
            f = patch.jumpMod(lvl);
            if (f != 0) {
                tooltips.add(EvolutionTexts.effectJump(f));
            }
            int i = patch.luck(lvl);
            if (i != 0) {
                tooltips.add(EvolutionTexts.effectLuck(i));
            }
            ObjectList<EffectHolder> causes = patch.causesEffect();
            if (!causes.isEmpty()) {
                for (int index = 0, l = causes.size(); index < l; index++) {
                    MobEffectInstance instance = causes.get(index).getInstance(lvl);
                    if (instance != null) {
                        tooltips.add(EvolutionTexts.effect(instance));
                    }
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
            tooltips.add(EvolutionTexts.effectRegen(patch.regen(lvl), patch.tickInterval(lvl), causesAnything || mayCauseAnything));
        }
        else if (patch.causesDmg(lvl)) {
            tooltips.add(EvolutionTexts.EMPTY);
            tooltips.add(EvolutionTexts.effectDmg(-patch.regen(lvl), patch.tickInterval(lvl), causesAnything || mayCauseAnything));
        }
    }

    public static MobEffectInstance infiniteOf(MobEffect effect, int amplifier, boolean isAmbient, boolean showParticles, boolean showIcon) {
        MobEffectInstance instance = new MobEffectInstance(effect, 10, amplifier, isAmbient, showParticles, showIcon);
        ((IMobEffectInstancePatch) instance).setInfinite(true);
        return instance;
    }

    public static void register() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
