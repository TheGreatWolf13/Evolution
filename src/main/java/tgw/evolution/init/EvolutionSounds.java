package tgw.evolution.init;

import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;

@EventBusSubscriber
public final class EvolutionSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Evolution.MODID);

    public static final RegistryObject<SoundEvent> COPPER_BREAK;
    public static final RegistryObject<SoundEvent> COPPER_FALL;
    public static final RegistryObject<SoundEvent> COPPER_HIT;
    public static final RegistryObject<SoundEvent> COPPER_PLACE;
    public static final RegistryObject<SoundEvent> COPPER_STEP;
    public static final RegistryObject<SoundEvent> JAVELIN_HIT_BLOCK;
    public static final RegistryObject<SoundEvent> JAVELIN_HIT_ENTITY;
    public static final RegistryObject<SoundEvent> JAVELIN_THROW;
    public static final RegistryObject<SoundEvent> PARRY_FAIL;
    public static final RegistryObject<SoundEvent> PARRY_SUCCESS;
    public static final RegistryObject<SoundEvent> SOIL_COLLAPSE;
    public static final RegistryObject<SoundEvent> STONE_BREAK;
    public static final RegistryObject<SoundEvent> STONE_COLLAPSE;
    public static final RegistryObject<SoundEvent> SWORD_SHEATHE;
    public static final RegistryObject<SoundEvent> SWORD_UNSHEATHE;
    public static final RegistryObject<SoundEvent> TREE_FALLING;
    public static final RegistryObject<SoundEvent> WOOD_BREAK;
    public static final RegistryObject<SoundEvent> WOOD_COLLAPSE;

    static {
        COPPER_BREAK = register("copper_break");
        COPPER_FALL = register("copper_fall");
        COPPER_HIT = register("copper_hit");
        COPPER_PLACE = register("copper_place");
        COPPER_STEP = register("copper_step");
        JAVELIN_HIT_BLOCK = register("javelin_hit_block");
        JAVELIN_HIT_ENTITY = register("javelin_hit_entity");
        JAVELIN_THROW = register("javelin_throw");
        PARRY_FAIL = register("parry_fail");
        PARRY_SUCCESS = register("parry_success");
        SOIL_COLLAPSE = register("soil_collapse");
        STONE_BREAK = register("stone_break");
        STONE_COLLAPSE = register("stone_collapse");
        SWORD_SHEATHE = register("sword_sheathe");
        SWORD_UNSHEATHE = register("sword_unsheathe");
        TREE_FALLING = register("tree_falling");
        WOOD_BREAK = SOUNDS.register("wood_break", () -> new SoundEvent(new ResourceLocation("entity.zombie.break_wooden_door")));
        WOOD_COLLAPSE = register("wood_collapse");
    }

    public static final SoundType COPPER = new ForgeSoundType(1.0F, 1.5F, COPPER_BREAK, COPPER_STEP, COPPER_PLACE, COPPER_HIT, COPPER_FALL);

    private EvolutionSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(Evolution.getResource(name)));
    }

    public static void register() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
