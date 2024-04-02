package tgw.evolution.capabilities.chunk;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import tgw.evolution.blocks.*;
import tgw.evolution.commands.CommandAtm;
import tgw.evolution.util.ChunkHolder;
import tgw.evolution.util.DirectionHolder;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.PropagationDirection;

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
        public void scheduleBlockTick(LevelChunk chunk, int x, int y, int z) {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public void scheduleIntegrityTick(LevelChunk chunk, int x, int y, int z, boolean oldFillable) {
            throw new IllegalStateException("Should not be called on the client!");
        }

        @Override
        public void schedulePreciseBlockTick(LevelChunk chunk, int x, int y, int z, int ticksInTheFuture) {
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

        @Override
        protected void scheduleAtmTick(LevelChunk chunk, int internalPos) {
            throw new IllegalStateException("Should not be called on the client!");
        }
    };
    private static final ThreadLocal<ChunkHolder> HOLDER = ThreadLocal.withInitial(ChunkHolder::new);
    private static final ThreadLocal<DirectionHolder> DIR_HOLDER = ThreadLocal.withInitial(DirectionHolder::new);
    private final ChunkAllowance allowance = new ChunkAllowance();
    private boolean continuousAtmDebug;
    private boolean needsSorting;
    private final IList pendingAtmTicks = new IArrayList();
    private final IList pendingBlockTicks = new IArrayList();
    private final IList pendingIntegrityTicks = new IArrayList();
    private final TickStorage pendingPreciseBlockTicks = new TickStorage();
    private byte updateTicks;

    /**
     * Retrieves the atm value to be used during Atm Priming. Always call after
     * {@link CapabilityChunkStorage#safeGetBlockstate(LevelChunk, LevelChunkSection, ChunkHolder, int, int, int, int, int)}.
     * This method will not make safety checks, as
     * {@linkplain CapabilityChunkStorage#safeGetBlockstate(LevelChunk, LevelChunkSection, ChunkHolder, int, int, int, int, int)}
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
    public static BlockState safeGetBlockstate(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index, int yOffset) {
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
        if (yOffset != 0) {
            y += yOffset;
            if (y < 0) {
                if (index - 1 < 0) {
                    //Accessing into the inferior void
                    return Blocks.BEDROCK.defaultBlockState();
                }
                section = chunk.getSection(--index);
                y &= 15;
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

    private static boolean canStabilize(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, IStructural structural, BlockState state, int x, int y, int z, int index, int selfLoad) {
        //Try to stabilize on the X axis
        BlockState stateW = safeGetBlockstate(chunk, section, holder, x - 1, y, z, index, 0);
        if (stateW.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, stateW)) {
            int loadW = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x - 1, y, z, index, 0));
            if (loadW <= selfLoad) {
                BlockState stateE = safeGetBlockstate(chunk, section, holder, x + 1, y, z, index, 0);
                if (stateE.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, stateE)) {
                    int loadE = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x + 1, y, z, index, 0));
                    if (loadE <= selfLoad) {
                        if (loadE < selfLoad || loadW < selfLoad) {
                            return true;
                        }
                    }
                }
            }
        }
        //Try to stabilize on the Z axis
        BlockState stateN = safeGetBlockstate(chunk, section, holder, x, y, z - 1, index, 0);
        if (stateN.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, stateN)) {
            int loadN = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x, y, z - 1, index, 0));
            if (loadN <= selfLoad) {
                BlockState stateS = safeGetBlockstate(chunk, section, holder, x, y, z + 1, index, 0);
                if (stateS.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, stateS)) {
                    int loadS = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x, y, z + 1, index, 0));
                    if (loadS <= selfLoad) {
                        return loadS < selfLoad || loadN < selfLoad;
                    }
                }
            }
        }
        return false;
    }

    private static boolean canStabilizeArch(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, IStructural structural, BlockState state, int x, int y, int z, int index, int selfLoad) {
        //Try to stabilize on the X axis
        for (int j = -1; j <= 1; ++j) {
            BlockState firstState = safeGetBlockstate(chunk, section, holder, x - 1, y, z, index, j);
            if (firstState.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, firstState)) {
                int loadW = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x - 1, y, z, index, j));
                if (loadW <= selfLoad) {
                    for (int i = -1; i <= 1; ++i) {
                        if (j == 1 && i == 1) {
                            continue;
                        }
                        BlockState secondState = safeGetBlockstate(chunk, section, holder, x + 1, y, z, index, i);
                        if (secondState.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, secondState)) {
                            int loadE = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x + 1, y, z, index, i));
                            if (loadE <= selfLoad) {
                                if (loadE < selfLoad || loadW < selfLoad) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        //Try to stabilize on the Z axis
        for (int j = -1; j <= 1; ++j) {
            BlockState firstState = safeGetBlockstate(chunk, section, holder, x, y, z - 1, index, j);
            if (firstState.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, firstState)) {
                int loadN = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x, y, z - 1, index, j));
                if (loadN <= selfLoad) {
                    for (int i = -1; i <= 1; ++i) {
                        if (j == 1 && i == 1) {
                            continue;
                        }
                        BlockState secondState = safeGetBlockstate(chunk, section, holder, x, y, z + 1, index, i);
                        if (secondState.getBlock() instanceof IFillable && structural.canMakeABeamWith(state, secondState)) {
                            int loadS = IFillable.getStableLoadFactor(safeGetStructuralData(chunk, section, holder, x, y, z + 1, index, i));
                            if (loadS <= selfLoad) {
                                return loadS < selfLoad || loadN < selfLoad;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void fail(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState_(x, y, z);
        if (state.getBlock() instanceof IStructural structural) {
            structural.fail(level, state, x, y, z);
        }
    }

    private static int safeGetStructuralData(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int index, int yOffset) {
        Direction dir = holder.getRememberedDir();
        if (yOffset != 0) {
            y += yOffset;
            if (y < 0) {
                if (index - 1 < 0) {
                    //Accessing into the inferior void
                    return 0;
                }
                section = chunk.getSection(--index);
                y &= 15;
            }
            else if (y > 15) {
                if (index + 1 < chunk.getSections().length) {
                    //Accessing into the superior void
                    return 0;
                }
                section = chunk.getSection(++index);
                y &= 15;
            }
        }
        if (dir == null) {
            return (section.getStabilityStorage().get(x, y, z) ? 1 << 31 : 0) | section.getLoadFactorStorage().get(x, y, z) << 16 | section.getIntegrityStorage().get(x, y, z);
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
                if (index - 1 < 0) {
                    //Accessing into the inferior void
                    return 0;
                }
                section = chunk.getSection(index - 1);
                y &= 15;
            }
            case UP -> {
                assert index + 1 < chunk.getSections().length : "Accessing into the superior void";
                section = chunk.getSection(index + 1);
                y &= 15;
            }
        }
        return (section.getStabilityStorage().get(x, y, z) ? 1 << 31 : 0) | section.getLoadFactorStorage().get(x, y, z) << 16 | section.getIntegrityStorage().get(x, y, z);
    }

    private static int verifyArch(BlockState state, IStructural structural, LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int index, int x, int y, int z, int selfIntegrity, int directionList, int incrementForBeam) {
        int load = 255;
        int integrity = selfIntegrity;
        float factor = (float) load / integrity;
        int invalidList = 0;
        while (!DirectionList.isEmpty(directionList)) {
            int dirIndex = DirectionList.getLast(directionList);
            Direction dir = DirectionList.get(directionList, dirIndex);
            directionList = DirectionList.remove(directionList, dirIndex);
            int x0 = x + dir.getStepX();
            int z0 = z + dir.getStepZ();
            BlockState otherState = safeGetBlockstate(chunk, section, holder, x0, y, z0, index, -1);
            if (otherState.getBlock() instanceof IStructural && structural.canMakeABeamWith(state, otherState)) {
                int data = safeGetStructuralData(chunk, section, holder, x0, y, z0, index, -1);
                int l = IFillable.getLoadFactor(data) + 2;
                int i = Math.min(IFillable.getIntegrity(data), selfIntegrity);
                float f = (float) l / i;
                if (f < factor) {
                    factor = f;
                    load = l;
                    integrity = i;
                }
            }
            else {
                invalidList = DirectionList.add(invalidList, dir);
            }
        }
        return verifySimpleBeam(state, structural, chunk, section, holder, invalidList, index, x, y, z, incrementForBeam, selfIntegrity, load, integrity);
    }

    private static int verifySimpleBeam(BlockState state, IStructural structural, LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int directionList, int index, int x, int y, int z, int increment, int selfIntegrity, int minLoad, int correspondingIntegrity) {
        int load = minLoad;
        int integrity = correspondingIntegrity;
        float factor = (float) load / integrity;
        while (!DirectionList.isEmpty(directionList)) {
            int dirIndex = DirectionList.getLast(directionList);
            Direction dir = DirectionList.get(directionList, dirIndex);
            directionList = DirectionList.remove(directionList, dirIndex);
            int x0 = x + dir.getStepX();
            int z0 = z + dir.getStepZ();
            BlockState otherState = safeGetBlockstate(chunk, section, holder, x0, y, z0, index, 0);
            if (otherState.getBlock() instanceof IStructural && structural.canMakeABeamWith(state, otherState)) {
                int data = safeGetStructuralData(chunk, section, holder, x0, y, z0, index, 0);
                int l = IFillable.getLoadFactor(data) + increment;
                int i = Math.min(IFillable.getIntegrity(data), selfIntegrity);
                float f = (float) l / i;
                if (f < factor) {
                    factor = f;
                    load = l;
                    integrity = i;
                }
            }
        }
        return load << 16 | integrity;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.allowance.deserializeNBT(NBTHelper.getCompoundOrEmpty(nbt, "Allowance"));
        int[] pendingIntegrityTicks = nbt.getIntArray("PendingIntegrityTicks");
        this.pendingIntegrityTicks.size(pendingIntegrityTicks.length);
        this.pendingIntegrityTicks.setElements(pendingIntegrityTicks);
        int[] pendingBlockTicks = nbt.getIntArray("PendingBlockTicks");
        this.pendingBlockTicks.size(pendingBlockTicks.length);
        this.pendingBlockTicks.setElements(pendingBlockTicks);
        int[] pendingAtmTicks = nbt.getIntArray("PendingAtmTicks");
        this.pendingAtmTicks.size(pendingAtmTicks.length);
        this.pendingAtmTicks.setElements(pendingAtmTicks);
        this.pendingPreciseBlockTicks.deserializeNbt(nbt.getCompound("PendingPreciseBlockTicks"));
        this.needsSorting = true;
        this.continuousAtmDebug = nbt.getBoolean("ContinuousAtmDebug");
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    public ChunkAllowance getAllowance() {
        return this.allowance;
    }

    public void scheduleAtmTick(LevelChunk chunk, int x, int y, int z, boolean forceUpdate) {
        this.scheduleAtmTick(chunk, IAir.packInternalPos(x & 15, y, z & 15, forceUpdate));
    }

    public void scheduleBlockTick(LevelChunk chunk, int x, int y, int z) {
        this.pendingBlockTicks.add(IAir.packInternalPos(x & 15, y, z & 15));
        chunk.setUnsaved(true);
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    public void scheduleIntegrityTick(LevelChunk chunk, int x, int y, int z, boolean oldFillable) {
        this.scheduleIntegrityTick(chunk, x, y, z, 0, PropagationDirection.NONE, oldFillable);
    }

    public void schedulePreciseBlockTick(LevelChunk chunk, int x, int y, int z, int ticksInTheFuture) {
        assert ticksInTheFuture > 0 : "Cannot schedule a tick to happen now or in the past!";
        this.needsSorting |= this.pendingPreciseBlockTicks.add(chunk.level.getGameTime() + ticksInTheFuture, IAir.packInternalPos(x & 15, y, z & 15));
        chunk.setUnsaved(true);
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("Allowance", this.allowance.serializeNBT());
        if (!this.pendingIntegrityTicks.isEmpty()) {
            nbt.putIntArray("PendingIntegrityTicks", this.pendingIntegrityTicks.toIntArray());
        }
        if (!this.pendingBlockTicks.isEmpty()) {
            nbt.putIntArray("PendingBlockTicks", this.pendingBlockTicks.toIntArray());
        }
        if (!this.pendingAtmTicks.isEmpty()) {
            nbt.putIntArray("PendingAtmTicks", this.pendingAtmTicks.toIntArray());
        }
        if (!this.pendingPreciseBlockTicks.isEmpty()) {
            nbt.put("PendingPreciseBlockTicks", this.pendingPreciseBlockTicks.serializeNbt());
        }
        if (this.continuousAtmDebug) {
            nbt.putBoolean("ContinuousAtmDebug", true);
        }
        return nbt;
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
        this.allowance.tick();
        this.processPreciseTicks(level, chunk);
        int len = this.pendingBlockTicks.size();
        if (len > 0) {
            for (int i = 0; i < len; ++i) {
                int pos = this.pendingBlockTicks.getInt(i);
                int x = IAir.unpackX(pos);
                int y = IAir.unpackY(pos);
                int z = IAir.unpackZ(pos);
                BlockState state = chunk.getBlockState_(x, y, z);
                state.tick_(level, x, y, z, level.random);
            }
            this.pendingBlockTicks.removeElements(0, len);
        }
        len = this.pendingIntegrityTicks.size();
        if (len > 0) {
            ChunkHolder holder = HOLDER.get();
            int maxSectionIndex = chunk.getSections().length;
            ChunkPos pos = chunk.getPos();
            int minX = pos.getMinBlockX();
            int minZ = pos.getMinBlockZ();
            for (int i = 0; i < len; ++i) {
                this.updatePhysics(level, chunk, holder, this.pendingIntegrityTicks.getInt(i), maxSectionIndex, minX, minZ);
            }
            holder.reset();
            this.pendingIntegrityTicks.removeElements(0, len);
        }
        len = this.pendingAtmTicks.size();
        if (len > 0) {
            ChunkHolder holder = HOLDER.get();
            DirectionHolder dirHolder = DIR_HOLDER.get();
            int maxSectionIndex = chunk.getSections().length;
            for (int i = 0; i < len; ++i) {
                this.updateAtm(chunk, holder, this.pendingAtmTicks.getInt(i), dirHolder, maxSectionIndex);
            }
            holder.reset();
            this.pendingAtmTicks.removeElements(0, len);
        }
        if (this.continuousAtmDebug) {
            if ((chunk.isUnsaved() || this.updateTicks > 0) && level.getGameTime() % 20 == 0) {
                CommandAtm.fill(chunk, CommandAtm.AIR, true, CommandAtm.ATM_MAKER);
            }
            if (this.updateTicks > 0) {
                --this.updateTicks;
            }
        }
    }

    protected void scheduleAtmTick(LevelChunk chunk, int internalPos) {
        this.pendingAtmTicks.add(internalPos);
        chunk.setUnsaved(true);
        if (this.continuousAtmDebug) {
            this.updateTicks = 40;
        }
    }

    private void failOrPropagate(LevelChunk chunk, ChunkHolder holder, LevelChunkSection section, int x, int y, int z, int index, int globalY, int failure, Block block, BlockState state, PropagationDirection propDir) {
        BlockState stateDown = safeGetBlockstate(chunk, section, holder, x, y - 1, z, index, 0);
        Block downBlock = stateDown.getBlock();
        if (downBlock instanceof IFillable) {
            int l = IFillable.getLoadFactor(safeGetStructuralData(chunk, section, holder, x, y - 1, z, index, 0));
            int selfIntegrity = block instanceof IStructural structural ? structural.getIntegrity(state) : 255;
            int downInt = downBlock instanceof IStructural s ? s.getIntegrity(stateDown) : 0;
            boolean isInLimit = failure > selfIntegrity;
            if (l <= section.getLoadFactorStorage().get(x, y, z) && !(isInLimit && (downInt > selfIntegrity || propDir != PropagationDirection.NONE && PropagationDirection.DOWN != propDir))) {
                this.schedulePhysicsUpdate(chunk, holder, x, globalY, z, failure, PropagationDirection.DOWN);
            }
            else {
                if (propDir == PropagationDirection.DOWN) {
                    fail(chunk.getLevel(), x + chunk.getPos().getMinBlockX(), globalY, z + chunk.getPos().getMinBlockZ());
                }
                else {
                    fail(chunk.getLevel(), x + chunk.getPos().getMinBlockX() - propDir.stepX, globalY - propDir.stepY, z + chunk.getPos().getMinBlockZ() - propDir.stepZ);
                }
            }
        }
        else if (block instanceof IStructural structural) {
            int selfIntegrity = structural.getIntegrity(state);
            switch (structural.getBeamType(state)) {
                case X_BEAM -> {
                    this.propagateFailureSimple(chunk, section, holder, state, structural, x, y, z, index, failure, selfIntegrity, DirectionList.X_AXIS, section.getLoadFactorStorage().get(x, y, z), globalY, propDir);
                }
                case Z_BEAM -> {
                    this.propagateFailureSimple(chunk, section, holder, state, structural, x, y, z, index, failure, selfIntegrity, DirectionList.Z_AXIS, section.getLoadFactorStorage().get(x, y, z), globalY, propDir);
                }
                case CARDINAL_BEAM -> {
                    this.propagateFailureSimple(chunk, section, holder, state, structural, x, y, z, index, failure, selfIntegrity, DirectionList.HORIZONTAL, section.getLoadFactorStorage().get(x, y, z), globalY, propDir);
                }
                case X_ARCH -> {
                    this.propagateFailureArch(chunk, section, holder, state, structural, x, y, z, index, failure, selfIntegrity, section.getLoadFactorStorage().get(x, y, z), globalY, DirectionList.X_AXIS);
                }
                case Z_ARCH -> {
                    this.propagateFailureArch(chunk, section, holder, state, structural, x, y, z, index, failure, selfIntegrity, section.getLoadFactorStorage().get(x, y, z), globalY, DirectionList.Z_AXIS);
                }
                case CARDINAL_ARCH -> {
                    this.propagateFailureArch(chunk, section, holder, state, structural, x, y, z, index, failure, selfIntegrity, section.getLoadFactorStorage().get(x, y, z), globalY, DirectionList.HORIZONTAL);
                }
            }
        }
    }

    private void processPreciseTicks(ServerLevel level, LevelChunk chunk) {
        if (!this.pendingPreciseBlockTicks.isEmpty()) {
            if (this.needsSorting) {
                this.needsSorting = false;
                this.pendingPreciseBlockTicks.sort();
            }
            int len = this.pendingPreciseBlockTicks.size();
            int i = len;
            long currentTick = chunk.level.getGameTime();
            while (this.pendingPreciseBlockTicks.getTick(--i) == currentTick) {
                int pos = this.pendingPreciseBlockTicks.getPos(i);
                int x = IAir.unpackX(pos);
                int y = IAir.unpackY(pos);
                int z = IAir.unpackZ(pos);
                BlockState state = chunk.getBlockState_(x, y, z);
                state.tick_(level, x, y, z, level.random);
                if (i == 0) {
                    this.pendingPreciseBlockTicks.clear();
                    return;
                }
            }
            this.pendingPreciseBlockTicks.removeElements(i + 1, len);
        }
    }

    private void propagateFailureArch(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, BlockState state, IStructural structural, int x, int y, int z, int index, int failure, int selfIntegrity, int minLoad, int globalY, int directionList) {
        int invalidList = 0;
        Direction failedDir = null;
        boolean down = false;
        boolean isInLimit = failure > selfIntegrity;
        while (!DirectionList.isEmpty(directionList)) {
            int dirIndex = DirectionList.getLast(directionList);
            Direction dir = DirectionList.get(directionList, dirIndex);
            directionList = DirectionList.remove(directionList, dirIndex);
            int x0 = x + dir.getStepX();
            int z0 = z + dir.getStepZ();
            BlockState otherState = safeGetBlockstate(chunk, section, holder, x0, y, z0, index, -1);
            if (otherState.getBlock() instanceof IStructural s && structural.canMakeABeamWith(state, otherState)) {
                int l = IFillable.getLoadFactor(safeGetStructuralData(chunk, section, holder, x0, y, z0, index, -1));
                if (l <= minLoad && !(isInLimit && (s.getIntegrity(otherState) > selfIntegrity || l == 0))) {
                    minLoad = l;
                    failedDir = dir;
                    down = true;
                }
            }
            else {
                invalidList = DirectionList.add(invalidList, dir);
            }
        }
        while (!DirectionList.isEmpty(invalidList)) {
            int dirIndex = DirectionList.getLast(invalidList);
            Direction dir = DirectionList.get(invalidList, dirIndex);
            invalidList = DirectionList.remove(invalidList, dirIndex);
            int x0 = x + dir.getStepX();
            int z0 = z + dir.getStepZ();
            BlockState otherState = safeGetBlockstate(chunk, section, holder, x0, y, z0, index, 0);
            if (otherState.getBlock() instanceof IStructural s && structural.canMakeABeamWith(state, otherState)) {
                int l = IFillable.getLoadFactor(safeGetStructuralData(chunk, section, holder, x0, y, z0, index, 0));
                if (l <= minLoad && !(isInLimit && (s.getIntegrity(otherState) > selfIntegrity || l == 0))) {
                    minLoad = l;
                    failedDir = dir;
                    down = false;
                }
            }
        }
        if (failedDir != null) {
            this.schedulePhysicsUpdate(chunk, holder, x, globalY, z, failure, down ? PropagationDirection.fromDirectionOffsetDown(failedDir) : PropagationDirection.fromDirection(failedDir));
        }
        else {
            Level level = chunk.getLevel();
            int globalX = x + chunk.getPos().getMinBlockX();
            int globalZ = z + chunk.getPos().getMinBlockZ();
            structural.fail(level, state, globalX, globalY, globalZ);
        }
    }

    private void propagateFailureSimple(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, BlockState state, IStructural structural, int x, int y, int z, int index, int failure, int selfIntegrity, int directionList, int minLoad, int globalY, PropagationDirection propDir) {
        Direction failedDir = null;
        boolean isInLimit = failure > selfIntegrity;
        while (!DirectionList.isEmpty(directionList)) {
            int dirIndex = DirectionList.getLast(directionList);
            Direction dir = DirectionList.get(directionList, dirIndex);
            directionList = DirectionList.remove(directionList, dirIndex);
            int x0 = x + dir.getStepX();
            int z0 = z + dir.getStepZ();
            BlockState otherState = safeGetBlockstate(chunk, section, holder, x0, y, z0, index, 0);
            if (otherState.getBlock() instanceof IStructural s && structural.canMakeABeamWith(state, otherState)) {
                int l = IFillable.getLoadFactor(safeGetStructuralData(chunk, section, holder, x0, y, z0, index, 0));
                if (l <= minLoad && !(isInLimit && (s.getIntegrity(otherState) > selfIntegrity || propDir != PropagationDirection.NONE && PropagationDirection.fromDirection(dir) != propDir))) {
                    minLoad = l;
                    failedDir = dir;
                }
            }
        }
        if (failedDir != null) {
            this.schedulePhysicsUpdate(chunk, holder, x, globalY, z, failure, PropagationDirection.fromDirection(failedDir));
        }
        else {
            ChunkPos pos = chunk.getPos();
            if (propDir != PropagationDirection.DOWN) {
                fail(chunk.getLevel(), x + pos.getMinBlockX() - propDir.stepX, globalY - propDir.stepY, z + pos.getMinBlockZ() - propDir.stepZ);
            }
            else {
                structural.fail(chunk.getLevel(), state, x + pos.getMinBlockX(), globalY, z + pos.getMinBlockZ());
            }
        }
    }

    private void scheduleAtmUpdate(LevelChunk chunk, ChunkHolder holder, int x, int globalY, int z, Direction dir) {
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

    private void scheduleIntegrityTick(LevelChunk chunk, int x, int y, int z, int failure, PropagationDirection propDir, boolean oldFillable) {
        this.pendingIntegrityTicks.add(IFillable.packInternalPos(x, y, z, failure, propDir, oldFillable));
        chunk.setUnsaved(true);
    }

    private void schedulePhysicsUpdate(LevelChunk chunk, ChunkHolder holder, int x, int y, int z, int failure, PropagationDirection dir) {
        int x1 = x + dir.stepX;
        int z1 = z + dir.stepZ;
        if (0 <= x1 && x1 < 16 && 0 <= z1 && z1 < 16) {
            this.scheduleIntegrityTick(chunk, x1, y + dir.stepY, z1, failure, dir, false);
        }
        else {
            Direction horizontal = dir.getClosestHorizontal();
            assert horizontal != null;
            holder.setupIfNeeded(chunk.getLevel(), chunk.getPos(), horizontal);
            LevelChunk held = holder.getHeld(horizontal);
            if (held != null) {
                held.getChunkStorage().scheduleIntegrityTick(held, x1 & 15, y + dir.stepY, z1 & 15, failure, dir, false);
            }
        }
    }

    private void setAndUpdate(LevelChunk chunk, LevelChunkSection section, ChunkHolder holder, int x, int y, int z, int atm, int globalY, int directionList) {
        section.getAtmStorage().set(x, y, z, atm);
        chunk.setUnsaved(true);
        if (directionList == DirectionList.NULL) {
            for (Direction dir : DirectionUtil.ALL) {
                this.scheduleAtmUpdate(chunk, holder, x, globalY, z, dir);
            }
        }
        else {
            while (!DirectionList.isEmpty(directionList)) {
                int index = DirectionList.getLast(directionList);
                Direction dir = DirectionList.get(directionList, index);
                directionList = DirectionList.remove(directionList, index);
                this.scheduleAtmUpdate(chunk, holder, x, globalY, z, dir);
            }
        }
    }

    private void updateAtm(LevelChunk chunk, ChunkHolder holder, int internalPos, DirectionHolder dirHolder, int maxSectionIndex) {
        int globalY = IAir.unpackY(internalPos);
        int index = chunk.getSectionIndex(globalY);
        if (0 > index || index >= maxSectionIndex) {
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
                    this.setAndUpdate(chunk, section, holder, x, y, z, 31, globalY, oldAtm == 0 ? DirectionList.ALL_EXCEPT_UP : DirectionList.NULL);
                }
                return;
            }
        }
        if (isAir || air.allowsFrom(state, Direction.UP)) {
            if (globalY == chunk.getMaxBuildHeight() - 1 || globalY > chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)) {
                if (oldAtm != 0 || IAir.unpackForceUpdate(internalPos)) {
                    this.setAndUpdate(chunk, section, holder, x, y, z, 0, globalY, DirectionList.ALL_EXCEPT_UP);
                }
                return;
            }
        }
        dirHolder.reset();
        for (Direction dir : DirectionUtil.ALL) {
            if (!isAir && !air.allowsFrom(state, dir)) {
                continue;
            }
            int x1 = x + dir.getStepX();
            int y1 = y + dir.getStepY();
            int z1 = z + dir.getStepZ();
            BlockState stateAtDir = safeGetBlockstate(chunk, section, holder, x1, y1, z1, index, 0);
            if (stateAtDir.isAir() || stateAtDir.getBlock() instanceof IAir a && a.allowsFrom(stateAtDir, dir.getOpposite())) {
                if (oldAtm == 0) {
                    if (dir == Direction.DOWN) {
                        dirHolder.store(Direction.DOWN, 31);
                        continue;
                    }
                }
                int atm = safeGetAtm(chunk, section, holder, x1, y1, z1, index);
                if (dir != Direction.UP || atm != 0) {
                    if (isAir) {
                        ++atm;
                    }
                    else {
                        atm += air.increment(state, dir);
                    }
                }
                dirHolder.store(dir, atm);
            }
        }
        int lowestAtm = dirHolder.getLowestAtm();
        if (IAir.unpackForceUpdate(internalPos)) {
            this.setAndUpdate(chunk, section, holder, x, y, z, lowestAtm, globalY, DirectionList.NULL);
        }
        else if (lowestAtm != oldAtm) {
            this.setAndUpdate(chunk, section, holder, x, y, z, lowestAtm, globalY, dirHolder.computeList(oldAtm == 0));
        }
    }

    private void updatePhysics(ServerLevel level, LevelChunk chunk, ChunkHolder holder, int internalPos, int maxSectionIndex, int minX, int minZ) {
        int globalY = IFillable.unpackY(internalPos);
        int index = chunk.getSectionIndex(globalY);
        if (0 > index || index >= maxSectionIndex) {
            return;
        }
        LevelChunkSection section = chunk.getSection(index);
        int x = IFillable.unpackX(internalPos);
        int y = globalY & 15;
        int z = IFillable.unpackZ(internalPos);
        int failure = IFillable.unpackFailure(internalPos);
        BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, y, z);
        Block block = state.getBlock();
        if (failure != 0) {
            this.failOrPropagate(chunk, holder, section, x, y, z, index, globalY, failure, block, state, IFillable.unpackPropDir(internalPos));
            return;
        }
        //Propagate Load Factor and Integrity
        IntegrityStorage loadFactorStorage = section.getLoadFactorStorage();
        IntegrityStorage integrityStorage = section.getIntegrityStorage();
        StabilityStorage stabilityStorage = section.getStabilityStorage();
        if (block instanceof IFillable) {
            int oldIntegrity = integrityStorage.get(x, y, z);
            int newIntegrity = oldIntegrity;
            int oldLoad = loadFactorStorage.get(x, y, z);
            int newLoad = oldLoad;
            boolean oldStable = stabilityStorage.get(x, y, z);
            boolean newStable = oldStable;
            if (block instanceof IStable stable) {
                newLoad = 0;
                newIntegrity = stable.getIntegrity(state);
                newStable = false;
            }
            else {
                BlockState stateDown = safeGetBlockstate(chunk, section, holder, x, y - 1, z, index, 0);
                if (stateDown.getBlock() instanceof IFillable) {
                    if (block instanceof ISloppable sloppable && sloppable.slopeLogic(level, minX + x, globalY, minZ + z)) {
                        return;
                    }
                    int data = safeGetStructuralData(chunk, section, holder, x, y - 1, z, index, 0);
                    int intDown = IFillable.getIntegrity(data);
                    newIntegrity = block instanceof IStructural structural ? Math.min(intDown, structural.getIntegrity(state)) : intDown;
                    int loadDown = IFillable.getStableLoadFactor(data);
                    if (loadDown == 0) {
                        newLoad = 0;
                    }
                    else {
                        newLoad = Math.min(loadDown + 1, 255);
                    }
                    newStable = false;
                }
                else if (block instanceof IStructural structural) {
                    int selfIntegrity = structural.getIntegrity(state);
                    int incrementForBeam = structural.getIncrementForBeam(state);
                    switch (structural.getBeamType(state)) {
                        case NONE -> {
                            structural.fail(level, state, x + minX, globalY, z + minZ);
                            return;
                        }
                        case X_BEAM -> {
                            int result = verifySimpleBeam(state, structural, chunk, section, holder, DirectionList.X_AXIS, index, x, y, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                            newLoad = result >> 16;
                            newIntegrity = result & 0xFFFF;
                        }
                        case Z_BEAM -> {
                            int result = verifySimpleBeam(state, structural, chunk, section, holder, DirectionList.Z_AXIS, index, x, y, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                            newLoad = result >> 16;
                            newIntegrity = result & 0xFFFF;
                        }
                        case CARDINAL_BEAM -> {
                            int result = verifySimpleBeam(state, structural, chunk, section, holder, DirectionList.HORIZONTAL, index, x, y, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                            newLoad = result >> 16;
                            newIntegrity = result & 0xFFFF;
                        }
                        case X_ARCH -> {
                            int result = verifyArch(state, structural, chunk, section, holder, index, x, y, z, selfIntegrity, DirectionList.X_AXIS, incrementForBeam);
                            newLoad = result >> 16;
                            newIntegrity = result & 0xFFFF;
                        }
                        case Z_ARCH -> {
                            int result = verifyArch(state, structural, chunk, section, holder, index, x, y, z, selfIntegrity, DirectionList.Z_AXIS, incrementForBeam);
                            newLoad = result >> 16;
                            newIntegrity = result & 0xFFFF;
                        }
                        case CARDINAL_ARCH -> {
                            int result = verifyArch(state, structural, chunk, section, holder, index, x, y, z, selfIntegrity, DirectionList.HORIZONTAL, incrementForBeam);
                            newLoad = result >> 16;
                            newIntegrity = result & 0xFFFF;
                        }
                    }
                    switch (structural.getStabilization(state)) {
                        case BEAM -> {
                            newStable = canStabilize(chunk, section, holder, structural, state, x, y, z, index, newLoad);
                        }
                        case ARCH -> {
                            newStable = canStabilizeArch(chunk, section, holder, structural, state, x, y, z, index, newLoad);
                        }
                    }
                }
                else {
                    //Not structural block
                    if (block instanceof IFallable fallable) {
                        fallable.fall(level, minX + x, globalY, minZ + z);
                        return;
                    }
                    newLoad = 0;
                    newIntegrity = 0;
                    newStable = false;
                }
            }
            boolean changed = false;
            if (oldLoad != newLoad) {
                loadFactorStorage.set(x, y, z, newLoad);
                changed = true;
            }
            if (oldIntegrity != newIntegrity) {
                integrityStorage.set(x, y, z, newIntegrity);
                changed = true;
            }
            if (oldStable != newStable) {
                stabilityStorage.set(x, y, z, newStable);
                changed = true;
            }
            if (newLoad > newIntegrity || newLoad == 255) {
                //Failure
                this.failOrPropagate(chunk, holder, section, x, y, z, index, globalY, newLoad, block, state, PropagationDirection.NONE);
                return;
            }
            if (changed) {
                for (PropagationDirection dir : PropagationDirection.randomHorizontal(level.random)) {
                    this.schedulePhysicsUpdate(chunk, holder, x, globalY, z, 0, dir);
                }
                this.schedulePhysicsUpdate(chunk, holder, x, globalY, z, 0, PropagationDirection.UP);
                for (PropagationDirection dir : PropagationDirection.randomHorizontal(level.random)) {
                    this.schedulePhysicsUpdate(chunk, holder, x, globalY + 1, z, 0, dir);
                }
                for (PropagationDirection dir : PropagationDirection.randomHorizontal(level.random)) {
                    this.schedulePhysicsUpdate(chunk, holder, x, globalY - 1, z, 0, dir);
                }
            }
        }
        else if (IFillable.unpackWasFillable(internalPos)) {
            loadFactorStorage.set(x, y, z, 0);
            integrityStorage.set(x, y, z, 0);
            stabilityStorage.set(x, y, z, false);
            for (PropagationDirection dir : PropagationDirection.randomHorizontal(level.random)) {
                this.schedulePhysicsUpdate(chunk, holder, x, globalY, z, 0, dir);
            }
            this.schedulePhysicsUpdate(chunk, holder, x, globalY, z, 0, PropagationDirection.UP);
            for (PropagationDirection dir : PropagationDirection.randomHorizontal(level.random)) {
                this.schedulePhysicsUpdate(chunk, holder, x, globalY + 1, z, 0, dir);
            }
            for (PropagationDirection dir : PropagationDirection.randomHorizontal(level.random)) {
                this.schedulePhysicsUpdate(chunk, holder, x, globalY - 1, z, 0, dir);
            }
        }
    }
}
