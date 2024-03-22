package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.*;

@Mixin(JigsawPlacement.Placer.class)
public abstract class MixinJigsawPlacement_Placer {

    @Shadow @Final Deque<JigsawPlacement.PieceState> placing;
    @Shadow @Final private ChunkGenerator chunkGenerator;
    @Shadow @Final private JigsawPlacement.PieceFactory factory;
    @Shadow @Final private int maxDepth;
    @Shadow @Final private List<? super PoolElementStructurePiece> pieces;
    @Shadow @Final private Registry<StructureTemplatePool> pools;
    @Shadow @Final private Random random;
    @Shadow @Final private StructureManager structureManager;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void tryPlacingChildren(PoolElementStructurePiece piecePool, MutableObject<VoxelShape> voxelShape, int i, boolean bl, LevelHeightAccessor level) {
        StructurePoolElement structurePoolElement = piecePool.getElement();
        BlockPos blockPos = piecePool.getPosition();
        Rotation rotation = piecePool.getRotation();
        StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
        boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
        MutableObject<VoxelShape> mutableObject2 = new MutableObject<>();
        BoundingBox boundingBox = piecePool.getBoundingBox();
        int j = boundingBox.minY();
        List<StructureTemplate.StructureBlockInfo> shuffledJigsawBlocks = structurePoolElement.getShuffledJigsawBlocks(this.structureManager, blockPos, rotation, this.random);
        Iterator<StructureTemplate.StructureBlockInfo> var14 = shuffledJigsawBlocks.iterator();
        while (true) {
            while (true) {
                while (true) {
                    label93:
                    for (int ii = 0, len = shuffledJigsawBlocks.size(); ii < len; ++ii) {
                        StructureTemplate.StructureBlockInfo structureBlockInfo = shuffledJigsawBlocks.get(ii);
                        Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo.state);
                        final BlockPos infoPos = structureBlockInfo.pos;
                        int blockPos2X = infoPos.getX();
                        int blockPos2Y = infoPos.getY();
                        int blockPos2Z = infoPos.getZ();
                        int blockPos3X = blockPos2X + direction.getStepX();
                        int blockPos3Y = blockPos2Y + direction.getStepY();
                        int blockPos3Z = blockPos2Z + direction.getStepZ();
                        ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo.nbt.getString("pool"));
                        Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
                        if (optional.isPresent() && (optional.get().size() != 0 || resourceLocation.equals(Pools.EMPTY.location()))) {
                            ResourceLocation resourceLocation2 = optional.get().getFallback();
                            Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourceLocation2);
                            if (optional2.isPresent() && (optional2.get().size() != 0 || resourceLocation2.equals(Pools.EMPTY.location()))) {
                                boolean bl3 = boundingBox.isInside_(blockPos3X, blockPos3Y, blockPos3Z);
                                MutableObject mutableObject3;
                                if (bl3) {
                                    mutableObject3 = mutableObject2;
                                    if (mutableObject2.getValue() == null) {
                                        mutableObject2.setValue(Shapes.create(AABB.of(boundingBox)));
                                    }
                                }
                                else {
                                    mutableObject3 = voxelShape;
                                }
                                OList<StructurePoolElement> list = new OArrayList<>();
                                if (i != this.maxDepth) {
                                    list.addAll(optional.get().getShuffledTemplates(this.random));
                                }
                                list.addAll(optional2.get().getShuffledTemplates(this.random));
                                int l = -1;
                                int k = blockPos2Y - j;
                                for (int iv = 0, len3 = list.size(); iv < len3; ++iv) {
                                    StructurePoolElement structurePoolElement2 = list.get(iv);
                                    if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
                                        break;
                                    }
                                    List<Rotation> shuffled = Rotation.getShuffled(this.random);
                                    label133:
                                    for (int iii = 0, len2 = shuffled.size(); iii < len2; ++iii) {
                                        Rotation rotation2 = shuffled.get(iii);
                                        List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation2, this.random);
                                        BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation2);
                                        int m = 0;
                                        if (bl && boundingBox2.getYSpan() <= 16) {
                                            for (int iiii = 0, len4 = list2.size(); iiii < len4; ++iiii) {
                                                StructureTemplate.StructureBlockInfo blockInfo = list2.get(iiii);
                                                Direction frontFacing = JigsawBlock.getFrontFacing(blockInfo.state);
                                                if (!boundingBox2.isInside_(blockInfo.pos.getX() + frontFacing.getStepX(), blockInfo.pos.getY() + frontFacing.getStepY(), blockInfo.pos.getZ() + frontFacing.getStepZ())) {
                                                    continue;
                                                }
                                                ResourceLocation pool = new ResourceLocation(blockInfo.nbt.getString("pool"));
                                                StructureTemplatePool templatePool = this.pools.get(pool);
                                                StructureTemplatePool fallbackTemplatePool = templatePool != null ? this.pools.get(templatePool.getFallback()) : null;
                                                int maxSize = templatePool != null ? templatePool.getMaxSize(this.structureManager) : 0;
                                                int fallbackMaxSize = fallbackTemplatePool != null ? fallbackTemplatePool.getMaxSize(this.structureManager) : 0;
                                                int max = Math.max(maxSize, fallbackMaxSize);
                                                if (max > m) {
                                                    m = max;
                                                }
                                            }
                                        }
                                        Iterator<StructureTemplate.StructureBlockInfo> var35 = list2.iterator();
                                        StructureTemplatePool.Projection projection2;
                                        boolean bl4;
                                        int o;
                                        int p;
                                        int q;
                                        BoundingBox boundingBox4;
                                        BlockPos blockPos6;
                                        int s;
                                        do {
                                            StructureTemplate.StructureBlockInfo structureBlockInfo2;
                                            do {
                                                if (!var35.hasNext()) {
                                                    continue label133;
                                                }

                                                structureBlockInfo2 = var35.next();
                                            } while (!JigsawBlock.canAttach(structureBlockInfo, structureBlockInfo2));
                                            final BlockPos blockPos4 = structureBlockInfo2.pos;
                                            int blockPos4X = blockPos4.getX();
                                            int blockPos4Y = blockPos4.getY();
                                            int blockPos4Z = blockPos4.getZ();
                                            int blockPos5X = blockPos3X - blockPos4X;
                                            int blockPos5Y = blockPos3Y - blockPos4Y;
                                            int blockPos5Z = blockPos3Z - blockPos4Z;
                                            BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureManager, new BlockPos(blockPos5X, blockPos5Y, blockPos5Z), rotation2);
                                            int n = boundingBox3.minY();
                                            projection2 = structurePoolElement2.getProjection();
                                            bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
                                            o = blockPos4Y;
                                            p = k - o + JigsawBlock.getFrontFacing(structureBlockInfo.state).getStepY();
                                            if (bl2 && bl4) {
                                                q = j + p;
                                            }
                                            else {
                                                if (l == -1) {
                                                    l = this.chunkGenerator.getFirstFreeHeight(blockPos2X, blockPos2Z, Heightmap.Types.WORLD_SURFACE_WG, level);
                                                }
                                                q = l - o;
                                            }
                                            int r = q - n;
                                            boundingBox4 = boundingBox3.moved(0, r, 0);
                                            blockPos6 = new BlockPos(blockPos5X, blockPos5Y + r, blockPos5Z);
                                            if (m > 0) {
                                                s = Math.max(m + 1, boundingBox4.maxY() - boundingBox4.minY());
                                                boundingBox4.encapsulate_(boundingBox4.minX(), boundingBox4.minY() + s, boundingBox4.minZ());
                                            }
                                        } while (Shapes.joinIsNotEmpty((VoxelShape) mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4).deflate(0.25D)), BooleanOp.ONLY_SECOND));
                                        mutableObject3.setValue(Shapes.joinUnoptimized((VoxelShape) mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
                                        s = piecePool.getGroundLevelDelta();
                                        int t;
                                        if (bl4) {
                                            t = s - p;
                                        }
                                        else {
                                            t = structurePoolElement2.getGroundLevelDelta();
                                        }
                                        PoolElementStructurePiece poolElementStructurePiece2 = this.factory.create(this.structureManager, structurePoolElement2, blockPos6, t, rotation2, boundingBox4);
                                        int u;
                                        if (bl2) {
                                            u = j + k;
                                        }
                                        else if (bl4) {
                                            u = q + o;
                                        }
                                        else {
                                            if (l == -1) {
                                                l = this.chunkGenerator.getFirstFreeHeight(blockPos2X, blockPos2Z, Heightmap.Types.WORLD_SURFACE_WG, level);
                                            }
                                            u = l + p / 2;
                                        }
                                        piecePool.addJunction(new JigsawJunction(blockPos3X, u - k + s, blockPos3Z, p, projection2));
                                        poolElementStructurePiece2.addJunction(new JigsawJunction(blockPos2X, u - o + t, blockPos2Z, -p, projection));
                                        this.pieces.add(poolElementStructurePiece2);
                                        if (i + 1 <= this.maxDepth) {
                                            this.placing.addLast(new JigsawPlacement.PieceState(poolElementStructurePiece2, mutableObject3, i + 1));
                                        }
                                        continue label93;
                                    }
                                }
                            }
                            else {
                                Evolution.warn("Empty or non-existent fallback pool: {}", resourceLocation2);
                            }
                        }
                        else {
                            Evolution.warn("Empty or non-existent pool: {}", resourceLocation);
                        }
                    }
                    return;
                }
            }
        }
    }
}
