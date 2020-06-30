package tgw.evolution.world.puzzle;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.util.NBTHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public abstract class PuzzlePiece {

    @Nullable
    private volatile PuzzlePattern.PlacementBehaviour projection;

    protected PuzzlePiece(PuzzlePattern.PlacementBehaviour projection) {
        this.projection = projection;
    }

    protected PuzzlePiece(INBT nbt) {
        this.projection = PuzzlePattern.PlacementBehaviour.getBehaviour(NBTHelper.asString(nbt, "projection", PuzzlePattern.PlacementBehaviour.RIGID.getName()));
    }

    public abstract List<Template.BlockInfo> getPuzzleBlocks(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn, Random rand);

    public abstract MutableBoundingBox getBoundingBox(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn);

    public abstract boolean place(TemplateManager templateManagerIn, IWorld worldIn, BlockPos pos, Rotation rotationIn, MutableBoundingBox boundsIn, Random rand);

    public void func_214846_a(IWorld worldIn, Template.BlockInfo blockInfo, BlockPos pos, Rotation rotationIn, Random rand, MutableBoundingBox boundingBox) {
    }

    public PuzzlePattern.PlacementBehaviour getPlacementBehaviour() {
        PuzzlePattern.PlacementBehaviour placementBehaviour = this.projection;
        if (placementBehaviour == null) {
            throw new IllegalStateException("Placement Behaviour cannot be null");
        }
        return placementBehaviour;
    }

    public PuzzlePiece setPlacementBehaviour(PuzzlePattern.PlacementBehaviour placementBehaviour) {
        this.projection = placementBehaviour;
        return this;
    }

    public abstract EnumPuzzleType getType();

    protected abstract INBT serialize0();

    public INBT serialize() {
        INBT pieceNBT = this.serialize0();
        INBT thisNBT = NBTHelper.mergeInto(pieceNBT, new StringNBT("element_type"), new StringNBT(this.getType().getKey()));
        return NBTHelper.mergeInto(thisNBT, new StringNBT("projection"), new StringNBT(this.projection.getName()));
    }

    public int groundLevelDelta() {
        return 1;
    }
}
