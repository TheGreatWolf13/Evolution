package tgw.evolution.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EvBlockDestructionProgress implements Comparable<EvBlockDestructionProgress> {

    private int createdTick;
    private @Nullable Direction face;
    private double hitX;
    private double hitY;
    private double hitZ;
    private final int id;
    private final long pos;
    private int progress;

    public EvBlockDestructionProgress(int id, long pos) {
        this.id = id;
        this.pos = pos;
    }

    @Override
    public int compareTo(EvBlockDestructionProgress o) {
        return this.progress != o.progress ? Integer.compare(this.progress, o.progress) : Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EvBlockDestructionProgress a) {
            return this.id == a.id;
        }
        return false;
    }

    public BlockState getBlockState(Level level) {
        int x = BlockPos.getX(this.pos);
        int y = BlockPos.getY(this.pos);
        int z = BlockPos.getZ(this.pos);
        return level.getBlockState_(x, y, z).getDestroyingState(level, x, y, z, this.face, this.hitX, this.hitY, this.hitZ);
    }

    public int getId() {
        return this.id;
    }

    public long getPos() {
        return this.pos;
    }

    public int getProgress() {
        return this.progress;
    }

    /**
     * retrieves the 'date' at which the PartiallyDestroyedBlock was created
     */
    public int getUpdatedRenderTick() {
        return this.createdTick;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }

    public void setLocation(@Nullable Direction face, double hitX, double hitY, double hitZ) {
        this.face = face;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    /**
     * inserts damage value into this partially destroyed Block. -1 causes client renderer to delete it, otherwise ranges
     * from 1 to 10
     */
    public void setProgress(int progress) {
        if (progress > 10) {
            progress = 10;
        }
        this.progress = progress;
    }

    /**
     * saves the current Cloud update tick into the PartiallyDestroyedBlock
     */
    public void updateTick(int tick) {
        this.createdTick = tick;
    }
}
