package tgw.evolution.world.puzzle;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.world.puzzle.pieces.config.PlacementType;

import java.util.List;
import java.util.Random;

public abstract class PuzzlePiece {

    private volatile PlacementType projection;

    protected PuzzlePiece(PlacementType projection) {
        this.projection = projection;
    }

    protected PuzzlePiece(INBT nbt) {
        this.projection = PlacementType.byId(NBTHelper.asByte(nbt, "Proj", PlacementType.RIGID.getId()));
    }

    public abstract List<Template.BlockInfo> getPuzzleBlocks(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn, Random rand);

    public abstract MutableBoundingBox getBoundingBox(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn);

    public abstract boolean place(TemplateManager templateManagerIn, IWorld worldIn, BlockPos pos, Rotation rotationIn, MutableBoundingBox boundsIn, Random rand);

    public void func_214846_a(IWorld worldIn, Template.BlockInfo blockInfo, BlockPos pos, Rotation rotationIn, Random rand, MutableBoundingBox boundingBox) {
    }

    public PlacementType getPlacementBehaviour() {
        PlacementType placementBehaviour = this.projection;
        if (placementBehaviour == null) {
            throw new IllegalStateException("Placement Behaviour cannot be null");
        }
        return placementBehaviour;
    }

    public PuzzlePiece setPlacementBehaviour(PlacementType placementBehaviour) {
        this.projection = placementBehaviour;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PuzzlePiece)) {
            return false;
        }
        PuzzlePiece that = (PuzzlePiece) o;
        return this.projection == that.projection;
    }

    public abstract EnumPuzzleType getType();

    protected abstract INBT serialize0();

    public final INBT serialize() {
        INBT pieceNBT = this.serialize0();
        INBT thisNBT = NBTHelper.mergeInto(pieceNBT, new StringNBT("PieceType"), new ByteNBT(this.getType().getId()));
        return NBTHelper.mergeInto(thisNBT, new StringNBT("Proj"), new ByteNBT(this.projection.getId()));
    }

    public int groundLevelDelta() {
        return 1;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
