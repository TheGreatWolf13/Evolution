package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.Evolution;
import tgw.evolution.util.hitbox.HitboxRegistry;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.hitbox.Matrix4d;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.Vec3d;

public class ProjectileHitInformation implements IHitInfo {

    private final Vec3d directorVec = new Vec3d(Vec3d.NULL);
    private final Vec3d firstOrtho = new Vec3d(Vec3d.NULL);
    private final Vec3d secondOrtho = new Vec3d(Vec3d.NULL);
    private final Vec3d[] vertices = new Vec3d[8];
    private final Vec3d[] verticesInHBVS = new Vec3d[8];
    private final Vec3d[] verticesInHBVSPartial = new Vec3d[8];
    private long data;
    private @Nullable Vec3 end;
    private @Nullable HitboxEntity<?> hitboxes;
    private boolean prepared;
    private boolean preparedInHBVS;
    private @Nullable Vec3 start;
    private double victimX;
    private double victimY;
    private double victimZ;

    public ProjectileHitInformation() {
        for (int i = 0, l = this.vertices.length; i < l; i++) {
            //noinspection ObjectAllocationInLoop
            this.vertices[i] = new Vec3d(Vec3d.NULL);
        }
        for (int i = 0, l = this.verticesInHBVSPartial.length; i < l; i++) {
            //noinspection ObjectAllocationInLoop
            this.verticesInHBVSPartial[i] = new Vec3d(Vec3d.NULL);
        }
        for (int i = 0, l = this.verticesInHBVS.length; i < l; i++) {
            //noinspection ObjectAllocationInLoop
            this.verticesInHBVS[i] = new Vec3d(Vec3d.NULL);
        }
    }

    @Override
    public void addHitbox(Entity entity, HitboxType hitbox) {
        Evolution.info("Collided with {} on {}", entity, hitbox);
        this.data = HitboxRegistry.append(entity.getType(), hitbox, this.data);
    }

    public void clear() {
        this.data = 0;
    }

    @Override
    public boolean contains(Entity entity, HitboxType hitbox) {
        if (this.data == 0) {
            return false;
        }
        return HitboxRegistry.contains(entity.getType(), hitbox, this.data);
    }

    public long getHitboxSet() {
        return this.data;
    }

    @Override
    public Vec3d getOrMakeEdge(@Range(from = 0, to = 11) int edge, boolean start) {
        @Range(from = 0, to = 7) int index;
        if (start) {
            switch (edge) {
                case 0, 1, 2 -> index = 0;
                case 3, 4, 5 -> index = 3;
                case 6, 7, 8 -> index = 5;
                case 9, 10, 11 -> index = 6;
                default -> throw new IndexOutOfBoundsException("Edge is out of bounds: " + edge);
            }
        }
        else {
            switch (edge) {
                case 0, 3, 6 -> index = 1;
                case 1, 4, 9 -> index = 2;
                case 2, 7, 10 -> index = 4;
                case 5, 8, 11 -> index = 7;
                default -> throw new IndexOutOfBoundsException("Edge is out of bounds: " + edge);
            }
        }
        return this.getOrMakeVertex(index);
    }

    @Override
    public Vec3d getOrMakeEdgeInHBVS(@Range(from = 0, to = 11) int edge, boolean start, Matrix4d transform) {
        @Range(from = 0, to = 7) int index;
        if (start) {
            switch (edge) {
                case 0, 1, 2 -> index = 0;
                case 3, 4, 5 -> index = 3;
                case 6, 7, 8 -> index = 5;
                case 9, 10, 11 -> index = 6;
                default -> throw new IndexOutOfBoundsException("Edge is out of bounds: " + edge);
            }
        }
        else {
            switch (edge) {
                case 0, 3, 6 -> index = 1;
                case 1, 4, 9 -> index = 2;
                case 2, 7, 10 -> index = 4;
                case 5, 8, 11 -> index = 7;
                default -> throw new IndexOutOfBoundsException("Edge is out of bounds: " + edge);
            }
        }
        return this.getOrMakeVertexInHBVS(index, transform);
    }

    @Override
    public Vec3d getOrMakeVertex(@Range(from = 0, to = 7) int index) {
        if (!this.prepared) {
            throw new IllegalStateException("Should prepare first!");
        }
        Vec3d vertex = this.vertices[index];
        if (!vertex.isNull()) {
            return vertex;
        }
        assert this.start != null;
        assert this.end != null;
        if (index < 4) {
            vertex.set(this.start);
        }
        else {
            vertex.set(this.end);
        }
        Vec3d ortho = index % 2 == 0 ? this.firstOrtho : this.secondOrtho;
        if (index % 4 < 2) {
            return vertex.subMutable(ortho);
        }
        return vertex.addMutable(ortho);
    }

    @Override
    public Vec3d getOrMakeVertexInHBVS(@Range(from = 0, to = 7) int index, Matrix4d transform) {
        if (!this.prepared) {
            throw new IllegalStateException("Should prepare first!");
        }
        if (!this.preparedInHBVS) {
            throw new IllegalStateException("Should prepare in Hitbox Vector Space first!");
        }
        Vec3d vertexHBVS = this.verticesInHBVS[index];
        if (!vertexHBVS.isNull()) {
            return vertexHBVS;
        }
        Vec3d partialVertex = this.verticesInHBVSPartial[index];
        if (partialVertex.isNull()) {
            partialVertex.set(this.getOrMakeVertex(index)).subMutable(this.victimX, this.victimY, this.victimZ);
            assert this.hitboxes != null;
            this.hitboxes.untransform(partialVertex);
        }
        vertexHBVS.set(partialVertex);
        return transform.untransform(vertexHBVS);
    }

    public boolean isEmpty() {
        return this.data == 0;
    }

    public void prepare(Vec3 start, Vec3 end, double radius) {
        this.start = start;
        this.end = end;
        for (Vec3d vertex : this.vertices) {
            vertex.set(Vec3d.NULL);
        }
        this.directorVec.set(this.end).subMutable(this.start);
        if (this.directorVec.y != 0 || this.directorVec.z != 0) {
            this.firstOrtho.set(1, 0, 0);
        }
        else if (this.directorVec.x != 0) {
            this.firstOrtho.set(0, 1, 0);
        }
        else {
            this.firstOrtho.set(0, 0, 1);
        }
        this.firstOrtho.crossMutable(this.directorVec).normalizeMutable().scaleMutable(radius);
        this.secondOrtho.set(this.directorVec).crossMutable(this.firstOrtho).normalizeMutable().scaleMutable(radius);
        this.prepared = true;
    }

    @Override
    public void prepareInHBVS(HitboxEntity<?> hitboxes, double victimX, double victimY, double victimZ) {
        this.hitboxes = hitboxes;
        this.victimX = victimX;
        this.victimY = victimY;
        this.victimZ = victimZ;
        for (Vec3d vertex : this.verticesInHBVSPartial) {
            vertex.set(Vec3d.NULL);
        }
        for (Vec3d vertex : this.verticesInHBVS) {
            vertex.set(Vec3d.NULL);
        }
        this.preparedInHBVS = true;
    }

    public void release() {
        this.prepared = false;
        this.start = null;
        this.end = null;
    }

    @Override
    public void releaseInHBVS() {
        this.preparedInHBVS = false;
        this.hitboxes = null;
    }

    @Override
    public void softRelease() {
        for (Vec3d vertexInHBVS : this.verticesInHBVS) {
            vertexInHBVS.set(Vec3d.NULL);
        }
    }
}
