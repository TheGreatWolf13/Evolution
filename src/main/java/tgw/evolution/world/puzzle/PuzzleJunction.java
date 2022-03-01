//package tgw.evolution.world.puzzle;
//
//import com.google.common.collect.ImmutableMap;
//import com.mojang.serialization.Dynamic;
//import com.mojang.serialization.DynamicOps;
//import tgw.evolution.world.puzzle.pieces.config.PlacementType;
//
//public class PuzzleJunction {
//
//    private final int deltaY;
//    private final PlacementType destProjection;
//    private final int sourceGroundY;
//    private final int sourceX;
//    private final int sourceZ;
//
//    public PuzzleJunction(int sourceX, int sourceGroundY, int sourceZ, int deltaY, PlacementType destProjection) {
//        this.sourceX = sourceX;
//        this.sourceGroundY = sourceGroundY;
//        this.sourceZ = sourceZ;
//        this.deltaY = deltaY;
//        this.destProjection = destProjection;
//    }
//
//    public static <T> PuzzleJunction func_236819_a_(Dynamic<T> dynamic) {
//        return new PuzzleJunction(dynamic.get("source_x").asInt(0),
//                                  dynamic.get("source_ground_y").asInt(0),
//                                  dynamic.get("source_z").asInt(0),
//                                  dynamic.get("delta_y").asInt(0),
//                                  PlacementType.byId(dynamic.get("dest_proj").asByte((byte) 0)));
//    }
//
//    @Override
//    public boolean equals(Object other) {
//        if (this == other) {
//            return true;
//        }
//        if (other != null && this.getClass() == other.getClass()) {
//            PuzzleJunction puzzleJunction = (PuzzleJunction) other;
//            if (this.sourceX != puzzleJunction.sourceX) {
//                return false;
//            }
//            if (this.sourceZ != puzzleJunction.sourceZ) {
//                return false;
//            }
//            if (this.deltaY != puzzleJunction.deltaY) {
//                return false;
//            }
//            return this.destProjection == puzzleJunction.destProjection;
//        }
//        return false;
//    }
//
//    public int getSourceGroundY() {
//        return this.sourceGroundY;
//    }
//
//    public int getSourceX() {
//        return this.sourceX;
//    }
//
//    public int getSourceZ() {
//        return this.sourceZ;
//    }
//
//    @Override
//    public int hashCode() {
//        int i = this.sourceX;
//        i = 31 * i + this.sourceGroundY;
//        i = 31 * i + this.sourceZ;
//        i = 31 * i + this.deltaY;
//        i = 31 * i + this.destProjection.hashCode();
//        return i;
//    }
//
//    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
//        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
//        builder.put(ops.createString("source_x"), ops.createInt(this.sourceX))
//               .put(ops.createString("source_ground_y"), ops.createInt(this.sourceGroundY))
//               .put(ops.createString("source_z"), ops.createInt(this.sourceZ))
//               .put(ops.createString("delta_y"), ops.createInt(this.deltaY))
//               .put(ops.createString("dest_proj"), ops.createByte(this.destProjection.getId()));
//        return new Dynamic<>(ops, ops.createMap(builder.build()));
//    }
//
//    @Override
//    public String toString() {
//        return "PuzzleJunction{sourceX=" +
//               this.sourceX +
//               ", sourceGroundY=" +
//               this.sourceGroundY +
//               ", sourceZ=" +
//               this.sourceZ +
//               ", deltaY=" +
//               this.deltaY +
//               ", destProjection=" +
//               this.destProjection +
//               '}';
//    }
//}
