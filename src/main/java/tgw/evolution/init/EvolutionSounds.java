package tgw.evolution.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;

public final class EvolutionSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Evolution.MODID);

    public static final RegistryObject<SoundEvent> METAL_SPEAR_HIT_ENTITY;
    public static final RegistryObject<SoundEvent> METAL_SPEAR_THROW;
    public static final RegistryObject<SoundEvent> METAL_WEAPON_BLOCKS;
    public static final RegistryObject<SoundEvent> METAL_WEAPON_HIT_BLOCK;
    public static final RegistryObject<SoundEvent> SOIL_COLLAPSE;
    public static final RegistryObject<SoundEvent> STONE_BREAK;
    public static final RegistryObject<SoundEvent> STONE_COLLAPSE;
    public static final RegistryObject<SoundEvent> STONE_SPEAR_HIT_ENTITY;
    public static final RegistryObject<SoundEvent> STONE_SPEAR_THROW;
    public static final RegistryObject<SoundEvent> STONE_WEAPON_HIT_BLOCK;
    public static final RegistryObject<SoundEvent> SWORD_SHEATHE;
    public static final RegistryObject<SoundEvent> SWORD_UNSHEATHE;
    public static final RegistryObject<SoundEvent> TREE_FALLING;
    public static final RegistryObject<SoundEvent> WOOD_BREAK;
    public static final RegistryObject<SoundEvent> WOOD_COLLAPSE;

    static {
        METAL_SPEAR_HIT_ENTITY = register("metal_spear_hit_entity");
        METAL_SPEAR_THROW = register("metal_spear_throw");
        METAL_WEAPON_BLOCKS = register("metal_weapon_blocks");
        METAL_WEAPON_HIT_BLOCK = register("metal_weapon_hit_block");
        SOIL_COLLAPSE = register("soil_collapse");
        STONE_BREAK = register("stone_break");
        STONE_COLLAPSE = register("stone_collapse");
        STONE_SPEAR_HIT_ENTITY = register("stone_spear_hit_entity");
        STONE_SPEAR_THROW = register("stone_spear_throw");
        STONE_WEAPON_HIT_BLOCK = register("stone_weapon_hit_block");
        SWORD_SHEATHE = register("sword_sheathe");
        SWORD_UNSHEATHE = register("sword_unsheathe");
        TREE_FALLING = register("tree_falling");
        WOOD_BREAK = register("wood_break");
        WOOD_COLLAPSE = register("wood_collapse");
    }

    private EvolutionSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(Evolution.getResource(name)));
    }

    public static void register() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
