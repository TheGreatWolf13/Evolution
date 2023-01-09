package tgw.evolution.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class ChunkHolder {

    private @Nullable LevelChunk chunkEast;
    private @Nullable LevelChunk chunkNorth;
    private @Nullable LevelChunk chunkSouth;
    private @Nullable LevelChunk chunkWest;
    /**
     * Bit 0: Holds North Chunk <br>
     * Bit 1: Holds South Chunk <br>
     * Bit 2: Holds East Chunk <br>
     * Bit 3: Holds West Chunk <br>
     */
    private byte flags;

    @Nullable
    public LevelChunk getHeld(Direction dir) {
        return switch (dir) {
            case NORTH -> this.chunkNorth;
            case SOUTH -> this.chunkSouth;
            case EAST -> this.chunkEast;
            case WEST -> this.chunkWest;
            case UP, DOWN -> null;
        };
    }

    public boolean isHolding(Direction dir) {
        if (dir.getAxis() == Direction.Axis.Y) {
            return true;
        }
        int mask = switch (dir) {
            case NORTH -> 1;
            case SOUTH -> 2;
            case EAST -> 4;
            case WEST -> 8;
            default -> throw new IllegalStateException("Shouldn't reach here");
        };
        return (this.flags & mask) != 0;
    }

    public void reset() {
        this.flags = 0;
        this.chunkNorth = null;
        this.chunkSouth = null;
        this.chunkEast = null;
        this.chunkWest = null;
    }

    public void setupIfNeeded(LevelAccessor level, ChunkPos pos, Direction dir) {
        if (this.isHolding(dir)) {
            return;
        }
        int x = pos.x + dir.getStepX();
        int z = pos.z + dir.getStepZ();
        LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
        switch (dir) {
            case NORTH -> {
                this.chunkNorth = chunk;
                this.flags |= 1;
            }
            case SOUTH -> {
                this.chunkSouth = chunk;
                this.flags |= 2;
            }
            case EAST -> {
                this.chunkEast = chunk;
                this.flags |= 4;
            }
            case WEST -> {
                this.chunkWest = chunk;
                this.flags |= 8;
            }
        }
    }
}
