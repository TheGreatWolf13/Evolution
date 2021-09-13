package tgw.evolution.world.puzzle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.Evolution;
import tgw.evolution.world.puzzle.pieces.config.PlacementType;

import java.util.List;
import java.util.Random;

public class PuzzlePattern {

    public static final PuzzlePattern EMPTY = new PuzzlePattern(Evolution.getResource("empty"),
                                                                Evolution.getResource("empty"),
                                                                ImmutableList.of(),
                                                                PlacementType.RIGID);
    public static final PuzzlePattern INVALID = new PuzzlePattern(Evolution.getResource("invalid"),
                                                                  Evolution.getResource("invalid"),
                                                                  ImmutableList.of(),
                                                                  PlacementType.RIGID);
    private final ResourceLocation fallbackPool;
    private final ResourceLocation pool;
    private final List<PuzzlePiece> puzzlePieces;
    private int maxHeight = Integer.MIN_VALUE;

    public PuzzlePattern(ResourceLocation pool,
                         ResourceLocation fallbackPool,
                         List<Pair<PuzzlePiece, Integer>> entries,
                         PlacementType placementBehaviour) {
        this.pool = pool;
        this.puzzlePieces = Lists.newArrayList();
        for (Pair<PuzzlePiece, Integer> pair : entries) {
            for (int placingInList = 0; placingInList < pair.getSecond(); placingInList++) {
                this.puzzlePieces.add(pair.getFirst().setPlacementBehaviour(placementBehaviour));
            }
        }
        this.fallbackPool = fallbackPool;
    }

    public ResourceLocation getFallbackPool() {
        return this.fallbackPool;
    }

    public int getMaxHeight(TemplateManager manager) {
        if (this.maxHeight == Integer.MIN_VALUE) {
            this.maxHeight = this.puzzlePieces.stream()
                                              .mapToInt(puzzlePiece -> puzzlePiece.getBoundingBox(manager, BlockPos.ZERO, Rotation.NONE).getYSpan())
                                              .max()
                                              .orElse(0);
        }
        return this.maxHeight;
    }

    public int getNumberOfPieces() {
        return this.puzzlePieces.size();
    }

    public ResourceLocation getPool() {
        return this.pool;
    }

    public PuzzlePiece getRandomPiece(Random rand) {
        if (this.puzzlePieces.isEmpty()) {
            throw new IllegalStateException("Pool size is ZERO: " + this.pool);
        }
        return this.puzzlePieces.get(rand.nextInt(this.puzzlePieces.size()));
    }

    public List<PuzzlePiece> getShuffledPieces(Random rand) {
        return ImmutableList.copyOf(ObjectArrays.shuffle(this.puzzlePieces.toArray(new PuzzlePiece[0]), rand));
    }
}
