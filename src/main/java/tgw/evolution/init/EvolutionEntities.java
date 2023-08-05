package tgw.evolution.init;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import tgw.evolution.Evolution;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.entities.misc.EntitySittable;
import tgw.evolution.entities.projectiles.EntityHook;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.entities.projectiles.EntityTorch;

import static net.minecraft.world.entity.MobCategory.MISC;

public final class EvolutionEntities {

    //Entities
//    public static final EntityType<EntityCow> COW;
//    public static final EntityType<EntityFallingPeat> FALLING_PEAT;
    public static final EntityType<EntityFallingWeight> FALLING_WEIGHT;
    public static final EntityType<EntityHook> HOOK;
    public static final EntityType<EntityPlayerCorpse> PLAYER_CORPSE;
    public static final EntityType<EntitySpear> SPEAR;
    public static final EntityType<EntitySittable> SIT;
    public static final EntityType<EntityTorch> TORCH;

    static {
//        COW = register("cow", EntityType.Builder.of(EntityCow::new, CREATURE).sized(0.9f, 1.4f));
//        FALLING_PEAT = register("falling_peat", EntityType.Builder.<EntityFallingPeat>of(EntityFallingPeat::new, MISC).sized(1.0F, 1.0F));
        FALLING_WEIGHT = register("falling_weight", EntityType.Builder.<EntityFallingWeight>of(EntityFallingWeight::new, MISC).sized(1.0F, 1.0F));
        HOOK = register("hook", EntityType.Builder.<EntityHook>of(EntityHook::new, MISC).sized(0.5f, 0.5f));
        PLAYER_CORPSE = register("player_corpse", EntityType.Builder.<EntityPlayerCorpse>of(EntityPlayerCorpse::new, MISC).sized(0.9F, 0.4F));
        SPEAR = register("spear", EntityType.Builder.<EntitySpear>of(EntitySpear::new, MISC).sized(0.1f, 0.1f));
        SIT = register("sit", EntityType.Builder.<EntitySittable>of(EntitySittable::new, MISC).sized(0.0f, 0.0f).noSummon());
        TORCH = register("torch", EntityType.Builder.<EntityTorch>of(EntityTorch::new, MISC).sized(0.2f, 0.2f));
    }

    private EvolutionEntities() {
    }

    public static void register() {
        //Entities are registered via class-loading.
    }

    private static <E extends Entity> EntityType<E> register(String name, EntityType.Builder<E> builder) {
        //noinspection ConstantConditions
        return Registry.register(Registry.ENTITY_TYPE, Evolution.getResource(name), builder.build(null));
    }

    /**
     * Adds the entity to the spawn list.
     */
    public static void registerEntityWorldSpawn(EntityType<?> entity, int weight, int minCount, int maxCount, Biome... biomes) {
//        for (Biome biome : biomes) {
//            if (biome != null) {
//                //noinspection ObjectAllocationInLoop
//                biome.getSpawns(entity.get().getClassification()).add(new SpawnListEntry(entity.get(), weight, minCount, maxCount));
//            }
//        }
        Evolution.warn("Register entity world spawn");
    }

    /**
     * Register the world spawns.
     */
    public static void registerEntityWorldSpawns() {
        //        registerEntityWorldSpawn(COW, 10, 2, 3, EvolutionBiomes.FOREST.get());
        //        registerEntityWorldSpawn(BULL, 10, 1, 2, EvolutionBiomes.FOREST.get());
    }
}
