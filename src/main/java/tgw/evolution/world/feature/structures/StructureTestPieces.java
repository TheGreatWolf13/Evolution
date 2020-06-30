package tgw.evolution.world.feature.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.Evolution;
import tgw.evolution.world.feature.EvolutionFeatures;
import tgw.evolution.world.puzzle.*;

import java.util.List;

public class StructureTestPieces {

    public static final ResourceLocation BASE_POOL = Evolution.location("test/top_room");
    public static final ResourceLocation SPAWN_POOL = Evolution.location("test/spawn");
    public static final ResourceLocation CORRIDORS_POOL = Evolution.location("test/corridors");
    public static final ResourceLocation CORRIDOR_END = Evolution.location("test/corridors_end");

    static {
        PuzzleManager.REGISTRY.register(new PuzzlePattern(BASE_POOL, Evolution.location("empty"), ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:test/top_room"), 1)), PuzzlePattern.PlacementBehaviour.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(SPAWN_POOL, Evolution.location("empty"), ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:test/spawn_room"), 1)), PuzzlePattern.PlacementBehaviour.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(CORRIDORS_POOL, CORRIDOR_END, ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:test/straight"), 100), Pair.of(new SinglePuzzlePiece("evolution:test/door_locked"), 10), Pair.of(new SinglePuzzlePiece("evolution:test/l_room"), 20), Pair.of(new SinglePuzzlePiece("evolution:test/six_room"), 20), Pair.of(new SinglePuzzlePiece("evolution:test/hole_down_lock"), 10), Pair.of(new SinglePuzzlePiece("evolution:test/hole_up_lock"), 10)), PuzzlePattern.PlacementBehaviour.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(CORRIDOR_END, Evolution.location("empty"), ImmutableList.of(Pair.of(new ForcedPuzzlePiece("evolution:test/door_locked", ForcedPuzzlePiece.ForceType.SOFT), 1), Pair.of(new ForcedPuzzlePiece("evolution:test/hole_down_lock", ForcedPuzzlePiece.ForceType.HARD), 1), Pair.of(new ForcedPuzzlePiece("evolution:test/hole_up_lock", ForcedPuzzlePiece.ForceType.HARD), 1)), PuzzlePattern.PlacementBehaviour.RIGID));
    }

    public static void start(ChunkGenerator<?> generator, TemplateManager manager, BlockPos pos, List<StructurePiece> pieces, SharedSeedRandom random) {
        int size = 3;
        PuzzleManager.startGeneration(BASE_POOL, size, Piece::new, generator, manager, pos, pieces, random);
    }

    public static class Piece extends PuzzleStructurePiece {

        public Piece(TemplateManager manager, PuzzlePiece puzzlePiece, BlockPos pos, int groundLevelDelta, Rotation rotation, MutableBoundingBox boundingBox) {
            super(EvolutionFeatures.PIECE_TEST, manager, puzzlePiece, pos, groundLevelDelta, rotation, boundingBox);
        }

        public Piece(TemplateManager manager, CompoundNBT nbt) {
            super(manager, nbt, EvolutionFeatures.PIECE_TEST);
        }
    }
}
