//package tgw.evolution.world.puzzle;
//
//import net.minecraft.nbt.ByteNBT;
//import net.minecraft.nbt.INBT;
//import net.minecraft.nbt.StringNBT;
//import net.minecraft.util.Rotation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.gen.feature.template.Template;
//import net.minecraft.world.gen.feature.template.TemplateManager;
//import tgw.evolution.util.constants.NBTHelper;
//import tgw.evolution.world.puzzle.pieces.config.PlacementType;
//
//
//import java.util.List;
//import java.util.Random;
//
//public abstract class PuzzlePiece {
//
//    @Nonnull
//    private volatile PlacementType projection;
//
//    protected PuzzlePiece(@Nonnull PlacementType projection) {
//        this.projection = projection;
//    }
//
//    protected PuzzlePiece(INBT nbt) {
//        this.projection = PlacementType.byId(NBTHelper.asByte(nbt, "Proj", PlacementType.RIGID.getId()));
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof PuzzlePiece)) {
//            return false;
//        }
//        PuzzlePiece that = (PuzzlePiece) o;
//        return this.projection == that.projection;
//    }
//
//    public abstract MutableBoundingBox getBoundingBox(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn);
//
//    public PlacementType getPlacementBehaviour() {
//        PlacementType placementBehaviour = this.projection;
//        if (placementBehaviour == null) {
//            throw new IllegalStateException("Placement Behaviour cannot be null");
//        }
//        return placementBehaviour;
//    }
//
//    public PuzzlePiece setPlacementBehaviour(PlacementType placementBehaviour) {
//        this.projection = placementBehaviour;
//        return this;
//    }
//
//    public abstract List<Template.BlockInfo> getPuzzleBlocks(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn, Random rand);
//
//    public abstract EnumPuzzleType getType();
//
//    public int groundLevelDelta() {
//        return 1;
//    }
//
//    @Override
//    public int hashCode() {
//        return 0;
//    }
//
//    public abstract boolean place(TemplateManager templateManagerIn,
//                                  IWorld worldIn,
//                                  BlockPos pos,
//                                  Rotation rotationIn,
//                                  MutableBoundingBox boundsIn,
//                                  Random rand);
//
//    public final INBT serialize() {
//        INBT pieceNBT = this.serialize0();
//        INBT thisNBT = NBTHelper.mergeInto(pieceNBT, StringNBT.valueOf("PieceType"), ByteNBT.valueOf(this.getType().getId()));
//        return NBTHelper.mergeInto(thisNBT, StringNBT.valueOf("Proj"), ByteNBT.valueOf(this.projection.getId()));
//    }
//
//    protected abstract INBT serialize0();
//}
