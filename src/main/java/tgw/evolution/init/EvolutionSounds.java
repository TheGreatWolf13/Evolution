package tgw.evolution.init;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;

@EventBusSubscriber
public final class EvolutionSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Evolution.MODID);

    public static final RegistryObject<SoundEvent> JAVELIN_HIT_BLOCK = SOUNDS.register("javelin_hit_block",
                                                                                       () -> new SoundEvent(Evolution.getResource(
                                                                                               "javelin_hit_block")));
    public static final RegistryObject<SoundEvent> JAVELIN_HIT_ENTITY = SOUNDS.register("javelin_hit_entity",
                                                                                        () -> new SoundEvent(Evolution.getResource(
                                                                                                "javelin_hit_entity")));
    public static final RegistryObject<SoundEvent> JAVELIN_THROW = SOUNDS.register("javelin_throw",
                                                                                   () -> new SoundEvent(Evolution.getResource("javelin_throw")));
    public static final RegistryObject<SoundEvent> PARRY_FAIL = SOUNDS.register("parry_fail",
                                                                                () -> new SoundEvent(Evolution.getResource("parry_fail")));
    public static final RegistryObject<SoundEvent> PARRY_SUCCESS = SOUNDS.register("parry_success",
                                                                                   () -> new SoundEvent(Evolution.getResource("parry_success")));
    public static final RegistryObject<SoundEvent> SOIL_COLLAPSE = SOUNDS.register("soil_collapse",
                                                                                   () -> new SoundEvent(Evolution.getResource("soil_collapse")));
    public static final RegistryObject<SoundEvent> STONE_BREAK = SOUNDS.register("stone_break",
                                                                                 () -> new SoundEvent(Evolution.getResource("stone_break")));
    public static final RegistryObject<SoundEvent> STONE_COLLAPSE = SOUNDS.register("stone_collapse",
                                                                                    () -> new SoundEvent(Evolution.getResource("stone_collapse")));
    public static final RegistryObject<SoundEvent> SWORD_SHEATH = SOUNDS.register("sword_sheath",
                                                                                  () -> new SoundEvent(Evolution.getResource("sword_sheath")));
    public static final RegistryObject<SoundEvent> SWORD_UNSHEATH = SOUNDS.register("sword_unsheath",
                                                                                    () -> new SoundEvent(Evolution.getResource("sword_unsheath")));
    public static final RegistryObject<SoundEvent> TREE_FALLING = SOUNDS.register("tree_falling",
                                                                                  () -> new SoundEvent(Evolution.getResource("tree_falling")));
    public static final RegistryObject<SoundEvent> WOOD_COLLAPSE = SOUNDS.register("wood_collapse",
                                                                                   () -> new SoundEvent(Evolution.getResource("wood_collapse")));

    private EvolutionSounds() {
    }

    public static void register() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
