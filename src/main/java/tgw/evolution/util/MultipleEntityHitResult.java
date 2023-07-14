package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.Comparator;

public class MultipleEntityHitResult extends EntityHitResult {

    private final Vec3 end;
    private final OList<DistanceHolder> hits = new OArrayList<>();
    private final Vec3 start;
    private int index;

    public MultipleEntityHitResult(Entity entity, Vec3 start, Vec3 end) {
        super(entity);
        this.start = start;
        this.end = end;
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

    public Vec3 getEnd() {
        return this.end;
    }

    @Override
    public Entity getEntity() {
        return this.hits.get(0).entity;
    }

    public Vec3 getStart() {
        return this.start;
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
