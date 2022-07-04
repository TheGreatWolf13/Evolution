package tgw.evolution.init;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.potion.*;

import java.util.List;

public final class EvolutionEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Evolution.MODID);
    public static final RegistryObject<MobEffect> ANAEMIA = EFFECTS.register("anaemia",
                                                                             () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0xdd_dd00));
    public static final RegistryObject<MobEffect> DEHYDRATION = EFFECTS.register("dehydration", EffectDehydration::new);
    public static final RegistryObject<MobEffect> DISORIENTED = EFFECTS.register("disoriented",
                                                                                 () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0xed_a677));
    public static final RegistryObject<MobEffect> DIZZINESS = EFFECTS.register("dizziness", EffectDizziness::new);
    public static final RegistryObject<MobEffect> HYDRATION = EFFECTS.register("hydration", EffectHydration::new);
    //TODO
    public static final RegistryObject<MobEffect> OVEREAT = EFFECTS.register("overeat", () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<MobEffect> SATURATION = EFFECTS.register("saturation", EffectSaturation::new);
    public static final RegistryObject<MobEffect> SHIVERING = EFFECTS.register("shivering",
                                                                               () -> new EffectGeneric(MobEffectCategory.NEUTRAL, 0xee_eeee));
    //TODO
    public static final RegistryObject<MobEffect> STARVATION = EFFECTS.register("starvation", () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<MobEffect> SWEATING = EFFECTS.register("sweating",
                                                                              () -> new EffectGeneric(MobEffectCategory.NEUTRAL, 0x0067_dd));
    public static final RegistryObject<MobEffect> THIRST = EFFECTS.register("thirst", () -> new EffectGeneric(MobEffectCategory.HARMFUL, 0x45_ff4b));
    public static final RegistryObject<MobEffect> WATER_INTOXICATION = EFFECTS.register("water_intoxication", EffectWaterIntoxication::new);

    private EvolutionEffects() {
    }

    private static Component getEffectComp(MobEffect effect, int level) {
        if (effect == DEHYDRATION.get()) {
            if (level > 8) {
                level = 8;
            }
            return new TranslatableComponent(effect.getDescriptionId() + ".desc." + level);
        }
        if (effect == WATER_INTOXICATION.get()) {
            if (level > 1) {
                level = 1;
            }
            return new TranslatableComponent(effect.getDescriptionId() + ".desc." + level);
        }
        if (effect == MobEffects.REGENERATION) {
            if (level > 5) {
                level = 5;
            }
            return new TranslatableComponent(effect.getDescriptionId() + ".desc." + level);
        }
        if (effect == MobEffects.POISON) {
            if (level > 4) {
                level = 4;
            }
            return new TranslatableComponent(effect.getDescriptionId() + ".desc." + level);
        }
        if (effect == MobEffects.WITHER) {
            if (level > 5) {
                level = 5;
            }
            return new TranslatableComponent(effect.getDescriptionId() + ".desc." + level);
        }
        return new TranslatableComponent(effect.getDescriptionId() + ".desc");
    }

    public static void getEffectDescription(List<Component> tooltips, MobEffect effect, int level) {
        tooltips.add(getEffectComp(effect, level));
    }

    public static void register() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
