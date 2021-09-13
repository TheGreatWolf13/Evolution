package tgw.evolution.init;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.potion.EffectDizziness;
import tgw.evolution.potion.EffectGeneric;
import tgw.evolution.potion.EffectHydration;
import tgw.evolution.potion.EffectWaterIntoxication;

import java.util.List;

public final class EvolutionEffects {

    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, Evolution.MODID);
    public static final RegistryObject<Effect> DISORIENTED = EFFECTS.register("disoriented", () -> new EffectGeneric(EffectType.HARMFUL, 0xed_a677));
    public static final RegistryObject<Effect> DIZZINESS = EFFECTS.register("dizziness", EffectDizziness::new);
    public static final RegistryObject<Effect> HYDRATION = EFFECTS.register("hydration", EffectHydration::new);
    public static final RegistryObject<Effect> THIRST = EFFECTS.register("thirst", () -> new EffectGeneric(EffectType.HARMFUL, 0x45_ff4b));
    public static final RegistryObject<Effect> WATER_INTOXICATION = EFFECTS.register("water_intoxication", EffectWaterIntoxication::new);

    private EvolutionEffects() {
    }

    private static ITextComponent getEffectComp(Effect effect, int level) {
        if (effect == Effects.REGENERATION) {
            if (level > 5) {
                level = 5;
            }
            return new TranslationTextComponent(effect.getDescriptionId() + ".desc." + level);
        }
        if (effect == Effects.POISON) {
            if (level > 4) {
                level = 4;
            }
            return new TranslationTextComponent(effect.getDescriptionId() + ".desc." + level);
        }
        if (effect == Effects.WITHER) {
            if (level > 5) {
                level = 5;
            }
            return new TranslationTextComponent(effect.getDescriptionId() + ".desc." + level);
        }
        return new TranslationTextComponent(effect.getDescriptionId() + ".desc");
    }

    public static void getEffectDescription(List<ITextComponent> tooltips, Effect effect, int level) {
        tooltips.add(getEffectComp(effect, level));
    }

    public static void register() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
