//package tgw.evolution.world.puzzle;
//
//import net.minecraft.nbt.INBT;
//import tgw.evolution.Evolution;
//import tgw.evolution.util.NBTHelper;
//import tgw.evolution.world.puzzle.pieces.*;
//
//public final class PuzzleDeserializerHelper {
//
//    private PuzzleDeserializerHelper() {
//    }
//
//    public static PuzzlePiece deserialize(INBT nbt, String string, PuzzlePiece defaultEntry) {
//        EnumPuzzleType type = EnumPuzzleType.byId(NBTHelper.asByte(nbt, string, 0));
//        if (type == null) {
//            Evolution.LOGGER.error("Unknown type {}, replacing with {}", nbt.getString(), defaultEntry);
//            return defaultEntry;
//        }
//        switch (type) {
//            case CAVE:
//                return new CavePuzzlePiece(nbt);
//            case CONFIGURED:
//                return new ConfiguredPuzzlePiece(nbt);
//            case FEATURE:
//                return new FeaturePuzzlePiece(nbt);
//            case LIST:
//                return new ListPuzzlePiece(nbt);
//            case SINGLE:
//                return new SinglePuzzlePiece(nbt);
//            default:
//            case EMPTY:
//                return EmptyPuzzlePiece.INSTANCE;
//        }
//    }
//}
