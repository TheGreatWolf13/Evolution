package tgw.evolution.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.blocks.IAir;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.patches.ILevelChunkSectionPatch;
import tgw.evolution.util.ChunkHolder;
import tgw.evolution.util.collection.IArrayList;
import tgw.evolution.util.collection.IList;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;

import java.util.stream.Stream;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements ICapabilityProviderImpl<LevelChunk> {

    private static final ThreadLocal<IList> TO_UPDATE = ThreadLocal.withInitial(IArrayList::new);
    private static final ThreadLocal<DirectionList> DIRECTION_LIST = ThreadLocal.withInitial(DirectionList::new);
    private static final ThreadLocal<ChunkHolder> HOLDER = ThreadLocal.withInitial(ChunkHolder::new);
    @Shadow
    @Final
    Level level;

    public LevelChunkMixin(ChunkPos p_187621_,
                           UpgradeData p_187622_,
                           LevelHeightAccessor p_187623_,
                           Registry<Biome> p_187624_,
                           long p_187625_,
                           @Nullable LevelChunkSection[] p_187626_,
                           @Nullable BlendingData p_187627_) {
        super(p_187621_, p_187622_, p_187623_, p_187624_, p_187625_, p_187626_, p_187627_);
    }

    @Shadow
    public abstract void addAndRegisterBlockEntity(BlockEntity pBlockEntity);

    @Shadow
    @javax.annotation.Nullable
    public abstract BlockEntity getBlockEntity(BlockPos pPos, LevelChunk.EntityCreationType pCreationType);

    /**
     * @author TheGreatWolf
     * @reason Overwrite to implement new fluid system.
     */
    @Override
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        int bx = pos.getX();
        int by = pos.getY();
        int bz = pos.getZ();
        try {
            int i = this.getSectionIndex(by);
            if (i >= 0 && i < this.sections.length) {
                LevelChunkSection chunksection = this.sections[i];
                if (!chunksection.hasOnlyAir()) {
                    return chunksection.getBlockState(bx & 15, by & 15, bz & 15).getFluidState();
                }
            }
            return Fluids.EMPTY.defaultFluidState();
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting fluid state");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
            crashreportcategory.setDetail("Location", () -> CrashReportCategory.formatLocation(this, bx, by, bz));
            throw new ReportedException(crashreport);
        }
    }

    /**
     * This implementation avoids iterating over empty chunk sections and uses direct access to read out block states
     * instead. Instead of allocating a BlockPos for every block in the chunk, they're now only allocated once we find
     * a light source.
     *
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Override
    @Overwrite
    public Stream<BlockPos> getLights() {
        OList<BlockPos> list = new OArrayList<>();
        int minX = this.chunkPos.getMinBlockX();
        int minZ = this.chunkPos.getMinBlockZ();
        for (LevelChunkSection section : this.sections) {
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            int startY = section.bottomBlockY();
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (state.getLightEmission() != 0) {
                            //noinspection ObjectAllocationInLoop
                            list.add(new BlockPos(minX + x, startY + y, minZ + z));
                        }
                    }
                }
            }
        }
        if (list.isEmpty()) {
            return Stream.empty();
        }
        return list.stream();
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;" +
                     "Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At("TAIL"))
    private void onInit(ServerLevel level, ProtoChunk chunk, @Nullable LevelChunk.PostLoadProcessor processor, CallbackInfo ci) {
        this.primeAtm();
    }

    private void primeAtm() {
        Heightmap heightmap = this.heightmaps.get(Heightmap.Types.WORLD_SURFACE);
        IList toUpdate = TO_UPDATE.get();
        toUpdate.clear();
        int deepestY = Integer.MAX_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int maxY = heightmap.getHighestTaken(x, z);
                if (deepestY > maxY + 1) {
                    deepestY = maxY + 1;
                }
                int atm = 0;
                for (int y = maxY; y >= this.getMinBuildHeight(); y--) {
                    int index = this.getSectionIndex(y);
                    if (index < 0 || index >= this.sections.length) {
                        break;
                    }
                    int localY = y & 15;
                    LevelChunkSection section = this.sections[index];
                    BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, localY, z);
                    if (state.isAir()) {
                        if (atm != 0) {
                            ((ILevelChunkSectionPatch) section).getAtmStorage().set(x, localY, z, 63);
                            toUpdate.add(IAir.packInternalPos(x, y, z));
                        }
                    }
                    else if (state.getBlock() instanceof IAir air) {
                        if (air.allowsFrom(state, Direction.UP)) {
                            if (atm != 0) {
                                ((ILevelChunkSectionPatch) section).getAtmStorage().set(x, localY, z, 63);
                                toUpdate.add(IAir.packInternalPos(x, y, z));
                            }
                            else if (!air.allowsFrom(state, Direction.DOWN)) {
                                atm = 63;
                            }
                        }
                        else {
                            atm = 63;
                            ((ILevelChunkSectionPatch) section).getAtmStorage().set(x, localY, z, 63);
                            toUpdate.add(IAir.packInternalPos(x, y, z));
                        }
                    }
                    else {
                        atm = 63;
                        ((ILevelChunkSectionPatch) section).getAtmStorage().set(x, localY, z, 63);
                    }
                    if (atm == 0 && deepestY > y) {
                        deepestY = y;
                    }
                }
            }
        }
        this.updateShallowAtm(deepestY, toUpdate);
    }

    private int safeGetAtm(LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            //Accessing beyond the border horizontally
            Direction dir = x < 0 ? Direction.WEST : x > 15 ? Direction.EAST : z < 0 ? Direction.NORTH : Direction.SOUTH;
            LevelChunk chunk = holder.getHeld(dir);
            assert chunk != null : "Chunk to the " + dir + " is null, how did you access it in the first place?";
            section = chunk.getSection(index);
        }
        else if (y < 0) {
            assert index - 1 >= 0 : "Accessing into the inferior void";
            section = this.sections[index - 1];
        }
        else if (y > 15) {
            assert index + 1 < this.sections.length : "Accessing into the superior void";
            section = this.sections[index + 1];
        }
        return ((ILevelChunkSectionPatch) section).getAtmStorage().get(x & 15, y & 15, z & 15);
    }

    private BlockState safeGetBlockstate(LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            //Accessing beyond the border horizontally
            Direction dir = x < 0 ? Direction.WEST : x > 15 ? Direction.EAST : z < 0 ? Direction.NORTH : Direction.SOUTH;
            holder.setupIfNeeded(this.level, this.chunkPos, dir);
            LevelChunk chunk = holder.getHeld(dir);
            if (chunk == null) {
                return Blocks.BEDROCK.defaultBlockState();
            }
            section = chunk.getSection(index);
        }
        else if (y < 0) {
            if (index - 1 < 0) {
                //Accessing into the inferior void
                return Blocks.BEDROCK.defaultBlockState();
            }
            section = this.sections[index - 1];
        }
        else if (y > 15) {
            if (index + 1 >= this.sections.length) {
                //Accessing into the superior void
                return Blocks.BEDROCK.defaultBlockState();
            }
            section = this.sections[index + 1];
        }
        if (section.hasOnlyAir()) {
            return Blocks.AIR.defaultBlockState();
        }
        return section.getBlockState(x & 15, y & 15, z & 15);
    }

    /**
     * @author TheGreatWolf
     * @reason Handle atm when blocks are placed
     */
    @javax.annotation.Nullable
    @Override
    @Nullable
    @Overwrite
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        int globalY = pos.getY();
        LevelChunkSection section = this.getSection(this.getSectionIndex(globalY));
        boolean hadOnlyAir = section.hasOnlyAir();
        if (hadOnlyAir && state.isAir()) {
            return null;
        }
        int x = pos.getX() & 15;
        int y = globalY & 15;
        int z = pos.getZ() & 15;
        BlockState oldState = section.setBlockState(x, y, z, state);
        if (oldState == state) {
            return null;
        }
        Block newBlock = state.getBlock();
        this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(x, globalY, z, state);
        this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(x, globalY, z, state);
        this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(x, globalY, z, state);
        this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(x, globalY, z, state);
        boolean hasOnlyAir = section.hasOnlyAir();
        if (hadOnlyAir != hasOnlyAir) {
            this.level.getChunkSource().getLightEngine().updateSectionStatus(pos, hasOnlyAir);
        }
        boolean hadTE = oldState.hasBlockEntity();
        if (!this.level.isClientSide) {
            oldState.onRemove(this.level, pos, state, isMoving);
        }
        else if ((!oldState.is(newBlock) || !state.hasBlockEntity()) && hadTE) {
            this.removeBlockEntity(pos);
        }
        if (!section.getBlockState(x, y, z).is(newBlock)) {
            //Idk when this could happen
            return null;
        }
        if (!this.level.isClientSide && !this.level.captureBlockSnapshots) {
            state.onPlace(this.level, pos, oldState, isMoving);
        }
        if (state.hasBlockEntity()) {
            BlockEntity tile = this.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
            if (tile == null) {
                tile = ((EntityBlock) newBlock).newBlockEntity(pos, state);
                if (tile != null) {
                    this.addAndRegisterBlockEntity(tile);
                }
            }
            else {
                tile.setBlockState(state);
                this.updateBlockEntityTicker(tile);
            }
        }
        if (!this.level.isClientSide) {
            IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(this, CapabilityChunkStorage.INSTANCE);
            chunkStorage.scheduleAtmTick((LevelChunk) (Object) this, x, globalY, z, newBlock instanceof IAir || oldState.getBlock() instanceof IAir);
        }
        this.unsaved = true;
        return oldState;
    }

    private void updateAtmFurther(IList toUpdate, ChunkHolder holder, DirectionList list) {
        while (!toUpdate.isEmpty()) {
            int len = toUpdate.size();
            for (int i = 0; i < len; i++) {
                int pos = toUpdate.getInt(i);
                int globalY = IAir.unpackY(pos);
                int index = this.getSectionIndex(globalY);
                if (index < 0 || index >= this.sections.length) {
                    continue;
                }
                LevelChunkSection section = this.sections[index];
                int x = IAir.unpackX(pos);
                int y = globalY & 15;
                int z = IAir.unpackZ(pos);
                int oldAtm = this.safeGetAtm(section, holder, x, y, z, index);
                if (oldAtm == 0) {
                    continue;
                }
                BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, y, z);
                boolean isAir = state.isAir();
                IAir air = null;
                if (!isAir) {
                    if (state.getBlock() instanceof IAir a) {
                        air = a;
                    }
                    else {
                        continue;
                    }
                }
                int lowestAtm = oldAtm;
                list.clear();
                for (Direction dir : DirectionUtil.ALL) {
                    if (!isAir && !air.allowsFrom(state, dir)) {
                        continue;
                    }
                    int x1 = x + dir.getStepX();
                    int y1 = y + dir.getStepY();
                    int z1 = z + dir.getStepZ();
                    BlockState stateAtDir = this.safeGetBlockstate(section, holder, x1, y1, z1, index);
                    if (stateAtDir.isAir() || stateAtDir.getBlock() instanceof IAir a && a.allowsFrom(stateAtDir, dir.getOpposite())) {
                        list.add(dir);
                        int atm = this.safeGetAtm(section, holder, x1, y1, z1, index);
                        if (isAir) {
                            ++atm;
                        }
                        else {
                            atm += air.increment(state, dir);
                        }
                        if (lowestAtm > atm) {
                            lowestAtm = atm;
                        }
                    }
                }
                if (lowestAtm < oldAtm) {
                    ((ILevelChunkSectionPatch) section).getAtmStorage().set(x, y, z, lowestAtm);
                    while (!list.isEmpty()) {
                        Direction dir = list.getLastAndRemove();
                        int x1 = x + dir.getStepX();
                        int z1 = z + dir.getStepZ();
                        if (0 <= x1 && x1 < 16 && 0 <= z1 && z1 < 16) {
                            toUpdate.add(IAir.packInternalPos(x1, globalY + dir.getStepY(), z1));
                        }
                        else {
                            LevelChunk chunk = holder.getHeld(dir);
                            //noinspection ObjectAllocationInLoop
                            assert chunk != null : "Chunk at the " + dir + " is null, how did you access it in the first place, then?";
                            IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(chunk, CapabilityChunkStorage.INSTANCE);
                            chunkStorage.scheduleAtmTick(chunk, x1, globalY, z1, false);
                        }
                    }
                }
            }
            toUpdate.removeElements(0, len);
        }
    }

    @Shadow
    protected abstract <T extends BlockEntity> void updateBlockEntityTicker(T pBlockEntity);

    private void updateShallowAtm(int deepestY, IList toUpdate) {
        DirectionList list = DIRECTION_LIST.get();
        ChunkHolder holder = HOLDER.get();
        holder.reset();
        int len = toUpdate.size();
        for (int i = 0; i < len; i++) {
            int pos = toUpdate.getInt(i);
            int x = IAir.unpackX(pos);
            int globalY = IAir.unpackY(pos);
            int z = IAir.unpackZ(pos);
            if (globalY < deepestY) {
                if (x != 0 && x != 15 && z != 0 && z != 15) {
                    continue;
                }
            }
            int index = this.getSectionIndex(globalY);
            if (index < 0 || index >= this.sections.length) {
                continue;
            }
            LevelChunkSection section = this.sections[index];
            int y = globalY & 15;
            BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, y, z);
            boolean isAir = state.isAir();
            IAir air = null;
            if (!isAir) {
                if (state.getBlock() instanceof IAir a) {
                    air = a;
                }
                else {
                    continue;
                }
            }
            int lowestAtm = 63;
            list.clear();
            for (Direction dir : DirectionUtil.ALL) {
                if (!isAir && !air.allowsFrom(state, dir)) {
                    continue;
                }
                int x1 = x + dir.getStepX();
                int y1 = y + dir.getStepY();
                int z1 = z + dir.getStepZ();
                BlockState stateAtDir = this.safeGetBlockstate(section, holder, x1, y1, z1, index);
                if (stateAtDir.isAir() || stateAtDir.getBlock() instanceof IAir a && a.allowsFrom(stateAtDir, dir.getOpposite())) {
                    list.add(dir);
                    int atm = this.safeGetAtm(section, holder, x1, y1, z1, index);
                    if (isAir) {
                        ++atm;
                    }
                    else {
                        atm += air.increment(state, dir);
                    }
                    if (lowestAtm > atm) {
                        lowestAtm = atm;
                    }
                }
            }
            if (lowestAtm < 63) {
                ((ILevelChunkSectionPatch) section).getAtmStorage().set(x, y, z, lowestAtm);
                while (!list.isEmpty()) {
                    Direction dir = list.getLastAndRemove();
                    int x1 = x + dir.getStepX();
                    int z1 = z + dir.getStepZ();
                    if (0 <= x1 && x1 < 16 && 0 <= z1 && z1 < 16) {
                        toUpdate.add(IAir.packInternalPos(x1, globalY + dir.getStepY(), z1));
                    }
                    else {
                        LevelChunk chunk = holder.getHeld(dir);
                        //noinspection ObjectAllocationInLoop
                        assert chunk != null : "Chunk at the " + dir + " is null, how did you access it in the first place, then?";
                        IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(chunk, CapabilityChunkStorage.INSTANCE);
                        chunkStorage.scheduleAtmTick(chunk, x1, globalY, z1, false);
                    }
                }
            }
        }
        toUpdate.removeElements(0, len);
        this.updateAtmFurther(toUpdate, holder, list);
    }
}
