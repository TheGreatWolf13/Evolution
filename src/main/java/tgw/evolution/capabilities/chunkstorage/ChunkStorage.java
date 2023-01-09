package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IAir;
import tgw.evolution.commands.CommandAtm;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.patches.ILevelChunkSectionPatch;
import tgw.evolution.util.ChunkHolder;
import tgw.evolution.util.collection.IArrayList;
import tgw.evolution.util.collection.IList;
import tgw.evolution.util.collection.LArrayList;
import tgw.evolution.util.collection.LList;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;

public class ChunkStorage implements IChunkStorage {

    private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private static final ThreadLocal<ChunkHolder> HOLDER = ThreadLocal.withInitial(ChunkHolder::new);
    private static final ThreadLocal<DirectionList> DIRECTION_LIST = ThreadLocal.withInitial(DirectionList::new);
    private final IList pendingAtmTicks = new IArrayList();
    private final LList pendingBlockTicks = new LArrayList();
    private boolean continuousAtmDebug;
    private byte updateTicks;

    private static int safeGetAtm(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            //Accessing beyond the border horizontally
            Direction dir = x < 0 ? Direction.WEST : x > 15 ? Direction.EAST : z < 0 ? Direction.NORTH : Direction.SOUTH;
            LevelChunk held = holder.getHeld(dir);
            assert held != null : "Chunk to the " + dir + " is null, how did you access it in the first place?";
            section = held.getSection(index);
        }
        else if (y < 0) {
            assert index - 1 >= 0 : "Accessing into the inferior void";
            section = chunk.getSection(index - 1);
        }
        else if (y > 15) {
            assert index + 1 < chunk.getSections().length : "Accessing into the superior void";
            section = chunk.getSection(index + 1);
        }
        return ((ILevelChunkSectionPatch) section).getAtmStorage().get(x & 15, y & 15, z & 15);
    }

    private static BlockState safeGetBlockstate(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            //Accessing beyond the border horizontally
            Direction dir = x < 0 ? Direction.WEST : x > 15 ? Direction.EAST : z < 0 ? Direction.NORTH : Direction.SOUTH;
            holder.setupIfNeeded(chunk.getLevel(), chunk.getPos(), dir);
            LevelChunk held = holder.getHeld(dir);
            if (held == null) {
                return Blocks.BEDROCK.defaultBlockState();
            }
            section = held.getSection(index);
        }
        else if (y < 0) {
            if (index - 1 < 0) {
                //Accessing into the inferior void
                return Blocks.BEDROCK.defaultBlockState();
            }
            section = chunk.getSection(index - 1);
        }
        else if (y > 15) {
            if (index + 1 >= chunk.getSections().length) {
                //Accessing into the superior void
                return Blocks.BEDROCK.defaultBlockState();
            }
            section = chunk.getSection(index + 1);
        }
        if (section.hasOnlyAir()) {
            return Blocks.AIR.defaultBlockState();
        }
        return section.getBlockState(x & 15, y & 15, z & 15);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        long[] pendingBlockTicks = nbt.getLongArray("PendingBlockTicks");
        this.pendingBlockTicks.size(pendingBlockTicks.length);
        this.pendingBlockTicks.setElements(pendingBlockTicks);
        int[] pendingAtmTicks = nbt.getIntArray("PendingAtmTicks");
        this.pendingAtmTicks.size(pendingAtmTicks.length);
        this.pendingAtmTicks.setElements(pendingAtmTicks);
        this.continuousAtmDebug = nbt.getBoolean("ContinuousAtmDebug");
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    @Override
    public void scheduleAtmTick(LevelChunk chunk, int internalPos) {
        this.pendingAtmTicks.add(internalPos);
        chunk.setUnsaved(true);
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    @Override
    public void scheduleBlockTick(LevelChunk chunk, long pos) {
        this.pendingBlockTicks.add(pos);
        chunk.setUnsaved(true);
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (!this.pendingBlockTicks.isEmpty()) {
            nbt.putLongArray("PendingBlockTicks", this.pendingBlockTicks.toLongArray());
        }
        if (!this.pendingAtmTicks.isEmpty()) {
            nbt.putIntArray("PendingAtmTicks", this.pendingAtmTicks.toIntArray());
        }
        if (this.continuousAtmDebug) {
            nbt.putBoolean("ContinuousAtmDebug", true);
        }
        return nbt;
    }

    private void setAndUpdate(LevelChunk chunk,
                              LevelChunkSection section,
                              ChunkHolder holder,
                              int x,
                              int y,
                              int z,
                              int atm,
                              int globalY,
                              @Nullable DirectionList list) {
        ((ILevelChunkSectionPatch) section).getAtmStorage().set(x, y, z, atm);
        chunk.setUnsaved(true);
        if (list == null) {
            for (Direction dir : DirectionUtil.ALL) {
                this.update(chunk, holder, x, globalY, z, dir);
            }
        }
        else {
            while (!list.isEmpty()) {
                this.update(chunk, holder, x, globalY, z, list.getLastAndRemove());
            }
        }
    }

    @Override
    public boolean setContinuousAtmDebug(LevelChunk chunk, boolean debug) {
        boolean old = this.continuousAtmDebug;
        this.continuousAtmDebug = debug;
        if (old && !debug) {
            CommandAtm.fill(chunk, CommandAtm.ATM, false, CommandAtm.AIR_MAKER);
        }
        return old != debug;
    }

    @Override
    public void tick(LevelChunk chunk) {
        if (chunk.getLevel().isClientSide) {
            return;
        }
        ServerLevel level = (ServerLevel) chunk.getLevel();
        BlockPos.MutableBlockPos mutablePos = MUTABLE_POS.get();
        int len = this.pendingBlockTicks.size();
        for (int i = 0; i < len; i++) {
            long pos = this.pendingBlockTicks.getLong(i);
            BlockState state = level.getBlockState(mutablePos.set(pos));
            state.tick(level, mutablePos, level.random);
        }
        this.pendingBlockTicks.removeElements(0, len);
        ChunkHolder holder = HOLDER.get();
        holder.reset();
        len = this.pendingAtmTicks.size();
        for (int i = 0; i < len; i++) {
            this.updateAtm(chunk, holder, this.pendingAtmTicks.getInt(i));
        }
        this.pendingAtmTicks.removeElements(0, len);
        if (this.continuousAtmDebug) {
            if ((chunk.isUnsaved() || this.updateTicks > 0) && level.getGameTime() % 20 == 0) {
                CommandAtm.fill(chunk, CommandAtm.AIR, true, CommandAtm.ATM_MAKER);
            }
            if (this.updateTicks > 0) {
                --this.updateTicks;
            }
        }
    }

    private void update(LevelChunk chunk, ChunkHolder holder, int x, int globalY, int z, Direction dir) {
        int x1 = x + dir.getStepX();
        int z1 = z + dir.getStepZ();
        if (0 <= x1 && x1 < 16 && 0 <= z1 && z1 < 16) {
            this.scheduleAtmTick(chunk, x1, globalY + dir.getStepY(), z1, false);
        }
        else {
            holder.setupIfNeeded(chunk.getLevel(), chunk.getPos(), dir);
            LevelChunk held = holder.getHeld(dir);
            if (held != null) {
                IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(held, CapabilityChunkStorage.INSTANCE);
                chunkStorage.scheduleAtmTick(held, x1, globalY, z1, false);
            }
        }
    }

    private void updateAtm(LevelChunk chunk, ChunkHolder holder, int internalPos) {
        int globalY = IAir.unpackY(internalPos);
        int index = chunk.getSectionIndex(globalY);
        if (0 > index || index >= chunk.getSections().length) {
            return;
        }
        LevelChunkSection section = chunk.getSection(index);
        int x = IAir.unpackX(internalPos);
        int y = globalY & 15;
        int z = IAir.unpackZ(internalPos);
        int oldAtm = safeGetAtm(chunk, section, holder, x, y, z, index);
        BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, y, z);
        boolean isAir = state.isAir();
        IAir air = null;
        if (!isAir) {
            if (state.getBlock() instanceof IAir a) {
                air = a;
            }
            else {
                if (oldAtm != 63) {
                    this.setAndUpdate(chunk, section, holder, x, y, z, 63, globalY, null);
                }
                return;
            }
        }
        if (isAir || air.allowsFrom(state, Direction.UP)) {
            if (globalY == chunk.getMaxBuildHeight() - 1 || globalY > chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)) {
                if (oldAtm != 0 || IAir.unpackForceUpdate(internalPos)) {
                    this.setAndUpdate(chunk, section, holder, x, y, z, 0, globalY, null);
                }
                return;
            }
        }
        int lowestAtm = 63;
        DirectionList list = DIRECTION_LIST.get();
        list.clear();
        for (Direction dir : DirectionUtil.ALL) {
            if (!isAir && !air.allowsFrom(state, dir)) {
                continue;
            }
            int x1 = x + dir.getStepX();
            int y1 = y + dir.getStepY();
            int z1 = z + dir.getStepZ();
            BlockState stateAtDir = safeGetBlockstate(chunk, section, holder, x1, y1, z1, index);
            if (stateAtDir.isAir() || stateAtDir.getBlock() instanceof IAir a && a.allowsFrom(stateAtDir, dir.getOpposite())) {
                list.add(dir);
                int atm = safeGetAtm(chunk, section, holder, x1, y1, z1, index);
                if (dir != Direction.UP || atm != 0) {
                    if (isAir) {
                        ++atm;
                    }
                    else {
                        atm += air.increment(state, dir);
                    }
                }
                if (lowestAtm > atm) {
                    lowestAtm = atm;
                }
            }
        }
        if (IAir.unpackForceUpdate(internalPos)) {
            this.setAndUpdate(chunk, section, holder, x, y, z, lowestAtm, globalY, null);
        }
        else if (lowestAtm != oldAtm) {
            this.setAndUpdate(chunk, section, holder, x, y, z, lowestAtm, globalY, list);
        }
    }
}
