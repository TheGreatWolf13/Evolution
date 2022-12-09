package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.EntityType;
import tgw.evolution.Evolution;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.*;

public final class HitboxRegistry {

    private static final R2OMap<EntityType, HitboxRegistry> REGISTRY = new R2OOpenHashMap<>();
    private static final HitboxRegistry DUMMY;

    static {
        RList<HitboxType> list = new RArrayList<>();
        list.add(HitboxType.ALL);
        list.add(HitboxType.EQUIP);
        DUMMY = new HitboxRegistry(list);
    }

    private final B2RMap<HitboxType> deserializer;
    private final R2BMap<HitboxType> serializer;

    private HitboxRegistry(RList<HitboxType> list) {
        int size = list.size();
        this.deserializer = new B2ROpenHashMap<>();
        this.serializer = new R2BOpenHashMap<>();
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
        player.add(HitboxType.HEAD);
        player.add(HitboxType.CHEST);
        player.add(HitboxType.SHOULDER_RIGHT);
        player.add(HitboxType.SHOULDER_LEFT);
        player.add(HitboxType.ARM_RIGHT);
        player.add(HitboxType.ARM_LEFT);
        player.add(HitboxType.HAND_RIGHT);
        player.add(HitboxType.HAND_LEFT);
        player.add(HitboxType.LEG_RIGHT);
        player.add(HitboxType.LEG_LEFT);
        player.add(HitboxType.FOOT_RIGHT);
        player.add(HitboxType.FOOT_LEFT);
        player.register(EntityType.PLAYER);
        Builder creeper = new Builder();
        creeper.add(HitboxType.HEAD);
        creeper.add(HitboxType.CHEST);
        creeper.add(HitboxType.LEG_FRONT_RIGHT);
        creeper.add(HitboxType.LEG_FRONT_LEFT);
        creeper.add(HitboxType.LEG_HIND_RIGHT);
        creeper.add(HitboxType.LEG_HIND_LEFT);
        creeper.register(EntityType.CREEPER);
    }

    public static final class Builder {
        private final RList<HitboxType> list = new RArrayList<>();
        private final RSet<HitboxType> set = new ROpenHashSet<>();
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

        public void register(EntityType type) {
            if (REGISTRY.containsKey(type)) {
                Evolution.warn("HitboxRegistry for EntityType {} is already registered. This might be a bug, or an intentional replacing.", type);
            }
            REGISTRY.put(type, new HitboxRegistry(this.list));
        }
    }
}
