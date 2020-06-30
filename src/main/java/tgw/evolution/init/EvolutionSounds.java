package tgw.evolution.init;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;

@EventBusSubscriber
public class EvolutionSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, Evolution.MODID);

    public static final RegistryObject<SoundEvent> JAVELIN_HIT_BLOCK = SOUNDS.register("javelin_hit_block", () -> new SoundEvent(Evolution.location("javelin_hit_block")));
    public static final RegistryObject<SoundEvent> JAVELIN_HIT_ENTITY = SOUNDS.register("javelin_hit_entity", () -> new SoundEvent(Evolution.location("javelin_hit_entity")));
    public static final RegistryObject<SoundEvent> JAVELIN_THROW = SOUNDS.register("javelin_throw", () -> new SoundEvent(Evolution.location("javelin_throw")));
    public static final RegistryObject<SoundEvent> SOIL_COLLAPSE = SOUNDS.register("soil_collapse", () -> new SoundEvent(Evolution.location("soil_collapse")));
    public static final RegistryObject<SoundEvent> STONE_BREAK = SOUNDS.register("stone_break", () -> new SoundEvent(Evolution.location("stone_break")));
    public static final RegistryObject<SoundEvent> STONE_COLLAPSE = SOUNDS.register("stone_collapse", () -> new SoundEvent(Evolution.location("stone_collapse")));
    public static final RegistryObject<SoundEvent> TREE_FALLING = SOUNDS.register("tree_falling", () -> new SoundEvent(Evolution.location("tree_falling")));
    public static final RegistryObject<SoundEvent> WOOD_COLLAPSE = SOUNDS.register("wood_collapse", () -> new SoundEvent(Evolution.location("wood_collapse")));


    public static void register() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
