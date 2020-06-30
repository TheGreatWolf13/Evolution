package tgw.evolution.init;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.potion.EffectDizziness;
import tgw.evolution.potion.EffectGeneric;

@SuppressWarnings("unused")
public class EvolutionEffects {

    public static final DeferredRegister<Effect> EFFECTS = new DeferredRegister<>(ForgeRegistries.POTIONS, Evolution.MODID);
    public static final RegistryObject<Effect> DIZZINESS = EFFECTS.register("dizziness", EffectDizziness::new);
    public static final RegistryObject<Effect> DISORIENTED = EFFECTS.register("disoriented", () -> new EffectGeneric(EffectType.HARMFUL, 0xeda677));

    public static void register() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
