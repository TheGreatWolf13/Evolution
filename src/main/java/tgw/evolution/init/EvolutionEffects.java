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

import java.util.Arrays;
import java.util.List;

public final class EvolutionEffects {

    public static final DeferredRegister<Effect> EFFECTS = new DeferredRegister<>(ForgeRegistries.POTIONS, Evolution.MODID);
    public static final RegistryObject<Effect> DIZZINESS = EFFECTS.register("dizziness", EffectDizziness::new);
    public static final RegistryObject<Effect> DISORIENTED = EFFECTS.register("disoriented", () -> new EffectGeneric(EffectType.HARMFUL, 0xed_a677));

    private EvolutionEffects() {
    }

    private static ITextComponent getEffectComp(Effect effect, int level) {
        if (effect == Effects.REGENERATION) {
            if (level > 5) {
                level = 5;
            }
            return new TranslationTextComponent(effect.getName() + ".desc." + level);
        }
        if (effect == Effects.POISON) {
            if (level > 4) {
                level = 4;
            }
            return new TranslationTextComponent(effect.getName() + ".desc." + level);
        }
        return new TranslationTextComponent(effect.getName() + ".desc");
    }

    public static void getEffectDescription(List<String> tooltips, Effect effect, int level) {
        String desc = getEffectComp(effect, level).getFormattedText();
        tooltips.addAll(Arrays.asList(desc.split("\n")));
    }

    public static void register() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
