package tgw.evolution.world.puzzle.pieces;

import net.minecraft.nbt.INBT;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import tgw.evolution.world.puzzle.EnumPuzzleType;
import tgw.evolution.world.puzzle.PuzzlePattern;

import java.util.List;

public class UndergroundPuzzlePiece extends SinglePuzzlePiece {

    public UndergroundPuzzlePiece(String location, List<StructureProcessor> processors) {
        super(location, processors);

    }

    public UndergroundPuzzlePiece(String location, List<StructureProcessor> processors, PuzzlePattern.PlacementBehaviour placementBehaviour) {
        super(location, processors, placementBehaviour);
    }

    public UndergroundPuzzlePiece(String location) {
        super(location);
    }

    public UndergroundPuzzlePiece(INBT nbt) {
        super(nbt);
    }

    @Override
    public String toString() {
        return "UndergroundPuzzlePiece[" + this.location + "]";
    }

    @Override
    public EnumPuzzleType getType() {
        return EnumPuzzleType.UNDERGROUND;
    }
}
