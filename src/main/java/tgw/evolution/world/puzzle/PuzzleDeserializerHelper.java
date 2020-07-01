package tgw.evolution.world.puzzle;

import net.minecraft.nbt.INBT;
import tgw.evolution.Evolution;
import tgw.evolution.util.NBTHelper;

public class PuzzleDeserializerHelper {

    public static PuzzlePiece deserialize(INBT nbt, String string, PuzzlePiece defaultEntry) {
        EnumPuzzleType type = EnumPuzzleType.byKey(NBTHelper.asString(nbt, string, ""));
        if (type == null) {
            Evolution.LOGGER.error("Unknown type {}, replacing with {}", nbt.getString(), defaultEntry);
            return defaultEntry;
        }
        switch (type) {
            case FEATURE:
                return new FeaturePuzzlePiece(nbt);
            case FORCED:
                return new ForcedPuzzlePiece(nbt);
            case LIST:
                return new ListPuzzlePiece(nbt);
            case SINGLE:
                return new SinglePuzzlePiece(nbt);
            case UNDERGROUND:
                return new UndergroundPuzzlePiece(nbt);
            default:
            case EMPTY:
                return EmptyPuzzlePiece.INSTANCE;
        }
    }
}
