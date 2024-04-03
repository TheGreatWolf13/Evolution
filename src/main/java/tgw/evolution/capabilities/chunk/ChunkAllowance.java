package tgw.evolution.capabilities.chunk;

import net.minecraft.nbt.CompoundTag;

public class ChunkAllowance {

    public static final int BASE_GRASS_COST = 2_000;
    private static final int GRASS_REGEN = 1;
    private static final int GRASS_MAX = 16 * BASE_GRASS_COST;
    private int grassAllowance;
    private int grassOverflowTimer;
    private int tallGrassAllowance;

    public void deserializeNBT(CompoundTag tag) {
        this.grassAllowance = Short.toUnsignedInt(tag.getShort("GrassAllowance"));
        this.grassOverflowTimer = tag.getShort("GrassOverflowTimer");
        this.tallGrassAllowance = tag.getShort("TallGrassAllowance");
    }

    public boolean ifHasConsumeGrassAllowance(int allowance, boolean force) {
        assert 1 <= allowance && allowance <= GRASS_MAX;
        this.grassOverflowTimer = 4_000;
        if (!force && this.grassAllowance < GRASS_MAX) {
            return false;
        }
        if (this.grassAllowance >= allowance) {
            this.grassAllowance -= allowance;
            return true;
        }
        return false;
    }

    public boolean ifHasConsumeGrassAllowance(int allowance) {
        return this.ifHasConsumeGrassAllowance(allowance, false);
    }

    public boolean ifHasConsumeTallGrassAllowance(int allowance) {
        assert 1 <= allowance && allowance <= GRASS_MAX;
        if (this.tallGrassAllowance >= allowance) {
            this.tallGrassAllowance -= allowance;
            return true;
        }
        return false;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putShort("GrassAllowance", (short) this.grassAllowance);
        tag.putShort("GrassOverflowTimer", (short) this.grassOverflowTimer);
        tag.putShort("TallGrassAllowance", (short) this.tallGrassAllowance);
        return tag;
    }

    public void tick() {
        this.grassAllowance += GRASS_REGEN;
        if (this.grassOverflowTimer > 0) {
            --this.grassOverflowTimer;
            if (this.grassAllowance > 2 * GRASS_MAX) {
                this.grassAllowance = 2 * GRASS_MAX;
            }
        }
        else {
            if (this.grassAllowance > GRASS_MAX) {
                this.grassAllowance = GRASS_MAX;
            }
        }
        this.tallGrassAllowance += GRASS_REGEN;
        if (this.tallGrassAllowance > GRASS_MAX) {
            this.tallGrassAllowance = GRASS_MAX;
        }
    }
}
