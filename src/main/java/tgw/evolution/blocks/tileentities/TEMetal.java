package tgw.evolution.blocks.tileentities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTileEntities;

public class TEMetal extends TileEntity {

    public int timePlaced;
    public boolean exposed;

    public TEMetal() {
        super(EvolutionTileEntities.TE_METAL.get());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.timePlaced = compound.getInt("TimePlaced");
        this.exposed = compound.getBoolean("Exposed");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("TimePlaced", this.timePlaced);
        compound.putBoolean("Exposed", this.exposed);
        return super.write(compound);
    }
}
