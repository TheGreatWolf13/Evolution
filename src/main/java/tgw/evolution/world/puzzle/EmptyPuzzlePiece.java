package tgw.evolution.world.puzzle;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.util.NBTHelper;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EmptyPuzzlePiece extends PuzzlePiece {

    public static final EmptyPuzzlePiece INSTANCE = new EmptyPuzzlePiece();

    public EmptyPuzzlePiece() {
        super(PuzzlePattern.PlacementBehaviour.TERRAIN_MATCHING);
    }

    @Override
    public List<Template.BlockInfo> getPuzzleBlocks(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn, Random rand) {
        return Collections.emptyList();
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn) {
        return MutableBoundingBox.getNewBoundingBox();
    }

    @Override
    public boolean place(TemplateManager templateManagerIn, IWorld worldIn, BlockPos pos, Rotation rotationIn, MutableBoundingBox boundsIn, Random rand) {
        return true;
    }

    @Override
    protected INBT serialize0() {
        return NBTHelper.emptyMap();
    }

    @Override
    public String toString() {
        return "Empty";
    }

    @Override
    public EnumPuzzleType getType() {
        return EnumPuzzleType.EMPTY;
    }
}
