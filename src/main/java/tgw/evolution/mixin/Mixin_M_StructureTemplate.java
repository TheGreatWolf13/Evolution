package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.entities.EntityUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.collection.lists.LArrayList;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mixin(StructureTemplate.class)
public abstract class Mixin_M_StructureTemplate {

    @Shadow @Final private List<StructureTemplate.StructureEntityInfo> entityInfoList;
    @Shadow @Final private List<StructureTemplate.Palette> palettes;
    @Shadow private Vec3i size;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor serverLevelAccessor, CompoundTag compoundTag) {
        throw new AbstractMethodError();
    }

    @Unique
    private static @Nullable Entity createEntityIgnoreException_(ServerLevelAccessor serverLevelAccessor, CompoundTag compoundTag) {
        try {
            return EntityUtils.create(compoundTag, serverLevelAccessor.getLevel());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Shadow
    public static List<StructureTemplate.StructureBlockInfo> processBlockInfos(LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, List<StructureTemplate.StructureBlockInfo> list) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static BlockPos transform(BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static Vec3 transform(Vec3 vec3, Mirror mirror, Rotation rotation, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static void updateShapeAtEdge(LevelAccessor levelAccessor, int i, DiscreteVoxelShape discreteVoxelShape, int j, int k, int l) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void placeEntities(ServerLevelAccessor serverLevelAccessor, BlockPos pos, Mirror mirror, Rotation rotation, BlockPos rotationPivot, @Nullable BoundingBox bb, boolean finalize) {
        List<StructureTemplate.StructureEntityInfo> entityInfoList = this.entityInfoList;
        for (int i = 0, len = entityInfoList.size(); i < len; ++i) {
            StructureTemplate.StructureEntityInfo info = entityInfoList.get(i);
            BlockPos infoPos = info.blockPos;
            Vec3 vec = info.pos;
            int x = infoPos.getX();
            int y = infoPos.getY();
            int z = infoPos.getZ();
            double dx = vec.x;
            double dy = vec.y;
            double dz = vec.z;
            boolean mirrored = true;
            switch (mirror) {
                case LEFT_RIGHT -> {
                    z = -z;
                    dz = 1.0 - dz;
                }
                case FRONT_BACK -> {
                    x = -x;
                    dx = 1.0 - dx;
                }
                default -> mirrored = false;
            }
            final int pivotX = rotationPivot.getX();
            final int pivotZ = rotationPivot.getZ();
            int x0;
            int z0;
            double dx0;
            double dz0;
            switch (rotation) {
                case COUNTERCLOCKWISE_90 -> {
                    x0 = pivotX - pivotZ + z;
                    z0 = pivotX + pivotZ - x;
                    dx0 = pivotX - pivotZ + dz;
                    dz0 = pivotX + pivotZ + 1 - dx;
                }
                case CLOCKWISE_90 -> {
                    x0 = pivotX + pivotZ - z;
                    z0 = pivotZ - pivotX + x;
                    dx0 = pivotX + pivotZ + 1 - dz;
                    dz0 = pivotZ - pivotX + dx;
                }
                case CLOCKWISE_180 -> {
                    x0 = pivotX + pivotX - x;
                    z0 = pivotZ + pivotZ - z;
                    dx0 = pivotX + pivotX + 1 - dx;
                    dz0 = pivotZ + pivotZ + 1 - dz;
                }
                default -> {
                    if (mirrored) {
                        x0 = x;
                        z0 = z;
                        dx0 = dx;
                        dz0 = dz;
                    }
                    else {
                        x0 = infoPos.getX();
                        z0 = infoPos.getZ();
                        dx0 = vec.x;
                        dz0 = vec.z;
                    }
                }
            }
            x0 += pos.getX();
            y += pos.getY();
            z0 += pos.getZ();
            dx0 += pos.getX();
            dy += pos.getY();
            dz0 += pos.getZ();
            if (bb != null && !bb.isInside_(x0, y, z0)) {
                continue;
            }
            CompoundTag compoundTag = info.nbt.copy();
            //noinspection ObjectAllocationInLoop
            ListTag posList = new ListTag();
            posList.add(DoubleTag.valueOf(dx0));
            posList.add(DoubleTag.valueOf(dy));
            posList.add(DoubleTag.valueOf(dz0));
            compoundTag.put("Pos", posList);
            compoundTag.remove("UUID");
            Entity entity = createEntityIgnoreException_(serverLevelAccessor, compoundTag);
            if (entity != null) {
                float f = entity.rotate(rotation);
                f += entity.mirror(mirror) - entity.getYRot();
                entity.moveTo(dx0, dy, dz0, f, entity.getXRot());
                if (finalize && entity instanceof Mob mob) {
                    mob.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt_(Mth.floor(dx0), Mth.floor(dy), Mth.floor(dz0)), MobSpawnType.STRUCTURE, null, compoundTag);
                }
                serverLevelAccessor.addFreshEntityWithPassengers(entity);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean placeInWorld(ServerLevelAccessor level, BlockPos pos, BlockPos structurePos, StructurePlaceSettings settings, Random random, @BlockFlags int blockFlags) {
        if (this.palettes.isEmpty()) {
            return false;
        }
        List<StructureTemplate.StructureBlockInfo> blockInfos = settings.getRandomPalette(this.palettes, pos).blocks();
        boolean shouldKeepLiquids = settings.shouldKeepLiquids();
        if ((!blockInfos.isEmpty() || !settings.isIgnoreEntities() && !this.entityInfoList.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            BoundingBox bb = settings.getBoundingBox();
            LList fluids;
            LList fluidSources;
            if (shouldKeepLiquids) {
                fluids = new LArrayList();
                fluidSources = new LArrayList();
            }
            else {
                fluids = null;
                fluidSources = null;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            List<StructureTemplate.StructureBlockInfo> processedBlockInfos = processBlockInfos(level, pos, structurePos, settings, blockInfos);
            Direction[] directions = DirectionUtil.UNESW;
            for (int i = 0; i < processedBlockInfos.size(); ++i) {
                StructureTemplate.StructureBlockInfo blockInfo = processedBlockInfos.get(i);
                BlockPos infoPos = blockInfo.pos;
                int infoPosX = infoPos.getX();
                int infoPosY = infoPos.getY();
                int infoPosZ = infoPos.getZ();
                if (bb != null && !bb.isInside_(infoPosX, infoPosY, infoPosZ)) {
                    processedBlockInfos.remove(i--);
                    continue;
                }
                FluidState fluidState = shouldKeepLiquids ? level.getFluidState_(infoPosX, infoPosY, infoPosZ) : null;
                BlockState blockState = blockInfo.state.mirror(settings.getMirror()).rotate(settings.getRotation());
                //noinspection VariableNotUsedInsideIf
                if (blockInfo.nbt != null) {
                    BlockEntity blockEntity = level.getBlockEntity_(infoPosX, infoPosY, infoPosZ);
                    Clearable.tryClear(blockEntity);
                    level.setBlock_(infoPosX, infoPosY, infoPosZ, Blocks.BARRIER.defaultBlockState(), BlockFlags.NO_RERENDER | BlockFlags.UPDATE_NEIGHBORS);
                }
                if (level.setBlock_(infoPosX, infoPosY, infoPosZ, blockState, blockFlags)) {
                    minX = Math.min(minX, infoPosX);
                    minY = Math.min(minY, infoPosY);
                    minZ = Math.min(minZ, infoPosZ);
                    maxX = Math.max(maxX, infoPosX);
                    maxY = Math.max(maxY, infoPosY);
                    maxZ = Math.max(maxZ, infoPosZ);
                    if (blockInfo.nbt != null) {
                        BlockEntity blockEntity = level.getBlockEntity_(infoPosX, infoPosY, infoPosZ);
                        if (blockEntity != null) {
                            if (blockEntity instanceof RandomizableContainerBlockEntity) {
                                blockInfo.nbt.putLong("LootTableSeed", random.nextLong());
                            }
                            blockEntity.load(blockInfo.nbt);
                        }
                    }
                    if (fluidState != null) {
                        if (blockState.getFluidState().isSource()) {
                            fluidSources.add(BlockPos.asLong(infoPosX, infoPosY, infoPosZ));
                        }
                        else if (blockState.getBlock() instanceof LiquidBlockContainer container) {
                            container.placeLiquid(level, infoPos, blockState, fluidState);
                            if (!fluidState.isSource()) {
                                fluids.add(BlockPos.asLong(infoPosX, infoPosY, infoPosZ));
                            }
                        }
                    }
                }
                else {
                    processedBlockInfos.remove(i--);
                }
            }
            boolean changed = true;
            while (changed && !fluids.isEmpty()) {
                changed = false;
                for (int i = 0; i < fluids.size(); ++i) {
                    long fluidPos = fluids.getLong(i);
                    int fluidPosX = BlockPos.getX(fluidPos);
                    int fluidPosY = BlockPos.getY(fluidPos);
                    int fluidPosZ = BlockPos.getZ(fluidPos);
                    FluidState fluidAtPos = level.getFluidState_(fluidPosX, fluidPosY, fluidPosZ);
                    for (Direction direction : directions) {
                        if (!fluidAtPos.isSource()) {
                            break;
                        }
                        int relFluidPosX = fluidPosX + direction.getStepX();
                        int relFluidPosY = fluidPosY + direction.getStepY();
                        int relFluidPosZ = fluidPosZ + direction.getStepZ();
                        FluidState fluidAtSide = level.getFluidState_(relFluidPosX, relFluidPosY, relFluidPosZ);
                        if (fluidAtSide.isSource() && !fluidSources.contains(BlockPos.asLong(relFluidPosX, relFluidPosY, relFluidPosZ))) {
                            fluidAtPos = fluidAtSide;
                        }
                    }
                    if (fluidAtPos.isSource()) {
                        BlockState state = level.getBlockState_(fluidPosX, fluidPosY, fluidPosZ);
                        if (state.getBlock() instanceof LiquidBlockContainer container) {
                            container.placeLiquid(level, new BlockPos(fluidPosX, fluidPosY, fluidPosZ), state, fluidAtPos);
                            changed = true;
                            fluids.remove(i--);
                        }
                    }
                }
            }
            if (minX <= maxX) {
                if (!settings.getKnownShape()) {
                    DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
                    for (int i = 0, len = processedBlockInfos.size(); i < len; ++i) {
                        BlockPos blockInfoPos = processedBlockInfos.get(i).pos;
                        discreteVoxelShape.fill(blockInfoPos.getX() - minX, blockInfoPos.getY() - minY, blockInfoPos.getZ() - minZ);
                    }
                    updateShapeAtEdge(level, blockFlags, discreteVoxelShape, minX, minY, minZ);
                }
                for (int i = 0, len = processedBlockInfos.size(); i < len; ++i) {
                    StructureTemplate.StructureBlockInfo blockInfo = processedBlockInfos.get(i);
                    BlockPos blockInfoPos = blockInfo.pos;
                    int blockInfoPosX = blockInfoPos.getX();
                    int blockInfoPosY = blockInfoPos.getY();
                    int blockInfoPosZ = blockInfoPos.getZ();
                    if (!settings.getKnownShape()) {
                        BlockState state = level.getBlockState_(blockInfoPosX, blockInfoPosY, blockInfoPosZ);
                        BlockState updatedState = Block.updateFromNeighbourShapes(state, level, blockInfoPos);
                        if (state != updatedState) {
                            level.setBlock_(blockInfoPosX, blockInfoPosY, blockInfoPosZ, updatedState, blockFlags & ~BlockFlags.NOTIFY | BlockFlags.UPDATE_NEIGHBORS);
                        }
                        level.blockUpdated(blockInfoPos, updatedState.getBlock());
                    }
                    //noinspection VariableNotUsedInsideIf
                    if (blockInfo.nbt != null) {
                        BlockEntity blockEntity = level.getBlockEntity_(blockInfoPosX, blockInfoPosY, blockInfoPosZ);
                        if (blockEntity != null) {
                            blockEntity.setChanged();
                        }
                    }
                }
            }
            if (!settings.isIgnoreEntities()) {
                this.placeEntities(level, pos, settings.getMirror(), settings.getRotation(), settings.getRotationPivot(), bb, settings.shouldFinalizeEntities());
            }
            return true;
        }
        return false;
    }
}
