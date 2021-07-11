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
import tgw.evolution.world.feature.structures.config.IConfigStruct;
import tgw.evolution.world.puzzle.PuzzleManager;
import tgw.evolution.world.puzzle.PuzzlePattern;
import tgw.evolution.world.puzzle.PuzzlePiece;
import tgw.evolution.world.puzzle.StructurePuzzlePiece;
import tgw.evolution.world.puzzle.pieces.ConfiguredPuzzlePiece;
import tgw.evolution.world.puzzle.pieces.SinglePuzzlePiece;
import tgw.evolution.world.puzzle.pieces.config.ConfigPuzzle;
import tgw.evolution.world.puzzle.pieces.config.ForceType;
import tgw.evolution.world.puzzle.pieces.config.PlacementType;

import java.util.List;

public final class StructureTestPieces {

    public static final ResourceLocation BASE_POOL = Evolution.getResource("test/top_room");
    public static final ResourceLocation SPAWN_POOL = Evolution.getResource("test/spawn");
    public static final ResourceLocation CORRIDORS_POOL = Evolution.getResource("test/corridors");
    public static final ResourceLocation CORRIDOR_END = Evolution.getResource("test/corridors_end");
    public static final ConfigPuzzle<?> UNDERGROUND = new ConfigPuzzle<>().underground();
    public static final ConfigPuzzle<?> HARD = new ConfigPuzzle<>().forceType(ForceType.HARD);
    public static final ConfigPuzzle<?> SOFT = new ConfigPuzzle<>().forceType(ForceType.SOFT);
    public static final ConfigPuzzle<?> HEIGHT = new ConfigPuzzle<>().underground().desiredHeight(40).maxDeviation(10);

    static {
        PuzzleManager.REGISTRY.register(new PuzzlePattern(BASE_POOL,
                                                          Evolution.getResource("empty"),
                                                          ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:test/top_room"), 1)),
                                                          PlacementType.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(SPAWN_POOL,
                                                          Evolution.getResource("empty"),
                                                          ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:test/spawn_room"), 1)),
                                                          PlacementType.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(CORRIDORS_POOL,
                                                          CORRIDOR_END,
                                                          ImmutableList.of(Pair.of(new ConfiguredPuzzlePiece("evolution:test/straight", UNDERGROUND),
                                                                                   80),
                                                                           Pair.of(new ConfiguredPuzzlePiece("evolution:test/door_locked",
                                                                                                             UNDERGROUND), 10),
                                                                           Pair.of(new ConfiguredPuzzlePiece("evolution:test/l_room", HEIGHT), 30),
                                                                           Pair.of(new ConfiguredPuzzlePiece("evolution:test/six_room", UNDERGROUND),
                                                                                   20),
                                                                           Pair.of(new ConfiguredPuzzlePiece("evolution:test/hole_down_lock",
                                                                                                             UNDERGROUND), 10),
                                                                           Pair.of(new ConfiguredPuzzlePiece("evolution:test/hole_up_lock",
                                                                                                             UNDERGROUND), 10)),
                                                          PlacementType.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(CORRIDOR_END,
                                                          Evolution.getResource("empty"),
                                                          ImmutableList.of(Pair.of(new ConfiguredPuzzlePiece("evolution:test/door_locked", SOFT), 1),
                                                                           Pair.of(new ConfiguredPuzzlePiece("evolution:test/hole_down_lock", HARD),
                                                                                   1),
                                                                           Pair.of(new ConfiguredPuzzlePiece("evolution:test/hole_up_lock", HARD),
                                                                                   1)),
                                                          PlacementType.RIGID));
    }

    private StructureTestPieces() {
    }

    public static void start(ChunkGenerator<?> generator,
                             TemplateManager manager,
                             BlockPos pos,
                             List<StructurePiece> pieces,
                             SharedSeedRandom random,
                             IConfigStruct config) {
        int size = 10;
        PuzzleManager.startGeneration(BASE_POOL, size, Piece::new, generator, manager, pos, pieces, random, config);
    }

    public static class Piece extends StructurePuzzlePiece {

        public Piece(TemplateManager manager,
                     PuzzlePiece puzzlePiece,
                     BlockPos pos,
                     int groundLevelDelta,
                     Rotation rotation,
                     MutableBoundingBox boundingBox) {
            super(EvolutionFeatures.PIECE_TEST, manager, puzzlePiece, pos, groundLevelDelta, rotation, boundingBox);
        }

        public Piece(TemplateManager manager, CompoundNBT nbt) {
            super(manager, nbt, EvolutionFeatures.PIECE_TEST);
        }
    }
}
