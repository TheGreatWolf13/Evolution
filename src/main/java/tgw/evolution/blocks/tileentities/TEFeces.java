package tgw.evolution.blocks.tileentities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTileEntities;
import tgw.evolution.util.EnumFoodNutrients;
import tgw.evolution.util.Feces;

public class TEFeces extends TileEntity implements ITickableTileEntity {

    public final Feces feces = new Feces();

    public TEFeces() {
        super(EvolutionTileEntities.TE_FECES.get());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.feces.set(EnumFoodNutrients.FOOD, compound.getByteArray("Feces")[0]);
        this.feces.set(EnumFoodNutrients.NITROGEN, compound.getByteArray("Feces")[1]);
        this.feces.set(EnumFoodNutrients.POTASSIUM, compound.getByteArray("Feces")[2]);
        this.feces.set(EnumFoodNutrients.PHOSPHORUS, compound.getByteArray("Feces")[3]);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putByteArray("Feces", new byte[]{(byte) this.feces.get(EnumFoodNutrients.FOOD),
                                                  (byte) this.feces.get(EnumFoodNutrients.NITROGEN),
                                                  (byte) this.feces.get(EnumFoodNutrients.POTASSIUM),
                                                  (byte) this.feces.get(EnumFoodNutrients.PHOSPHORUS)});
        return compound;
    }

    @Override
    public void tick() {
    }
}
