package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.Comparator;

public class MultipleEntityHitResult extends EntityHitResult {

    public final double endX;
    public final double endY;
    public final double endZ;
    public final double startX;
    public final double startY;
    public final double startZ;
    private final OList<DistanceHolder> hits = new OArrayList<>();
    private int index;

    public MultipleEntityHitResult(Entity entity, double startX, double startY, double startZ, double endX, double endY, double endZ) {
        super(entity);
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public void add(Entity entity, double dist) {
        this.hits.add(new DistanceHolder(dist, entity));
    }

    @Override
    public double distanceTo(Entity pEntity) {
        return this.hits.get(0).dist;
    }

    public void finish() {
        this.hits.sort(Comparator.comparingDouble(h -> h.dist));
    }

    @Override
    public Entity getEntity() {
        return this.hits.get(0).entity;
    }

    public @Nullable Entity popEntity() {
        if (this.index == this.hits.size()) {
            return null;
        }
        return this.hits.get(this.index++).entity;
    }

    public static record DistanceHolder(double dist, Entity entity) {

    }
}
