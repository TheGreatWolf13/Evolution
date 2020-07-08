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
import tgw.evolution.world.puzzle.pieces.SinglePuzzlePiece;
import tgw.evolution.world.puzzle.pieces.config.PlacementType;

import java.util.ArrayList;
import java.util.List;

public class StructureCavePieces {

    public static final ResourceLocation GEN_SUP = Evolution.location("gen_sup");
    public static final ResourceLocation SUP = Evolution.location("sup");
    public static final ResourceLocation GEN_UND = Evolution.location("gen_und");
    public static final ResourceLocation CAV = Evolution.location("cav");
    public static final ResourceLocation TERM = Evolution.location("term");
    private static final List<Pair<PuzzlePiece, Integer>> CAV_LIST = new ArrayList<>();
    private static final List<Pair<PuzzlePiece, Integer>> TERM_LIST = new ArrayList<>();

    static {
        add(CAV_LIST, "cav_claus1", 10); //DEGRADED
        add(CAV_LIST, "cav_claus2", 10);
        add(CAV_LIST, "cav_corredor1", 10);
        add(CAV_LIST, "cav_greatpit1", 10);
        add(CAV_LIST, "cav_lago1", 10);
        add(CAV_LIST, "cav_lava1", 10);
        add(CAV_LIST, "cav_queda1", 10);
        add(CAV_LIST, "cav_sala1", 10);
        add(CAV_LIST, "cav_tribal1", 10);
        add(CAV_LIST, "cav_tribal2", 10);
        add(CAV_LIST, "cav_water1", 10);
        add(CAV_LIST, "cav_water2", 10);
        add(CAV_LIST, "cav_waterfall1", 10);
        add(TERM_LIST, "cav_ent_normal1", 10);
        add(TERM_LIST, "cav_ent_tribal1", 10);
        add(TERM_LIST, "cav_hold_normal1", 10);
        add(TERM_LIST, "cav_holu_normal1", 10);
        for (Pair<PuzzlePiece, Integer> piece : TERM_LIST) {
            add(CAV_LIST, piece.getFirst(), 3);
        }
        PuzzleManager.REGISTRY.register(new PuzzlePattern(GEN_SUP, new ResourceLocation("empty"), ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:gen_sup"), 1)), PlacementType.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(GEN_UND, new ResourceLocation("empty"), ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:gen_und"), 1)), PlacementType.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(SUP, new ResourceLocation("empty"), ImmutableList.of(Pair.of(new SinglePuzzlePiece("evolution:cav_stair1"), 1)), PlacementType.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(CAV, TERM, CAV_LIST, PlacementType.RIGID));
        PuzzleManager.REGISTRY.register(new PuzzlePattern(TERM, new ResourceLocation("empty"), TERM_LIST, PlacementType.RIGID));
    }

    private static void add(List<Pair<PuzzlePiece, Integer>> list, String name, int weight) {
        list.add(Pair.of(new SinglePuzzlePiece("evolution:" + name), weight));
    }

    private static void add(List<Pair<PuzzlePiece, Integer>> list, PuzzlePiece piece, int weight) {
        list.add(Pair.of(piece, weight));
    }

    public static void start(ChunkGenerator<?> generator, TemplateManager manager, BlockPos pos, List<StructurePiece> pieces, SharedSeedRandom random, IConfigStruct config) {
        int size = 7;
        PuzzleManager.startGeneration(GEN_SUP, size, StructureCavePieces.Piece::new, generator, manager, pos, pieces, random, config); //DEGRADED
    }

    public static class Piece extends StructurePuzzlePiece {

        public Piece(TemplateManager templateManagerIn, PuzzlePiece jigsawPieceIn, BlockPos posIn, int groundLevelDelta, Rotation rotationIn, MutableBoundingBox boundsIn) {
            super(EvolutionFeatures.PIECE_CAVE, templateManagerIn, jigsawPieceIn, posIn, groundLevelDelta, rotationIn, boundsIn);
        }

        public Piece(TemplateManager manager, CompoundNBT nbt) {
            super(manager, nbt, EvolutionFeatures.PIECE_CAVE);
        }
    }
}
