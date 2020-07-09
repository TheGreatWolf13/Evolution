package tgw.evolution.world.puzzle;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockPuzzle;
import tgw.evolution.util.MathHelper;
import tgw.evolution.world.feature.structures.config.IConfigStruct;
import tgw.evolution.world.puzzle.pieces.ConfiguredPuzzlePiece;
import tgw.evolution.world.puzzle.pieces.EmptyPuzzlePiece;
import tgw.evolution.world.puzzle.pieces.config.PlacementType;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PuzzleManager {

    public static final PuzzlePatternRegistry REGISTRY = new PuzzlePatternRegistry();
    public static final Logger LOGGER = LogManager.getLogger();

    static {
        REGISTRY.register(PuzzlePattern.EMPTY);
    }

    public static void startGeneration(ResourceLocation spawnPool, int size, PuzzleManager.IPieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, TemplateManager manager, BlockPos pos, List<StructurePiece> pieces, Random random, IConfigStruct config) {
        new PuzzleManager.Assembler(spawnPool, size, pieceFactory, chunkGenerator, manager, pos, pieces, random, config);
    }

    public interface IPieceFactory {

        StructurePuzzlePiece create(TemplateManager manager, PuzzlePiece puzzlePiece, BlockPos pos, int groundLevelDelta, Rotation rotation, MutableBoundingBox boundingBox);
    }

    static final class Assembler {

        private final int size;
        private final PuzzleManager.IPieceFactory pieceFactory;
        private final ChunkGenerator<?> chunkGenerator;
        private final TemplateManager templateManager;
        private final List<StructurePiece> structurePieces;
        private final Random rand;
        private final Deque<PuzzleManager.Entry> availablePieces = Queues.newArrayDeque();
        private final Set<BlockPos> placedPuzzlesPos;
        private final IConfigStruct config;

        public Assembler(ResourceLocation spawnPool, int size, PuzzleManager.IPieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, TemplateManager manager, BlockPos pos, List<StructurePiece> pieces, Random rand, IConfigStruct config) {
            this.size = size;
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.templateManager = manager;
            this.structurePieces = pieces;
            this.placedPuzzlesPos = new HashSet<>();
            this.config = config;
            this.rand = rand;
            Rotation chosenRotation = Rotation.randomRotation(rand);
            PuzzlePattern pool = PuzzleManager.REGISTRY.get(spawnPool);
            PuzzlePiece chosenSpawnPiece = pool.getRandomPiece(rand);
            StructurePuzzlePiece structure = pieceFactory.create(manager, chosenSpawnPiece, pos, chosenSpawnPiece.groundLevelDelta(), chosenRotation, chosenSpawnPiece.getBoundingBox(manager, pos, chosenRotation));
            MutableBoundingBox chosenBB = structure.getBoundingBox();
            int middleX = (chosenBB.maxX + chosenBB.minX) / 2;
            int middleZ = (chosenBB.maxZ + chosenBB.minZ) / 2;
            int surfaceY = chunkGenerator.func_222532_b(middleX, middleZ, Heightmap.Type.WORLD_SURFACE_WG);
            pieces.add(structure);
            if (size > 0) {
                AxisAlignedBB limitBB = new AxisAlignedBB(middleX - 128, MathHelper.clampMin(surfaceY - 80, 5), middleZ - 128, middleX + 128 + 1, surfaceY + 80 + 1, middleZ + 128 + 1);
                this.availablePieces.addLast(new PuzzleManager.Entry(structure, new AtomicReference<>(MathHelper.subtract(VoxelShapes.create(limitBB), VoxelShapes.create(AxisAlignedBB.func_216363_a(chosenBB)))), surfaceY + 80, 0));
                while (!this.availablePieces.isEmpty()) {
                    PuzzleManager.Entry managerEntry = this.availablePieces.removeFirst();
                    this.placePuzzlePiece(managerEntry.structurePiece, managerEntry.currentShape, managerEntry.maxHeight, managerEntry.currentSize);
                }
            }
        }

        private void placePuzzlePiece(StructurePuzzlePiece structurePiece, AtomicReference<VoxelShape> currentShape, int maxHeight, int currentSize) {
            PuzzlePiece placingPiece = structurePiece.getPuzzlePiece();
            BlockPos placingPos = structurePiece.getPos();
            Rotation placingRotation = structurePiece.getRotation();
            PlacementType placingProjection = placingPiece.getPlacementBehaviour();
            boolean isPlacingRigid = placingProjection == PlacementType.RIGID;
            AtomicReference<VoxelShape> checkingShape = new AtomicReference<>();
            MutableBoundingBox placingBB = structurePiece.getBoundingBox();
            int placingMinY = placingBB.minY;
            List<PuzzlePiece> piecesForConnection = Lists.newArrayList();
            BlockPos.MutableBlockPos matchingCornerPos = new BlockPos.MutableBlockPos();
            Set<PuzzlePiece> failedPieces = Sets.newHashSet();
            placingPuzzleBlocks:
            for (Template.BlockInfo puzzleBlock : placingPiece.getPuzzleBlocks(this.templateManager, placingPos, placingRotation, this.rand)) {
                boolean shouldPuzzleBlockCheckBB = puzzleBlock.nbt.getBoolean("CheckBB");
                Direction puzzleBlockFacing = puzzleBlock.state.get(BlockPuzzle.FACING);
                BlockPos puzzleBlockPos = puzzleBlock.pos;
                BlockPos connectionPos = puzzleBlockPos.offset(puzzleBlockFacing);
                if (this.placedPuzzlesPos.contains(connectionPos)) {
                    continue;
                }
                int relativePuzzleBlockYPos = puzzleBlockPos.getY() - placingMinY;
                //noinspection ObjectAllocationInLoop
                PuzzlePattern targetPool = PuzzleManager.REGISTRY.get(new ResourceLocation(puzzleBlock.nbt.getString("TargetPool")));
                PuzzlePattern fallbackPool = PuzzleManager.REGISTRY.get(targetPool.getFallbackPool());
                if (targetPool != PuzzlePattern.INVALID && (targetPool.getNumberOfPieces() != 0 || targetPool == PuzzlePattern.EMPTY)) {
                    boolean isConnectionPosInside = placingBB.isVecInside(connectionPos);
                    if (isConnectionPosInside) {
                        if (shouldPuzzleBlockCheckBB) {
                            LOGGER.warn("Ignoring Puzzle Block at {} because of the Bounding Box check. This is probably a bug", puzzleBlockPos);
                            continue;
                        }
                    }
                    //noinspection ObjectAllocationInLoop
                    checkingShape.set(shouldPuzzleBlockCheckBB ? currentShape.get() : MathHelper.union(currentShape.get(), VoxelShapes.create(AxisAlignedBB.func_216363_a(placingBB))));
                    piecesForConnection.clear();
                    if (currentSize != this.size) {
                        piecesForConnection.addAll(targetPool.getShuffledPieces(this.rand));
                    }
                    piecesForConnection.addAll(fallbackPool.getShuffledPieces(this.rand));
                    failedPieces.clear();
                    int surfaceYForPuzzleBlock = -1;
                    for (PuzzlePiece candidatePiece : piecesForConnection) {
                        if (candidatePiece == EmptyPuzzlePiece.INSTANCE) {
                            break;
                        }
                        if (failedPieces.contains(candidatePiece)) {
                            continue;
                        }
                        for (Rotation candidateRotation : Rotation.shuffledRotations(this.rand)) {
                            List<Template.BlockInfo> candidatePuzzleBlocks = candidatePiece.getPuzzleBlocks(this.templateManager, BlockPos.ZERO, candidateRotation, this.rand);
                            for (Template.BlockInfo candidatePuzzleBlock : candidatePuzzleBlocks) {
                                if (BlockPuzzle.puzzlesMatches(puzzleBlock, candidatePuzzleBlock)) {
                                    BlockPos matchingPuzzleBlockRelativePos = candidatePuzzleBlock.pos;
                                    matchingCornerPos.setPos(connectionPos.getX() - matchingPuzzleBlockRelativePos.getX(), connectionPos.getY() - matchingPuzzleBlockRelativePos.getY(), connectionPos.getZ() - matchingPuzzleBlockRelativePos.getZ());
                                    MutableBoundingBox matchingBB = candidatePiece.getBoundingBox(this.templateManager, matchingCornerPos, candidateRotation);
                                    int matchingMinY = matchingBB.minY;
                                    PlacementType matchingProjection = candidatePiece.getPlacementBehaviour();
                                    boolean isMatchingRigid = matchingProjection == PlacementType.RIGID;
                                    int matchingPuzzleBlockRelativePosY = matchingPuzzleBlockRelativePos.getY();
                                    int heightDelta = relativePuzzleBlockYPos - matchingPuzzleBlockRelativePosY + puzzleBlock.state.get(BlockPuzzle.FACING).getYOffset();
                                    int actualMatchingMinY;
                                    if (isPlacingRigid && isMatchingRigid) {
                                        actualMatchingMinY = placingMinY + heightDelta;
                                    }
                                    else {
                                        if (surfaceYForPuzzleBlock == -1) {
                                            surfaceYForPuzzleBlock = this.chunkGenerator.func_222532_b(puzzleBlockPos.getX(), puzzleBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                        }
                                        actualMatchingMinY = surfaceYForPuzzleBlock - matchingPuzzleBlockRelativePosY;
                                    }
                                    int matchingMinYDelta = actualMatchingMinY - matchingMinY;
                                    //func_215127_b moves placingBB
                                    MutableBoundingBox actualMachingBB = matchingMinYDelta == 0 ? matchingBB : matchingBB.func_215127_b(0, matchingMinYDelta, 0);
                                    BlockPos actualMatchingCornerPos = matchingCornerPos.add(0, matchingMinYDelta, 0);
                                    if (this.canPlace(candidatePiece, actualMachingBB, checkingShape)) {
                                        if (candidatePiece instanceof ConfiguredPuzzlePiece) {
                                            ((ConfiguredPuzzlePiece) candidatePiece).success(this.config);
                                        }
                                        currentShape.set(MathHelper.subtract(currentShape.get(), VoxelShapes.create(AxisAlignedBB.func_216363_a(actualMachingBB))));
                                        int placingGroundLevelDelta = structurePiece.getGroundLevelDelta();
                                        int matchingGroundLevelDelta;
                                        if (isMatchingRigid) {
                                            matchingGroundLevelDelta = placingGroundLevelDelta - heightDelta;
                                        }
                                        else {
                                            matchingGroundLevelDelta = candidatePiece.groundLevelDelta();
                                        }
                                        StructurePuzzlePiece matchingStructure = this.pieceFactory.create(this.templateManager, candidatePiece, actualMatchingCornerPos, matchingGroundLevelDelta, candidateRotation, actualMachingBB);
                                        int junctionPosY;
                                        if (isPlacingRigid) {
                                            junctionPosY = placingMinY + relativePuzzleBlockYPos;
                                        }
                                        else if (isMatchingRigid) {
                                            junctionPosY = actualMatchingMinY + matchingPuzzleBlockRelativePosY;
                                        }
                                        else {
                                            if (surfaceYForPuzzleBlock == -1) {
                                                surfaceYForPuzzleBlock = this.chunkGenerator.func_222532_b(puzzleBlockPos.getX(), puzzleBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                            }
                                            junctionPosY = surfaceYForPuzzleBlock + heightDelta / 2;
                                        }
                                        structurePiece.addJunction(new PuzzleJunction(connectionPos.getX(), junctionPosY - relativePuzzleBlockYPos + placingGroundLevelDelta, connectionPos.getZ(), heightDelta, matchingProjection));
                                        matchingStructure.addJunction(new PuzzleJunction(puzzleBlockPos.getX(), junctionPosY - matchingPuzzleBlockRelativePosY + matchingGroundLevelDelta, puzzleBlockPos.getZ(), -heightDelta, placingProjection));
                                        this.structurePieces.add(matchingStructure);
                                        if (currentSize + 1 <= this.size) {
                                            //noinspection ObjectAllocationInLoop
                                            this.availablePieces.addLast(new Entry(matchingStructure, currentShape, maxHeight, currentSize + 1));
                                        }
                                        this.placedPuzzlesPos.add(puzzleBlockPos);
                                        continue placingPuzzleBlocks;
                                    }
                                }
                            }
                        }
                        failedPieces.add(candidatePiece);
                    }
                }
                else {
                    LOGGER.warn("Empty or none existent pool: {}", puzzleBlock.nbt.getString("TargetPool"));
                }
            }
        }

        private boolean canPlace(PuzzlePiece piece, MutableBoundingBox pieceBB, AtomicReference<VoxelShape> checkingShape) {
            if (piece instanceof ConfiguredPuzzlePiece) {
                ConfiguredPuzzlePiece config = (ConfiguredPuzzlePiece) piece;
                if (!config.childConditions(this.config)) {
                    return false;
                }
                switch (config.getForceType()) {
                    case HARD:
                        return true;
                    case SOFT:
                        return !MathHelper.isShapeTotallyOutside(VoxelShapes.create(AxisAlignedBB.func_216363_a(pieceBB).shrink(0.25)), checkingShape.get());
                }
                if (config.getDesiredHeight() != -1) {
                    int standardDeviation = 3;
                    int chosenHeight = (pieceBB.minY + pieceBB.maxY) / 2;
                    int deltaY = Math.abs(config.getDesiredHeight() - chosenHeight);
                    if (Math.abs(this.rand.nextGaussian()) >= standardDeviation - deltaY / (config.getMaxDeviation() / (float) standardDeviation)) {
                        return false;
                    }
                }
                if (config.isUnderground()) {
                    int middleX = (pieceBB.maxX + pieceBB.minX) / 2;
                    int middleZ = (pieceBB.maxZ + pieceBB.minZ) / 2;
                    int surfaceY = this.chunkGenerator.func_222532_b(middleX, middleZ, Heightmap.Type.OCEAN_FLOOR_WG);
                    if (pieceBB.maxY > surfaceY - 5) {
                        return false;
                    }
                }
            }
            return MathHelper.isShapeTotallyInside(VoxelShapes.create(AxisAlignedBB.func_216363_a(pieceBB).shrink(0.25)), checkingShape.get());
        }
    }

    static final class Entry {

        private final StructurePuzzlePiece structurePiece;
        private final AtomicReference<VoxelShape> currentShape;
        private final int maxHeight;
        private final int currentSize;

        private Entry(StructurePuzzlePiece structurePiece, AtomicReference<VoxelShape> currentShape, int maxHeight, int currentSize) {
            this.structurePiece = structurePiece;
            this.currentShape = currentShape;
            this.maxHeight = maxHeight;
            this.currentSize = currentSize;
        }
    }
}
