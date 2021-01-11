package tgw.evolution.blocks.tileentities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTileEntities;

public class TELiquid extends TileEntity {

    /**
     * In units of 10 mL. 1 block contains 1_000 L, a layer contains 125 L.
     */
    private int missingLiquid;

    public TELiquid() {
        super(EvolutionTileEntities.TE_LIQUID.get());
    }

    public int getMissingLiquid() {
        return this.missingLiquid;
    }

    public void setMissingLiquid(int missingLiquid) {
        this.missingLiquid = missingLiquid;
        this.markDirty();
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.setMissingLiquid(compound.getInt("MissingLiquid"));
    }

    @Override
    public String toString() {
        return "TELiquid{" + "missingLiquid=" + this.missingLiquid + '}';
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("MissingLiquid", this.missingLiquid);
        return super.write(compound);
    }
}
