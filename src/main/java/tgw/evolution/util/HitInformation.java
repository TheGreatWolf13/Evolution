package tgw.evolution.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import tgw.evolution.Evolution;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSHitInformation;
import tgw.evolution.network.PacketCSSpecialHit;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.collection.I2OMap;
import tgw.evolution.util.collection.I2OOpenHashMap;
import tgw.evolution.util.hitbox.HitboxType;

import java.util.EnumSet;
import java.util.Set;

public class HitInformation {

    private final I2OMap<Set<HitboxType>> data = new I2OOpenHashMap<>();

    public void addHitbox(Entity entity, HitboxType hitbox) {
        ClientEvents.getInstance().getRenderer().updateHitmarkers(false);
        Evolution.info("Collided with {} on {}", entity, hitbox);
        Set<HitboxType> set = this.data.get(entity.getId());
        if (set == null) {
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSHitInformation(entity));
            set = EnumSet.noneOf(HitboxType.class);
            this.data.put(entity.getId(), set);
        }
        set.add(hitbox);
    }

    public boolean areAllChecked(Entity entity) {
        Set<HitboxType> set = this.data.get(entity.getId());
        if (set == null) {
            return false;
        }
        if (!((IEntityPatch) entity).hasHitboxes()) {
            //If the entity does not have hitboxes, its only hitbox should be HitboxType.ALL
            return set.size() >= 1;
        }
        int numHitboxes = ((IEntityPatch) entity).getHitboxes().getBoxes().size();
        return set.size() >= numHitboxes;
    }

    public void clear() {
        this.data.clear();
    }

    public void clearMemory() {
        this.data.reset();
    }

    public boolean contains(Entity entity, HitboxType hitbox) {
        Set<HitboxType> set = this.data.get(entity.getId());
        if (set == null) {
            return false;
        }
        return set.contains(hitbox);
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public void sendHits(InteractionHand hand) {
        for (Int2ObjectMap.Entry<Set<HitboxType>> entry : this.data.int2ObjectEntrySet()) {
            //noinspection ObjectAllocationInLoop
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSpecialHit(entry.getIntKey(), hand, entry.getValue().toArray(HitboxType[]::new)));
        }
    }
}
