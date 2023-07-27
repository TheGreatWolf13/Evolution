package tgw.evolution.capabilities.chunk;

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
import tgw.evolution.util.ChunkHolder;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;

public class CapabilityChunkStorage {

    public static final CapabilityChunkStorage CLIENT = new CapabilityChunkStorage() {
        @Override
        public void deserializeNBT(CompoundTag nbt) {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public void scheduleAtmTick(LevelChunk chunk, int x, int y, int z, boolean forceUpdate) {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public void scheduleAtmTick(LevelChunk chunk, int internalPos) {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public void scheduleBlockTick(LevelChunk chunk, int x, int y, int z) {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public CompoundTag serializeNBT() {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public boolean setContinuousAtmDebug(LevelChunk chunk, boolean debug) {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public void tick(LevelChunk chunk) {
            throw new IllegalStateException("Should not be called on the client!");
        }
    };
    private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private static final ThreadLocal<ChunkHolder> HOLDER = ThreadLocal.withInitial(ChunkHolder::new);
    private static final ThreadLocal<DirectionList> DIRECTION_LIST = ThreadLocal.withInitial(DirectionList::new);
    private final IList pendingAtmTicks = new IArrayList();
    private final IList pendingBlockTicks = new IArrayList();
    private boolean continuousAtmDebug;
    private byte updateTicks;

    /**
     * Retrieves the atm value to be used during Atm Priming. Always call after
     * {@link CapabilityChunkStorage#safeGetBlockstate(LevelChunk, LevelChunkSection, ChunkHolder, int, int, int, int)}.
     * This method will not make safety checks, as
     * {@linkplain CapabilityChunkStorage#safeGetBlockstate(LevelChunk, LevelChunkSection, ChunkHolder, int, int, int, int) safeGetBlockState}
     * already did them.
     */
    public static int safeGetAtm(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index) {
        Direction dir = holder.getRememberedDir();
        if (dir == null) {
            return section.getAtmStorage().get(x, y, z);
        }
        switch (dir) {
            case EAST, WEST -> {
                //Accessing beyond the border horizontally
                LevelChunk held = holder.getHeld(dir);
                assert held != null : "Chunk to the " + dir + " is null, how did you access it in the first place?";
                section = held.getSection(index);
                x &= 15;
            }
            case NORTH, SOUTH -> {
                //Accessing beyond the border horizontally
                LevelChunk held = holder.getHeld(dir);
                assert held != null : "Chunk to the " + dir + " is null, how did you access it in the first place?";
                section = held.getSection(index);
                z &= 15;
            }
            case DOWN -> {
                assert index - 1 >= 0 : "Accessing into the inferior void";
                section = chunk.getSection(index - 1);
                y &= 15;
            }
            case UP -> {
                assert index + 1 < chunk.getSections().length : "Accessing into the superior void";
                section = chunk.getSection(index + 1);
                y &= 15;
            }
        }
        return section.getAtmStorage().get(x, y, z);
    }

    /**
     * Retrieves the {@link BlockState} to be used during Atm Priming. When accessing invalid positions, will return {@link Blocks#BEDROCK}. When
     * accessing a neighbour chunk, if it doesn't exist, will also return {@link Blocks#BEDROCK}.
     */
    public static BlockState safeGetBlockstate(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index) {
        Direction dir = null;
        boolean horizontal = false;
        if (x < 0) {
            dir = Direction.WEST;
            x &= 15;
            horizontal = true;
        }
        else if (x > 15) {
            dir = Direction.EAST;
            x &= 15;
            horizontal = true;
        }
        else if (z < 0) {
            dir = Direction.NORTH;
            z &= 15;
            horizontal = true;
        }
        else if (z > 15) {
            dir = Direction.SOUTH;
            z &= 15;
            horizontal = true;
        }
        else if (y < 0) {
            if (index - 1 < 0) {
                //Accessing into the inferior void
                return Blocks.BEDROCK.defaultBlockState();
            }
            dir = Direction.DOWN;
            y &= 15;
            section = chunk.getSection(index - 1);
        }
        else if (y > 15) {
            if (index + 1 >= chunk.getSections().length) {
                //Accessing into the superior void
                return Blocks.BEDROCK.defaultBlockState();
            }
            dir = Direction.UP;
            y &= 15;
            section = chunk.getSection(index + 1);
        }
        if (horizontal) {
            //Accessing beyond the border horizontally
            holder.setupIfNeeded(chunk.getLevel(), chunk.getPos(), dir);
            LevelChunk held = holder.getHeld(dir);
            if (held == null) {
                return Blocks.BEDROCK.defaultBlockState();
            }
            section = held.getSection(index);
        }
        holder.rememberLastDir(dir);
        if (section.hasOnlyAir()) {
            return Blocks.AIR.defaultBlockState();
        }
        return section.getBlockState(x, y, z);
    }

    public void deserializeNBT(CompoundTag nbt) {
        int[] pendingBlockTicks = nbt.getIntArray("PendingBlockTicks");
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

    public void scheduleAtmTick(LevelChunk chunk, int x, int y, int z, boolean forceUpdate) {
        this.scheduleAtmTick(chunk, IAir.packInternalPos(x & 15, y, z & 15, forceUpdate));
    }

    public void scheduleAtmTick(LevelChunk chunk, int internalPos) {
        this.pendingAtmTicks.add(internalPos);
        chunk.setUnsaved(true);
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    public void scheduleBlockTick(LevelChunk chunk, int x, int y, int z) {
        this.pendingBlockTicks.add(IAir.packInternalPos(x & 15, y, z & 15));
        chunk.setUnsaved(true);
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (!this.pendingBlockTicks.isEmpty()) {
            nbt.putIntArray("PendingBlockTicks", this.pendingBlockTicks.toIntArray());
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
        section.getAtmStorage().set(x, y, z, atm);
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

    public boolean setContinuousAtmDebug(LevelChunk chunk, boolean debug) {
        boolean old = this.continuousAtmDebug;
        this.continuousAtmDebug = debug;
        boolean changed = old != debug;
        if (changed) {
            if (debug) {
                CommandAtm.fill(chunk, CommandAtm.AIR, true, CommandAtm.ATM_MAKER);
            }
            else {
                CommandAtm.fill(chunk, CommandAtm.ATM, false, CommandAtm.AIR_MAKER);
            }
        }
        return changed;
    }

    public void tick(LevelChunk chunk) {
        ServerLevel level = (ServerLevel) chunk.getLevel();
        BlockPos.MutableBlockPos mutablePos = MUTABLE_POS.get();
        int len = this.pendingBlockTicks.size();
        for (int i = 0; i < len; i++) {
            int pos = this.pendingBlockTicks.getInt(i);
            int x = IAir.unpackX(pos);
            int y = IAir.unpackY(pos);
            int z = IAir.unpackZ(pos);
            BlockState state = level.getBlockState_(x, y, z);
            state.tick(level, mutablePos.set(x, y, z), level.random);
        }
        this.pendingBlockTicks.removeElements(0, len);
        ChunkHolder holder = HOLDER.get();
        len = this.pendingAtmTicks.size();
        for (int i = 0; i < len; i++) {
            this.updateAtm(chunk, holder, this.pendingAtmTicks.getInt(i));
        }
        holder.reset();
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
                held.getChunkStorage().scheduleAtmTick(held, x1, globalY, z1, false);
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
        int oldAtm = section.getAtmStorage().get(x, y, z);
        BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, y, z);
        boolean isAir = state.isAir();
        IAir air = null;
        if (!isAir) {
            if (state.getBlock() instanceof IAir a) {
                air = a;
            }
            else {
                if (oldAtm != 31) {
                    this.setAndUpdate(chunk, section, holder, x, y, z, 31, globalY, null);
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
        int lowestAtm = 31;
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