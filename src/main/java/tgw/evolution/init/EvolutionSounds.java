package tgw.evolution.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import tgw.evolution.Evolution;

public final class EvolutionSounds {

    public static final SoundEvent FIST_PUNCHES_BLOCK;
    public static final SoundEvent METAL_SPEAR_HIT_ENTITY;
    public static final SoundEvent METAL_SPEAR_THROW;
    public static final SoundEvent METAL_WEAPON_BLOCKS;
    public static final SoundEvent METAL_WEAPON_HIT_BLOCK;
    public static final SoundEvent SOIL_COLLAPSE;
    public static final SoundEvent STONE_BREAK;
    public static final SoundEvent STONE_COLLAPSE;
    public static final SoundEvent STONE_SPEAR_HIT_ENTITY;
    public static final SoundEvent STONE_SPEAR_THROW;
    public static final SoundEvent STONE_WEAPON_HIT_BLOCK;
    public static final SoundEvent SWORD_SHEATHE;
    public static final SoundEvent SWORD_UNSHEATHE;
    public static final SoundEvent TREE_FALLING;
    public static final SoundEvent WOOD_BREAK;
    public static final SoundEvent WOOD_COLLAPSE;

    static {
        FIST_PUNCHES_BLOCK = register("fist_punches_block");
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

    private static SoundEvent register(String name) {
        ResourceLocation loc = Evolution.getResource(name);
        return Registry.register(Registry.SOUND_EVENT, loc, new SoundEvent(loc));
    }

    public static void register() {
        //Sounds are registered via class-loading
    }
}
