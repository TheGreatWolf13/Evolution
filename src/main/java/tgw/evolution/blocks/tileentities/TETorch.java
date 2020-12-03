package tgw.evolution.blocks.tileentities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTileEntities;

public class TETorch extends TileEntity {

    private long timePlaced;

    public TETorch() {
        super(EvolutionTileEntities.TE_TORCH.get());
    }

    public void create() {
        this.setTimePlaced(this.world.getDayTime());
    }

    public long getTimePlaced() {
        return this.timePlaced;
    }

    public void setTimePlaced(long timePlaced) {
        this.timePlaced = timePlaced;
        this.markDirty();
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.setTimePlaced(compound.getLong("TimePlaced"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLong("TimePlaced", this.timePlaced);
        return super.write(compound);
    }
}
