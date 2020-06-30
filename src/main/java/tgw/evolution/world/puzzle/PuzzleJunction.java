package tgw.evolution.world.puzzle;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class PuzzleJunction {

    private final int sourceX;
    private final int sourceGroundY;
    private final int sourceZ;
    private final int deltaY;
    private final PuzzlePattern.PlacementBehaviour destProjection;

    public PuzzleJunction(int sourceX, int sourceGroundY, int sourceZ, int deltaY, PuzzlePattern.PlacementBehaviour destProjection) {
        this.sourceX = sourceX;
        this.sourceGroundY = sourceGroundY;
        this.sourceZ = sourceZ;
        this.deltaY = deltaY;
        this.destProjection = destProjection;
    }

    public static <T> PuzzleJunction deserialize(Dynamic<T> p_214894_0_) {
        return new PuzzleJunction(p_214894_0_.get("source_x").asInt(0), p_214894_0_.get("source_ground_y").asInt(0), p_214894_0_.get("source_z").asInt(0), p_214894_0_.get("delta_y").asInt(0), PuzzlePattern.PlacementBehaviour.getBehaviour(p_214894_0_.get("dest_proj").asString("")));
    }

    public int getSourceX() {
        return this.sourceX;
    }

    public int getSourceGroundY() {
        return this.sourceGroundY;
    }

    public int getSourceZ() {
        return this.sourceZ;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("source_x"), ops.createInt(this.sourceX)).put(ops.createString("source_ground_y"), ops.createInt(this.sourceGroundY)).put(ops.createString("source_z"), ops.createInt(this.sourceZ)).put(ops.createString("delta_y"), ops.createInt(this.deltaY)).put(ops.createString("dest_proj"), ops.createString(this.destProjection.getName()));
        return new Dynamic<>(ops, ops.createMap(builder.build()));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other != null && this.getClass() == other.getClass()) {
            PuzzleJunction puzzleJunction = (PuzzleJunction) other;
            if (this.sourceX != puzzleJunction.sourceX) {
                return false;
            }
            if (this.sourceZ != puzzleJunction.sourceZ) {
                return false;
            }
            if (this.deltaY != puzzleJunction.deltaY) {
                return false;
            }
            return this.destProjection == puzzleJunction.destProjection;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int i = this.sourceX;
        i = 31 * i + this.sourceGroundY;
        i = 31 * i + this.sourceZ;
        i = 31 * i + this.deltaY;
        i = 31 * i + this.destProjection.hashCode();
        return i;
    }

    @Override
    public String toString() {
        return "PuzzleJunction{sourceX=" + this.sourceX + ", sourceGroundY=" + this.sourceGroundY + ", sourceZ=" + this.sourceZ + ", deltaY=" + this.deltaY + ", destProjection=" + this.destProjection + '}';
    }
}
