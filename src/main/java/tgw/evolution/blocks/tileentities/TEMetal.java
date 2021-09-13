package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTEs;

public class TEMetal extends TileEntity {

    public boolean exposed;
    public int timePlaced;

    public TEMetal() {
        super(EvolutionTEs.METAL.get());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.timePlaced = compound.getInt("TimePlaced");
        this.exposed = compound.getBoolean("Exposed");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putInt("TimePlaced", this.timePlaced);
        compound.putBoolean("Exposed", this.exposed);
        return super.save(compound);
    }
}
