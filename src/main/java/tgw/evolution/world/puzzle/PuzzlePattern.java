package tgw.evolution.world.puzzle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.template.GravityStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class PuzzlePattern {

    public static final PuzzlePattern EMPTY = new PuzzlePattern(Evolution.location("empty"), Evolution.location("empty"), ImmutableList.of(), PuzzlePattern.PlacementBehaviour.RIGID);
    public static final PuzzlePattern INVALID = new PuzzlePattern(Evolution.location("invalid"), Evolution.location("invalid"), ImmutableList.of(), PuzzlePattern.PlacementBehaviour.RIGID);
    private final ResourceLocation pool;
    private final List<PuzzlePiece> puzzlePieces;
    private final ResourceLocation fallbackPool;
    private int maxHeight = Integer.MIN_VALUE;

    public PuzzlePattern(ResourceLocation pool, ResourceLocation fallbackPool, List<Pair<PuzzlePiece, Integer>> entries, PuzzlePattern.PlacementBehaviour placementBehaviour) {
        this.pool = pool;
        this.puzzlePieces = Lists.newArrayList();
        for (Pair<PuzzlePiece, Integer> pair : entries) {
            for (int placingInList = 0; placingInList < pair.getSecond(); placingInList++) {
                this.puzzlePieces.add(pair.getFirst().setPlacementBehaviour(placementBehaviour));
            }
        }
        this.fallbackPool = fallbackPool;
    }

    public int getMaxHeight(TemplateManager manager) {
        if (this.maxHeight == Integer.MIN_VALUE) {
            this.maxHeight = this.puzzlePieces.stream().mapToInt(puzzlePiece -> puzzlePiece.getBoundingBox(manager, BlockPos.ZERO, Rotation.NONE).getYSize()).max().orElse(0);
        }
        return this.maxHeight;
    }

    public ResourceLocation getFallbackPool() {
        return this.fallbackPool;
    }

    public PuzzlePiece getRandomPiece(Random rand) {
        return this.puzzlePieces.get(rand.nextInt(this.puzzlePieces.size()));
    }

    public List<PuzzlePiece> getShuffledPieces(Random rand) {
        return ImmutableList.copyOf(ObjectArrays.shuffle(this.puzzlePieces.toArray(new PuzzlePiece[0]), rand));
    }

    public ResourceLocation getPool() {
        return this.pool;
    }

    public int getNumberOfPieces() {
        return this.puzzlePieces.size();
    }

    public enum PlacementBehaviour implements net.minecraftforge.common.IExtensibleEnum {
        TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityStructureProcessor(Heightmap.Type.WORLD_SURFACE_WG, -1))),
        RIGID("rigid", ImmutableList.of());

        private static final Map<String, PuzzlePattern.PlacementBehaviour> BEHAVIOURS = Arrays.stream(values()).collect(Collectors.toMap(PuzzlePattern.PlacementBehaviour::getName, p_214935_0_ -> p_214935_0_));
        private final String name;
        private final ImmutableList<StructureProcessor> structureProcessors;

        PlacementBehaviour(String nameIn, ImmutableList<StructureProcessor> processors) {
            this.name = nameIn;
            this.structureProcessors = processors;
        }

        @SuppressWarnings("unused")
        public static PlacementBehaviour create(String enumName, String name, ImmutableList<StructureProcessor> processors) {
            throw new IllegalStateException("Enum not extended");
        }

        public static PuzzlePattern.PlacementBehaviour getBehaviour(String nameIn) {
            return BEHAVIOURS.get(nameIn);
        }

        public String getName() {
            return this.name;
        }

        public ImmutableList<StructureProcessor> getStructureProcessors() {
            return this.structureProcessors;
        }

        @Override
        @Deprecated
        public void init() {
            BEHAVIOURS.put(this.name, this);
        }
    }
}
