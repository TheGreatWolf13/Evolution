package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.EntityType;
import tgw.evolution.Evolution;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.*;
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;

public final class HitboxRegistry {

    private static final R2OMap<EntityType, HitboxRegistry> REGISTRY = new R2OHashMap<>();
    private static final HitboxRegistry DUMMY;

    static {
        OList<HitboxType> list = new OArrayList<>();
        list.add(HitboxType.ALL);
        list.add(HitboxType.EQUIP);
        DUMMY = new HitboxRegistry(list);
    }

    private final B2RMap<HitboxType> deserializer;
    private final R2BMap<HitboxType> serializer;

    private HitboxRegistry(OList<HitboxType> list) {
        int size = list.size();
        this.deserializer = new B2RHashMap<>();
        this.serializer = new R2BHashMap<>();
        this.serializer.defaultReturnValue((byte) -1);
        for (byte b = 0; b < size; b++) {
            HitboxType type = list.get(b);
            this.deserializer.put(b, type);
            this.serializer.put(type, b);
        }
        this.deserializer.trimCollection();
        this.serializer.trimCollection();
    }

    public static long append(EntityType entity, HitboxType hitbox, long set) {
        return set | 1L << getIndex(entity, hitbox);
    }

    public static boolean contains(EntityType entity, HitboxType hitbox, long set) {
        return (set & 1L << getIndex(entity, hitbox)) != 0;
    }

    public static HitboxType deserialize(EntityType type, int index) {
        HitboxRegistry registry = REGISTRY.get(type);
        if (registry == null) {
            Evolution.warn("EntityType {} doesn't have registered hitboxes!", type);
            registry = DUMMY;
        }
        final HitboxType hitboxType = registry.deserializer.get((byte) index);
        if (hitboxType == null) {
            throw new UnregisteredFeatureException("EntityType " + type + " doesn't have HitboxType with id " + index + " registered!");
        }
        return hitboxType;
    }

    private static int getIndex(EntityType entity, HitboxType hitbox) {
        HitboxRegistry hitboxRegistry = REGISTRY.get(entity);
        if (hitboxRegistry == null) {
            Evolution.warn("EntityType {} doesn't have registered hitboxes!", entity);
            hitboxRegistry = DUMMY;
        }
        byte index = hitboxRegistry.serializer.getByte(hitbox);
        if (index == -1) {
            throw new UnregisteredFeatureException("EntityType " + entity + " doesn't have HitboxType " + hitbox + " registered!");
        }
        return index;
    }

    public static void register() {
        Builder player = new Builder();
        player.add(HitboxType.HEAD)
              .add(HitboxType.CHEST)
              .add(HitboxType.SHOULDER_RIGHT)
              .add(HitboxType.SHOULDER_LEFT)
              .addArms()
              .addHands()
              .addLegs()
              .add(HitboxType.FOOT_RIGHT)
              .add(HitboxType.FOOT_LEFT)
              .register(EntityType.PLAYER);
        Builder creeper = new Builder();
        creeper.add(HitboxType.HEAD)
               .add(HitboxType.CHEST)
               .add(HitboxType.LEG_FRONT_RIGHT)
               .add(HitboxType.LEG_FRONT_LEFT)
               .add(HitboxType.LEG_HIND_RIGHT)
               .add(HitboxType.LEG_HIND_LEFT)
               .register(EntityType.CREEPER);
        Builder villager = new Builder();
        villager.add(HitboxType.HEAD)
                .add(HitboxType.NOSE)
                .add(HitboxType.CHEST)
                .addArms()
                .addHands()
                .addLegs()
                .register(EntityType.VILLAGER);
        Builder spider = new Builder();
        spider.add(HitboxType.HEAD)
              .add(HitboxType.CHEST)
              .add(HitboxType.LEG_FRONT_RIGHT)
              .add(HitboxType.LEG_FRONT_LEFT)
              .add(HitboxType.LEG_FRONT_MIDDLE_RIGHT)
              .add(HitboxType.LEG_FRONT_MIDDLE_LEFT)
              .add(HitboxType.LEG_HIND_MIDDLE_RIGHT)
              .add(HitboxType.LEG_HIND_MIDDLE_LEFT)
              .add(HitboxType.LEG_HIND_RIGHT)
              .add(HitboxType.LEG_HIND_LEFT)
              .register(EntityType.SPIDER);
        spider.register(EntityType.CAVE_SPIDER);
    }

    public static final class Builder {
        private final OList<HitboxType> list = new OArrayList<>();
        private final RSet<HitboxType> set = new RHashSet<>();
        private int counter;

        public Builder() {
            this.add(HitboxType.ALL);
            this.add(HitboxType.EQUIP);
        }

        public Builder add(HitboxType type) {
            if (this.counter >= 64) {
                throw new IllegalStateException("This HitboxRegistry Builder is full. The size limit is 64!");
            }
            if (this.set.contains(type)) {
                throw new IllegalStateException("This HitboxRegistry Builder already contains this HitboxType: " + type);
            }
            if (type == HitboxType.NONE) {
                throw new IllegalStateException("Cannot add HitboxType NONE to the registry, as it is not collidable!");
            }
            this.set.add(type);
            this.list.add(type);
            this.counter++;
            return this;
        }

        public Builder addArms() {
            return this.add(HitboxType.ARM_RIGHT).add(HitboxType.ARM_LEFT);
        }

        public Builder addHands() {
            return this.add(HitboxType.HAND_RIGHT).add(HitboxType.HAND_LEFT);
        }

        public Builder addLegs() {
            return this.add(HitboxType.LEG_RIGHT).add(HitboxType.LEG_LEFT);
        }

        public void register(EntityType type) {
            if (REGISTRY.containsKey(type)) {
                Evolution.warn("HitboxRegistry for EntityType {} is already registered. This might be a bug, or an intentional replacing.", type);
            }
            REGISTRY.put(type, new HitboxRegistry(this.list));
        }
    }
}
