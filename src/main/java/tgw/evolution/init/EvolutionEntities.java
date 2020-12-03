package tgw.evolution.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.entities.EntityCow;
import tgw.evolution.entities.misc.*;
import tgw.evolution.entities.projectiles.EntityHook;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.entities.projectiles.EntityTorch;
import tgw.evolution.items.ItemSpawnEgg;

import java.util.function.Supplier;

import static net.minecraft.entity.EntityClassification.CREATURE;
import static net.minecraft.entity.EntityClassification.MISC;

@EventBusSubscriber
public final class EvolutionEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, Evolution.MODID);

    public static final RegistryObject<EntityType<EntityFallingPeat>> FALLING_PEAT = register("falling_peat", EvolutionEntities::fallingPeat);
    public static final RegistryObject<EntityType<EntityFallingWeight>> FALLING_WEIGHT = register("falling_weight", EvolutionEntities::fallingWeight);
    public static final RegistryObject<EntityType<EntityFallingTimber>> FALLING_TIMBER = register("falling_timber", EvolutionEntities::fallingTimber);
    public static final RegistryObject<EntityType<EntitySpear>> SPEAR = register("spear", EvolutionEntities::spear);
    public static final RegistryObject<EntityType<EntityCow>> COW = register("cow", EvolutionEntities::cow);
    //    public static final RegistryObject<EntityType<EntityBull>> BULL = register("bull", EvolutionEntities::bull);
    //    public static final RegistryObject<EntityType<EntityShadowHound>> SHADOWHOUND = register("shadowhound", EvolutionEntities::shadowHound);
    public static final RegistryObject<EntityType<EntitySit>> SIT = register("sit", EvolutionEntities::sit);
    public static final RegistryObject<EntityType<EntityHook>> HOOK = register("hook", EvolutionEntities::hook);
    public static final RegistryObject<EntityType<EntityTorch>> TORCH = register("torch", EvolutionEntities::torch);
    public static final RegistryObject<EntityType<EntityPlayerCorpse>> PLAYER_CORPSE = register("player_corpse", EvolutionEntities::playerCorpse);

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Evolution.MODID);

    public static final RegistryObject<Item> SPAWN_EGG_COW = ITEMS.register("spawn_egg_cow", () -> genEgg(COW));
    //    public static final RegistryObject<Item> SPAWN_EGG_BULL = ITEMS.register("spawn_egg_bull", () -> genEgg(BULL));
    //    public static final RegistryObject<Item> SPAWN_EGG_SHADOWHOUND = ITEMS.register("spawn_egg_shadowhound", () -> genEgg(SHADOWHOUND));

    private EvolutionEntities() {
    }

    private static EntityType.Builder<EntityCow> cow() {
        return EntityType.Builder.create(EntityCow::new, CREATURE).size(0.9f, 1.4f);
    }

    private static EntityType.Builder<EntityFallingPeat> fallingPeat() {
        return EntityType.Builder.<EntityFallingPeat>create(EntityFallingPeat::new, MISC).size(1.0F, 1.0F)
                                                                                         .setTrackingRange(10)
                                                                                         .setUpdateInterval(20)
                                                                                         .setCustomClientFactory(EntityFallingPeat::new)
                                                                                         .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityFallingTimber> fallingTimber() {
        return EntityType.Builder.<EntityFallingTimber>create(EntityFallingTimber::new, MISC).size(1.0F, 1.0F)
                                                                                             .setTrackingRange(10)
                                                                                             .setUpdateInterval(20)
                                                                                             .setCustomClientFactory(EntityFallingTimber::new)
                                                                                             .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityFallingWeight> fallingWeight() {
        return EntityType.Builder.<EntityFallingWeight>create(EntityFallingWeight::new, MISC).size(1.0F, 1.0F)
                                                                                             .setTrackingRange(10)
                                                                                             .setUpdateInterval(20)
                                                                                             .setCustomClientFactory(EntityFallingWeight::new)
                                                                                             .setShouldReceiveVelocityUpdates(true);
    }

    /**
     * Returns a new Spawn Egg Item.
     */
    public static <E extends Entity> Item genEgg(RegistryObject<EntityType<E>> type) {
        return new ItemSpawnEgg<>(type);
    }

    private static EntityType.Builder<EntityHook> hook() {
        return EntityType.Builder.<EntityHook>create(EntityHook::new, MISC).size(0.5f, 0.5f)
                                                                           .setTrackingRange(5)
                                                                           .setUpdateInterval(20)
                                                                           .setCustomClientFactory(EntityHook::new)
                                                                           .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityPlayerCorpse> playerCorpse() {
        return EntityType.Builder.<EntityPlayerCorpse>create(EntityPlayerCorpse::new, MISC).size(0.9F, 0.4F)
                                                                                           .setTrackingRange(10)
                                                                                           .setUpdateInterval(20)
                                                                                           .setCustomClientFactory(EntityPlayerCorpse::new)
                                                                                           .setShouldReceiveVelocityUpdates(true);
    }

    private static <E extends Entity> RegistryObject<EntityType<E>> register(String name, Supplier<EntityType.Builder<E>> builder) {
        return ENTITIES.register(name, () -> builder.get().build(name));
    }

    public static void register() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    /**
     * Adds the entity to the spawn list.
     */
    public static void registerEntityWorldSpawn(RegistryObject<EntityType<?>> entity, int weight, int minCount, int maxCount, Biome... biomes) {
        for (Biome biome : biomes) {
            if (biome != null) {
                //noinspection ObjectAllocationInLoop
                biome.getSpawns(entity.get().getClassification()).add(new SpawnListEntry(entity.get(), weight, minCount, maxCount));
            }
        }
    }

//    private static EntityType.Builder<EntityBull> bull() {
//        return EntityType.Builder.create(EntityBull::new, CREATURE).size(0.9f, 1.4f);
//    }

//    private static EntityType.Builder<EntityShadowHound> shadowHound() {
//        return EntityType.Builder.<EntityShadowHound>create(EntityShadowHound::new, MONSTER).size(0.7f, 0.8f);
//    }

    /**
     * Register the world spawns.
     */
    public static void registerEntityWorldSpawns() {
        //        registerEntityWorldSpawn(COW, 10, 2, 3, EvolutionBiomes.FOREST.get());
        //        registerEntityWorldSpawn(BULL, 10, 1, 2, EvolutionBiomes.FOREST.get());
    }

    private static EntityType.Builder<EntitySit> sit() {
        return EntityType.Builder.<EntitySit>create(EntitySit::new, MISC).size(0.0f, 0.0f).setCustomClientFactory(EntitySit::new);
    }

    private static EntityType.Builder<EntitySpear> spear() {
        return EntityType.Builder.<EntitySpear>create(EntitySpear::new, MISC).size(0.1f, 0.1f)
                                                                             .setTrackingRange(5)
                                                                             .setUpdateInterval(20)
                                                                             .setCustomClientFactory(EntitySpear::new)
                                                                             .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityTorch> torch() {
        return EntityType.Builder.<EntityTorch>create(EntityTorch::new, MISC).size(0.2f, 0.2f)
                                                                             .setTrackingRange(5)
                                                                             .setUpdateInterval(20)
                                                                             .setCustomClientFactory(EntityTorch::new)
                                                                             .setShouldReceiveVelocityUpdates(true);
    }
}
