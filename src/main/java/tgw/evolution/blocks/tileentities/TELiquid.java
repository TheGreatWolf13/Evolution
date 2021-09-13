package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTEs;

public class TELiquid extends TileEntity {

    /**
     * In units of 10 mL. 1 block contains 1_000 L, a layer contains 125 L.
     */
    private int missingLiquid;

    public TELiquid() {
        super(EvolutionTEs.LIQUID.get());
    }

    public int getMissingLiquid() {
        return this.missingLiquid;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.setMissingLiquid(compound.getInt("MissingLiquid"));
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putInt("MissingLiquid", this.missingLiquid);
        return super.save(compound);
    }

    public void setMissingLiquid(int missingLiquid) {
        this.missingLiquid = missingLiquid;
        this.setChanged();
    }

    @Override
    public String toString() {
        return "TELiquid{" + "missingLiquid=" + this.missingLiquid + '}';
    }
}
