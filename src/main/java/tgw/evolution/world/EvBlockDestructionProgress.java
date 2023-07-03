package tgw.evolution.world;

import net.minecraft.core.BlockPos;

public class EvBlockDestructionProgress implements Comparable<EvBlockDestructionProgress> {

    private final int id;
    private final long pos;
    private int createdTick;
    private int progress;

    public EvBlockDestructionProgress(int id, long pos) {
        this.id = id;
        this.pos = pos;
    }

    public EvBlockDestructionProgress(int id, BlockPos pos) {
        this.id = id;
        this.pos = pos.asLong();
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

    /**
     * inserts damage value into this partially destroyed Block. -1 causes client renderer to delete it, otherwise ranges
     * from 1 to 10
     */
    public void setProgress(int pDamage) {
        if (pDamage > 10) {
            pDamage = 10;
        }

        this.progress = pDamage;
    }

    /**
     * saves the current Cloud update tick into the PartiallyDestroyedBlock
     */
    public void updateTick(int tick) {
        this.createdTick = tick;
    }
}
