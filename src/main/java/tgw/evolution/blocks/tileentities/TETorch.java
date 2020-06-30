package tgw.evolution.blocks.tileentities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTileEntities;

public class TETorch extends TileEntity {

    public int timePlaced;

    public TETorch() {
        super(EvolutionTileEntities.TE_TORCH.get());
    }

    public void create() {
        this.timePlaced = Math.toIntExact(this.world.getDayTime());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.timePlaced = compound.getInt("TimePlaced");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("TimePlaced", this.timePlaced);
        return super.write(compound);
    }
}
