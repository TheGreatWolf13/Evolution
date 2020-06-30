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
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.Evolution;
import tgw.evolution.world.feature.EvolutionFeatures;

import java.util.ArrayList;
import java.util.List;

public class StructureCavePieces {

    public static final ResourceLocation GEN_SUP = Evolution.location("gen_sup");
    public static final ResourceLocation SUP = Evolution.location("sup");
    public static final ResourceLocation CAV = Evolution.location("cav");
    public static final ResourceLocation TERM = Evolution.location("term");
    private static final List<Pair<JigsawPiece, Integer>> CAV_LIST = new ArrayList<>();
    private static final List<Pair<JigsawPiece, Integer>> TERM_LIST = new ArrayList<>();

    static {
        add(CAV_LIST, "cav_claus1", 10);
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
        add(TERM_LIST, "cav_aqt_normal1", 10);
        add(TERM_LIST, "cav_ent_normal1", 10);
        add(TERM_LIST, "cav_ent_tribal1", 10);
        add(TERM_LIST, "cav_hold_normal1", 10);
        add(TERM_LIST, "cav_holu_normal1", 10);
        for (Pair<JigsawPiece, Integer> piece : TERM_LIST) {
            add(CAV_LIST, piece.getFirst(), 3);
        }
        JigsawManager.REGISTRY.register(new JigsawPattern(GEN_SUP, new ResourceLocation("empty"), ImmutableList.of(Pair.of(new SingleJigsawPiece("evolution:gen_sup"), 1)), JigsawPattern.PlacementBehaviour.RIGID));
        JigsawManager.REGISTRY.register(new JigsawPattern(SUP, new ResourceLocation("empty"), ImmutableList.of(Pair.of(new SingleJigsawPiece("evolution:sup_normal1"), 1), Pair.of(new SingleJigsawPiece("evolution:sup_tribal1"), 1)), JigsawPattern.PlacementBehaviour.RIGID));
        JigsawManager.REGISTRY.register(new JigsawPattern(CAV, TERM, CAV_LIST, JigsawPattern.PlacementBehaviour.RIGID));
        JigsawManager.REGISTRY.register(new JigsawPattern(TERM, new ResourceLocation("empty"), TERM_LIST, JigsawPattern.PlacementBehaviour.RIGID));
    }

    private static void add(List<Pair<JigsawPiece, Integer>> list, String name, int weight) {
        list.add(Pair.of(new SingleJigsawPiece("evolution:" + name), weight));
    }

    private static void add(List<Pair<JigsawPiece, Integer>> list, JigsawPiece piece, int weight) {
        list.add(Pair.of(piece, weight));
    }

    public static void start(ChunkGenerator<?> generator, TemplateManager manager, BlockPos pos, List<StructurePiece> pieces, SharedSeedRandom random) {
        int size = 5;
        JigsawManager.func_214889_a(GEN_SUP, size, StructureCavePieces.Piece::new, generator, manager, pos, pieces, random);
    }

    public static class Piece extends AbstractVillagePiece {

        public Piece(TemplateManager templateManagerIn, JigsawPiece jigsawPieceIn, BlockPos posIn, int groundLevelDelta, Rotation rotationIn, MutableBoundingBox boundsIn) {
            super(EvolutionFeatures.PIECE_CAVE, templateManagerIn, jigsawPieceIn, posIn, groundLevelDelta, rotationIn, boundsIn);
        }

        public Piece(TemplateManager manager, CompoundNBT nbt) {
            super(manager, nbt, EvolutionFeatures.PIECE_CAVE);
        }
    }
}
