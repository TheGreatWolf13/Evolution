package tgw.evolution.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.entities.EntityCow;
import tgw.evolution.entities.misc.EntityFallingPeat;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.entities.misc.EntitySittable;
import tgw.evolution.entities.projectiles.EntityHook;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.entities.projectiles.EntityTorch;
import tgw.evolution.items.ItemSpawnEgg;
import tgw.evolution.util.PlayerHelper;

import java.util.function.Supplier;

import static net.minecraft.world.entity.MobCategory.CREATURE;
import static net.minecraft.world.entity.MobCategory.MISC;

public final class EvolutionEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Evolution.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Evolution.MODID);

    public static final RegistryObject<EntityType<EntityCow>> COW;
    public static final RegistryObject<Item> SPAWN_EGG_COW;
    public static final RegistryObject<EntityType<EntityFallingPeat>> FALLING_PEAT;
    public static final RegistryObject<EntityType<EntityFallingWeight>> FALLING_WEIGHT;
    public static final RegistryObject<EntityType<EntityHook>> HOOK;
    public static final RegistryObject<EntityType<EntityPlayerCorpse>> PLAYER_CORPSE;
    public static final RegistryObject<EntityType<EntitySpear>> SPEAR;
    public static final RegistryObject<EntityType<EntitySittable>> SIT;
    public static final RegistryObject<EntityType<EntityTorch>> TORCH;

    static {
        COW = register("cow", () -> EntityType.Builder.of(EntityCow::new, CREATURE).sized(0.9f, 1.4f));
        SPAWN_EGG_COW = ITEMS.register("spawn_egg_cow", () -> genEgg(COW));
        FALLING_PEAT = register("falling_peat", () -> EntityType.Builder.<EntityFallingPeat>of(EntityFallingPeat::new, MISC)
                                                                        .sized(1.0F, 1.0F)
                                                                        .setTrackingRange(10)
                                                                        .setUpdateInterval(20)
                                                                        .setCustomClientFactory(EntityFallingPeat::new)
                                                                        .setShouldReceiveVelocityUpdates(true));
        FALLING_WEIGHT = register("falling_weight", () -> EntityType.Builder.<EntityFallingWeight>of(EntityFallingWeight::new, MISC)
                                                                            .sized(1.0F, 1.0F)
                                                                            .setTrackingRange(10)
                                                                            .setUpdateInterval(20)
                                                                            .setCustomClientFactory(EntityFallingWeight::new)
                                                                            .setShouldReceiveVelocityUpdates(true));
        HOOK = register("hook", () -> EntityType.Builder.<EntityHook>of(EntityHook::new, MISC)
                                                        .sized(0.5f, 0.5f)
                                                        .setTrackingRange(5)
                                                        .setUpdateInterval(20)
                                                        .setCustomClientFactory(EntityHook::new)
                                                        .setShouldReceiveVelocityUpdates(true));
        PLAYER_CORPSE = register("player_corpse", () -> EntityType.Builder.<EntityPlayerCorpse>of(EntityPlayerCorpse::new, MISC)
                                                                          .sized(0.9F, 0.4F)
                                                                          .setTrackingRange(10)
                                                                          .setUpdateInterval(20)
                                                                          .setCustomClientFactory(EntityPlayerCorpse::new)
                                                                          .setShouldReceiveVelocityUpdates(true));
        SPEAR = register("spear", () -> EntityType.Builder.<EntitySpear>of(EntitySpear::new, MISC)
                                                          .sized(0.1f, 0.1f)
                                                          .setTrackingRange(5)
                                                          .setUpdateInterval(20)
                                                          .setCustomClientFactory(EntitySpear::new)
                                                          .setShouldReceiveVelocityUpdates(true));
        SIT = register("sit", () -> EntityType.Builder.<EntitySittable>of(EntitySittable::new, MISC)
                                                      .sized(0.0f, 0.0f)
                                                      .setCustomClientFactory(EntitySittable::new));
        TORCH = register("torch", () -> EntityType.Builder.<EntityTorch>of(EntityTorch::new, MISC)
                                                          .sized(0.2f, 0.2f)
                                                          .setTrackingRange(5)
                                                          .setUpdateInterval(20)
                                                          .setCustomClientFactory(EntityTorch::new)
                                                          .setShouldReceiveVelocityUpdates(true));
    }

    private EvolutionEntities() {
    }

    /**
     * Returns a new Spawn Egg Item.
     */
    public static <E extends Entity> Item genEgg(RegistryObject<EntityType<E>> type) {
        return new ItemSpawnEgg<>(type);
    }

    public static void modifyEntityAttribute(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, Attributes.MAX_HEALTH, PlayerHelper.MAX_HEALTH);
        event.add(EntityType.PLAYER, Attributes.ATTACK_DAMAGE, PlayerHelper.ATTACK_DAMAGE);
        event.add(EntityType.PLAYER, Attributes.ATTACK_SPEED, PlayerHelper.ATTACK_SPEED);
        event.add(EntityType.PLAYER, ForgeMod.REACH_DISTANCE.get(), PlayerHelper.REACH_DISTANCE);
        event.add(EntityType.PLAYER, Attributes.MOVEMENT_SPEED, PlayerHelper.WALK_FORCE);
        event.add(EntityType.PLAYER, EvolutionAttributes.FRICTION.get());
        event.add(EntityType.PLAYER, EvolutionAttributes.COLD_RESISTANCE.get());
        event.add(EntityType.PLAYER, EvolutionAttributes.HEAT_RESISTANCE.get());
        for (EntityType type : ForgeRegistries.ENTITIES.getValues()) {
            event.add(type, EvolutionAttributes.MASS.get());
        }
    }

    private static <E extends Entity> RegistryObject<EntityType<E>> register(String name, Supplier<EntityType.Builder<E>> builder) {
        return ENTITIES.register(name, () -> builder.get().build(name));
    }

    public static void register() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void registerEntityAttribute(EntityAttributeCreationEvent event) {
        event.put(COW.get(), PathfinderMob.createMobAttributes().build());
    }

    /**
     * Adds the entity to the spawn list.
     */
    public static void registerEntityWorldSpawn(RegistryObject<EntityType<?>> entity, int weight, int minCount, int maxCount, Biome... biomes) {
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
