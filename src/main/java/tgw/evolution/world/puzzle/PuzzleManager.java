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
import net.minecraft.world.gen.feature.structure.Structures;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.Logger;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockPuzzle;
import tgw.evolution.util.MathHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PuzzleManager {

    public static final PuzzlePatternRegistry REGISTRY = new PuzzlePatternRegistry();
    private static final Logger LOGGER = Evolution.LOGGER;

    static {
        REGISTRY.register(PuzzlePattern.EMPTY);
    }

    public static void startGeneration(ResourceLocation spawnPool, int size, PuzzleManager.IPieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, TemplateManager manager, BlockPos pos, List<StructurePiece> pieces, Random random) {
        Structures.init();
        new PuzzleManager.Assembler(spawnPool, size, pieceFactory, chunkGenerator, manager, pos, pieces, random);
    }

    public interface IPieceFactory {

        PuzzleStructurePiece create(TemplateManager manager, PuzzlePiece puzzlePiece, BlockPos pos, int groundLevelDelta, Rotation rotation, MutableBoundingBox boundingBox);
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

        public Assembler(ResourceLocation spawnPool, int size, PuzzleManager.IPieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, TemplateManager manager, BlockPos pos, List<StructurePiece> pieces, Random rand) {
            Evolution.LOGGER.debug("Assembler");
            this.size = size;
            Evolution.LOGGER.debug("size = " + size);
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.templateManager = manager;
            this.structurePieces = pieces;
            this.placedPuzzlesPos = new HashSet<>();
            this.rand = rand;
            Rotation chosenRotation = Rotation.randomRotation(rand);
            Evolution.LOGGER.debug("chosenRotation = " + chosenRotation);
            Evolution.LOGGER.debug("spawnPool = " + spawnPool);
            PuzzlePattern pool = PuzzleManager.REGISTRY.get(spawnPool);
            PuzzlePiece chosenSpawnPiece = pool.getRandomPiece(rand);
            Evolution.LOGGER.debug("chosenSpawnPiece = " + chosenSpawnPiece);
            PuzzleStructurePiece structure = pieceFactory.create(manager, chosenSpawnPiece, pos, chosenSpawnPiece.groundLevelDelta(), chosenRotation, chosenSpawnPiece.getBoundingBox(manager, pos, chosenRotation));
            MutableBoundingBox chosenBB = structure.getBoundingBox();
            Evolution.LOGGER.debug("chosenBB = " + chosenBB);
            int middleX = (chosenBB.maxX + chosenBB.minX) / 2;
            Evolution.LOGGER.debug("middleX = " + middleX);
            int middleZ = (chosenBB.maxZ + chosenBB.minZ) / 2;
            Evolution.LOGGER.debug("middleZ = " + middleZ);
            int surfaceY = chunkGenerator.func_222532_b(middleX, middleZ, Heightmap.Type.WORLD_SURFACE_WG);
            Evolution.LOGGER.debug("surfaceY = " + surfaceY);
            //Forces the spawn structure to the surface
            structure.offset(0, surfaceY - (chosenBB.minY + structure.getGroundLevelDelta()), 0);
            pieces.add(structure);
            if (size > 0) {
                Evolution.LOGGER.debug("size > 0");
                AxisAlignedBB limitBB = new AxisAlignedBB(middleX - 80, surfaceY - 80, middleZ - 80, middleX + 80 + 1, surfaceY + 80 + 1, middleZ + 80 + 1);
                Evolution.LOGGER.debug("limitBB = " + limitBB);
                this.availablePieces.addLast(new PuzzleManager.Entry(structure, new AtomicReference<>(MathHelper.subtract(VoxelShapes.create(limitBB), VoxelShapes.create(AxisAlignedBB.func_216363_a(chosenBB)))), surfaceY + 80, 0));
                while (!this.availablePieces.isEmpty()) {
                    Evolution.LOGGER.debug("availablePieces not empty");
                    PuzzleManager.Entry managerEntry = this.availablePieces.removeFirst();
                    this.placePuzzlePiece(managerEntry.structurePiece, managerEntry.currentShape, managerEntry.maxHeight, managerEntry.currentSize);
                }
                Evolution.LOGGER.debug("availablePieces empty");
            }
        }

        private void placePuzzlePiece(PuzzleStructurePiece structurePiece, AtomicReference<VoxelShape> currentShape, int maxHeight, int currentSize) {
            Evolution.LOGGER.debug("placePuzzlePiece");
            PuzzlePiece placingPiece = structurePiece.getPuzzlePiece();
            Evolution.LOGGER.debug("placingPiece = " + placingPiece);
            BlockPos placingPos = structurePiece.getPos();
            Evolution.LOGGER.debug("placingPos = " + placingPos);
            Rotation placingRotation = structurePiece.getRotation();
            Evolution.LOGGER.debug("placingRotation = " + placingRotation);
            PuzzlePattern.PlacementBehaviour placingProjection = placingPiece.getPlacementBehaviour();
            Evolution.LOGGER.debug("placingProjection = " + placingProjection);
            boolean isPlacingRigid = placingProjection == PuzzlePattern.PlacementBehaviour.RIGID;
            Evolution.LOGGER.debug("isPlacingRigid = {}", isPlacingRigid);
            AtomicReference<VoxelShape> checkingBB = new AtomicReference<>();
            MutableBoundingBox placingBB = structurePiece.getBoundingBox();
            Evolution.LOGGER.debug("placingBB = {}", placingBB);
            int placingMinY = placingBB.minY;
            Evolution.LOGGER.debug("placingMinY = {}", placingMinY);
            List<PuzzlePiece> piecesForConnection = Lists.newArrayList();
            BlockPos.MutableBlockPos matchingCornerPos = new BlockPos.MutableBlockPos();
            Set<PuzzlePiece> failedPieces = Sets.newHashSet();
            placingPuzzleBlocks:
            for (Template.BlockInfo puzzleBlock : placingPiece.getPuzzleBlocks(this.templateManager, placingPos, placingRotation, this.rand)) {
                Evolution.LOGGER.debug("1st for: puzzleBlock = {}", puzzleBlock);
                boolean shouldPuzzleBlockCheckBB = puzzleBlock.nbt.getBoolean("CheckBB");
                Evolution.LOGGER.debug("shouldPuzzleBlockCheckBB = {}", shouldPuzzleBlockCheckBB);
                Direction puzzleBlockFacing = puzzleBlock.state.get(BlockPuzzle.FACING);
                Evolution.LOGGER.debug("puzzleBlockFacing = {}", puzzleBlockFacing);
                BlockPos puzzleBlockPos = puzzleBlock.pos;
                Evolution.LOGGER.debug("puzzleBlockPos = {}", puzzleBlockPos);
                BlockPos connectionPos = puzzleBlockPos.offset(puzzleBlockFacing);
                Evolution.LOGGER.debug("connectionPos = {}", connectionPos);
                if (this.placedPuzzlesPos.contains(connectionPos)) {
                    Evolution.LOGGER.debug("puzzle block already matched, skipping");
                    continue;
                }
                int relativePuzzleBlockYPos = puzzleBlockPos.getY() - placingMinY;
                Evolution.LOGGER.debug("relativePuzzleBlockYPos = {}", relativePuzzleBlockYPos);
                //noinspection ObjectAllocationInLoop
                PuzzlePattern targetPool = PuzzleManager.REGISTRY.get(new ResourceLocation(puzzleBlock.nbt.getString("TargetPool")));
                Evolution.LOGGER.debug("targetPool = {}", targetPool.getPool());
                PuzzlePattern fallbackPool = PuzzleManager.REGISTRY.get(targetPool.getFallbackPool());
                Evolution.LOGGER.debug("fallbackPool = {}", fallbackPool.getPool());
                if (targetPool != PuzzlePattern.INVALID && (targetPool.getNumberOfPieces() != 0 || targetPool == PuzzlePattern.EMPTY)) {
                    Evolution.LOGGER.debug("targetPool is not invalid and has more than zero pieces");
                    boolean isConnectionPosInside = placingBB.isVecInside(connectionPos);
                    Evolution.LOGGER.debug("isConnectionPosInside = {}", isConnectionPosInside);
                    if (isConnectionPosInside) {
                        Evolution.LOGGER.debug("connectionPos is inside");
                        if (shouldPuzzleBlockCheckBB) {
                            Evolution.LOGGER.debug("BB must be checked, ignoring puzzle block");
                            continue;
                        }
                        Evolution.LOGGER.debug("BB does not need to be checked");
                    }
                    //noinspection ObjectAllocationInLoop
                    checkingBB.set(shouldPuzzleBlockCheckBB ? currentShape.get() : MathHelper.union(currentShape.get(), VoxelShapes.create(AxisAlignedBB.func_216363_a(placingBB))));
                    piecesForConnection.clear();
                    Evolution.LOGGER.debug("currentSize = {}", currentSize);
                    if (currentSize != this.size) {
                        Evolution.LOGGER.debug("currentSize != size, adding targetPool to piecesForConnection");
                        piecesForConnection.addAll(targetPool.getShuffledPieces(this.rand));
                    }
                    piecesForConnection.addAll(fallbackPool.getShuffledPieces(this.rand));
                    Evolution.LOGGER.debug("adding fallbackPool to piecesForConnection");
                    failedPieces.clear();
                    int surfaceYForPuzzleBlock = -1;
                    for (PuzzlePiece candidatePiece : piecesForConnection) {
                        Evolution.LOGGER.debug("2nd for: candidatePiece = {}", candidatePiece);
                        if (candidatePiece == EmptyPuzzlePiece.INSTANCE) {
                            Evolution.LOGGER.debug("candidatePiece is empty, breaking");
                            break;
                        }
                        if (failedPieces.contains(candidatePiece)) {
                            Evolution.LOGGER.debug("Failed piece, continuing");
                            continue;
                        }
                        for (Rotation candidateRotation : Rotation.shuffledRotations(this.rand)) {
                            Evolution.LOGGER.debug("3rd for: candidateRotation = {}", candidateRotation);
                            List<Template.BlockInfo> candidatePuzzleBlocks = candidatePiece.getPuzzleBlocks(this.templateManager, BlockPos.ZERO, candidateRotation, this.rand);
                            MutableBoundingBox candidateBB = candidatePiece.getBoundingBox(this.templateManager, BlockPos.ZERO, candidateRotation);
                            Evolution.LOGGER.debug("candidateBB = {}", candidateBB);
                            //not sure yet; has to do with connections inside their own BB
                            //                            int maxPoolHeight;
                            //                            if (candidateBB.getYSize() > 16) {
                            //                                Evolution.LOGGER.debug("candidateBB ySize > 16, maxPoolHeight = 0");
                            //                                maxPoolHeight = 0;
                            //                            }
                            //                            else {
                            //                                //noinspection ObjectAllocationInLoop
                            //                                maxPoolHeight = candidatePuzzleBlocks.stream().mapToInt(candidatePuzzleBlock -> {
                            //                                    //checks if the PuzzleBlock connection is not inside the bounding box of its own piece
                            //                                    if (!candidateBB.isVecInside(candidatePuzzleBlock.pos.offset(candidatePuzzleBlock.state.get(BlockPuzzle.FACING)))) {
                            //                                        return 0;
                            //                                    }
                            //                                    //noinspection ObjectAllocationInLoop
                            //                                    ResourceLocation candidatePoolLocation = new ResourceLocation(candidatePuzzleBlock.nbt.getString("TargetPool"));
                            //                                    PuzzlePattern candidateTargetPool = PuzzleManager.REGISTRY.get(candidatePoolLocation);
                            //                                    PuzzlePattern candidateFallbackPool = PuzzleManager.REGISTRY.get(candidateTargetPool.getFallbackPool());
                            //                                    return Math.max(candidateTargetPool.getMaxHeight(this.templateManager), candidateFallbackPool.getMaxHeight(this.templateManager));
                            //                                }).max().orElse(0);
                            //                                Evolution.LOGGER.debug("maxPoolHeight = {}", maxPoolHeight);
                            //                            }
                            for (Template.BlockInfo candidatePuzzleBlock : candidatePuzzleBlocks) {
                                Evolution.LOGGER.debug("4th for: candidatePuzzleBlock = {}", candidatePuzzleBlock);
                                if (BlockPuzzle.puzzlesMatches(puzzleBlock, candidatePuzzleBlock)) {
                                    Evolution.LOGGER.debug("puzzles matches");
                                    BlockPos matchingPuzzleBlockRelativePos = candidatePuzzleBlock.pos;
                                    Evolution.LOGGER.debug("matchingPuzzleBlockpos = {}", matchingPuzzleBlockRelativePos);
                                    matchingCornerPos.setPos(connectionPos.getX() - matchingPuzzleBlockRelativePos.getX(), connectionPos.getY() - matchingPuzzleBlockRelativePos.getY(), connectionPos.getZ() - matchingPuzzleBlockRelativePos.getZ());
                                    Evolution.LOGGER.debug("matchingCornerPos = {}", matchingCornerPos);
                                    MutableBoundingBox matchingBB = candidatePiece.getBoundingBox(this.templateManager, matchingCornerPos, candidateRotation);
                                    Evolution.LOGGER.debug("matchingBB = {}", matchingBB);
                                    int matchingMinY = matchingBB.minY;
                                    Evolution.LOGGER.debug("matchingMinY = {}", matchingMinY);
                                    PuzzlePattern.PlacementBehaviour matchingProjection = candidatePiece.getPlacementBehaviour();
                                    Evolution.LOGGER.debug("matchingProjection = {}", matchingProjection);
                                    boolean isMatchingRigid = matchingProjection == PuzzlePattern.PlacementBehaviour.RIGID;
                                    Evolution.LOGGER.debug("isMatchingRigid = {}", isMatchingRigid);
                                    int matchingPuzzleBlockRelativePosY = matchingPuzzleBlockRelativePos.getY();
                                    Evolution.LOGGER.debug("matchingPuzzleBlockRelativePosY = {}", matchingPuzzleBlockRelativePosY);
                                    int heightDelta = relativePuzzleBlockYPos - matchingPuzzleBlockRelativePosY + puzzleBlock.state.get(BlockPuzzle.FACING).getYOffset();
                                    Evolution.LOGGER.debug("heightDelta = {}", heightDelta);
                                    int actualMatchingMinY;
                                    if (isPlacingRigid && isMatchingRigid) {
                                        Evolution.LOGGER.debug("both are rigid");
                                        actualMatchingMinY = placingMinY + heightDelta;
                                    }
                                    else {
                                        Evolution.LOGGER.debug("at least one is not rigid");
                                        if (surfaceYForPuzzleBlock == -1) {
                                            surfaceYForPuzzleBlock = this.chunkGenerator.func_222532_b(puzzleBlockPos.getX(), puzzleBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                            Evolution.LOGGER.debug("surfaceYForPuzzleBlock = {}", surfaceYForPuzzleBlock);
                                        }
                                        actualMatchingMinY = surfaceYForPuzzleBlock - matchingPuzzleBlockRelativePosY;
                                    }
                                    Evolution.LOGGER.debug("actualMatchingMinY = {}", actualMatchingMinY);
                                    int matchingMinYDelta = actualMatchingMinY - matchingMinY;
                                    Evolution.LOGGER.debug("matchingMinYDelta = {}", matchingMinYDelta);
                                    //func_215127_b moves placingBB
                                    MutableBoundingBox actualMachingBB = matchingMinYDelta == 0 ? matchingBB : matchingBB.func_215127_b(0, matchingMinYDelta, 0);
                                    Evolution.LOGGER.debug("actualMachingBB = {}", actualMachingBB);
                                    BlockPos actualMatchingCornerPos = matchingCornerPos.add(0, matchingMinYDelta, 0);
                                    Evolution.LOGGER.debug("actualMatchingCornerPos = {}", actualMatchingCornerPos);
                                    //                                    if (maxPoolHeight > 0) {
                                    //                                        Evolution.LOGGER.debug("maxPoolHeight > 0");
                                    //                                        int k2 = Math.max(maxPoolHeight + 1, actualMachingBB.maxY - actualMachingBB.minY);
                                    //                                        Evolution.LOGGER.debug("k2 = {}", k2);
                                    //                                        actualMachingBB.maxY = actualMachingBB.minY + k2;
                                    //                                        Evolution.LOGGER.debug("actualMachingBB.maxY = {}", actualMachingBB.maxY);
                                    //                                    }
                                    boolean canPlace;
                                    if (candidatePiece instanceof ForcedPuzzlePiece) {
                                        Evolution.LOGGER.debug("candidatePiece instanceof ForcedPuzzlePiece");
                                        switch (((ForcedPuzzlePiece) candidatePiece).getForceType()) {
                                            case HARD:
                                                Evolution.LOGGER.debug("forceType is HARD");
                                                canPlace = true;
                                                break;
                                            case SOFT:
                                                //noinspection ObjectAllocationInLoop
                                                canPlace = !MathHelper.isShapeTotallyOutside(VoxelShapes.create(AxisAlignedBB.func_216363_a(actualMachingBB).shrink(0.25)), checkingBB.get());
                                                Evolution.LOGGER.debug("forceType is SOFT and resulted in {}", canPlace);
                                                break;
                                            default:
                                                throw new IllegalStateException("Missing ForceType");
                                        }
                                    }
                                    else {
                                        //noinspection ObjectAllocationInLoop
                                        canPlace = MathHelper.isShapeTotallyInside(VoxelShapes.create(AxisAlignedBB.func_216363_a(actualMachingBB).shrink(0.25)), checkingBB.get());
                                    }
                                    if (canPlace) {
                                        Evolution.LOGGER.debug("actualMatchingBB is inside currentShape");
                                        currentShape.set(MathHelper.subtract(currentShape.get(), VoxelShapes.create(AxisAlignedBB.func_216363_a(actualMachingBB))));
                                        int placingGroundLevelDelta = structurePiece.getGroundLevelDelta();
                                        Evolution.LOGGER.debug("placingGroundLevelDelta = {}", placingGroundLevelDelta);
                                        int matchingGroundLevelDelta;
                                        if (isMatchingRigid) {
                                            Evolution.LOGGER.debug("Matching is rigid");
                                            matchingGroundLevelDelta = placingGroundLevelDelta - heightDelta;
                                        }
                                        else {
                                            Evolution.LOGGER.debug("Matching is NOT rigid");
                                            matchingGroundLevelDelta = candidatePiece.groundLevelDelta();
                                        }
                                        Evolution.LOGGER.debug("matchingGroundLevelDelta = {}", matchingGroundLevelDelta);
                                        PuzzleStructurePiece matchingStructure = this.pieceFactory.create(this.templateManager, candidatePiece, actualMatchingCornerPos, matchingGroundLevelDelta, candidateRotation, actualMachingBB);
                                        int junctionPosY;
                                        if (isPlacingRigid) {
                                            Evolution.LOGGER.debug("Placing is rigid");
                                            junctionPosY = placingMinY + relativePuzzleBlockYPos;
                                        }
                                        else if (isMatchingRigid) {
                                            Evolution.LOGGER.debug("Matching is rigid");
                                            junctionPosY = actualMatchingMinY + matchingPuzzleBlockRelativePosY;
                                        }
                                        else {
                                            Evolution.LOGGER.debug("None are rigid");
                                            if (surfaceYForPuzzleBlock == -1) {
                                                surfaceYForPuzzleBlock = this.chunkGenerator.func_222532_b(puzzleBlockPos.getX(), puzzleBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                            }
                                            Evolution.LOGGER.debug("surfaceYForPuzzleBlock = {}", surfaceYForPuzzleBlock);
                                            junctionPosY = surfaceYForPuzzleBlock + heightDelta / 2;
                                        }
                                        Evolution.LOGGER.debug("junctionPosY = {}", junctionPosY);
                                        structurePiece.addJunction(new PuzzleJunction(connectionPos.getX(), junctionPosY - relativePuzzleBlockYPos + placingGroundLevelDelta, connectionPos.getZ(), heightDelta, matchingProjection));
                                        matchingStructure.addJunction(new PuzzleJunction(puzzleBlockPos.getX(), junctionPosY - matchingPuzzleBlockRelativePosY + matchingGroundLevelDelta, puzzleBlockPos.getZ(), -heightDelta, placingProjection));
                                        this.structurePieces.add(matchingStructure);
                                        Evolution.LOGGER.debug("currentSize = {}", currentSize);
                                        if (currentSize + 1 <= this.size) {
                                            Evolution.LOGGER.debug("currentSize + 1 <= size, size = {}", this.size);
                                            //noinspection ObjectAllocationInLoop
                                            this.availablePieces.addLast(new Entry(matchingStructure, currentShape, maxHeight, currentSize + 1));
                                            Evolution.LOGGER.debug("added {} to availableList", matchingStructure);
                                        }
                                        Evolution.LOGGER.debug("continuing to 1st loop");
                                        this.placedPuzzlesPos.add(puzzleBlockPos);
                                        continue placingPuzzleBlocks;
                                    }
                                }
                                else {
                                    Evolution.LOGGER.debug("puzzles do not match");
                                }
                            }
                        }
                        failedPieces.add(candidatePiece);
                    }
                }
                else {
                    PuzzleManager.LOGGER.warn("Empty or none existent pool: {}", puzzleBlock.nbt.getString("TargetPool"));
                }
            }
        }
    }

    static final class Entry {

        private final PuzzleStructurePiece structurePiece;
        private final AtomicReference<VoxelShape> currentShape;
        private final int maxHeight;
        private final int currentSize;

        private Entry(PuzzleStructurePiece structurePiece, AtomicReference<VoxelShape> currentShape, int maxHeight, int currentSize) {
            this.structurePiece = structurePiece;
            this.currentShape = currentShape;
            this.maxHeight = maxHeight;
            this.currentSize = currentSize;
        }
    }
}
