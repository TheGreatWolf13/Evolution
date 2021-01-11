package tgw.evolution.blocks.tileentities;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.init.EvolutionTileEntities;

import javax.annotation.Nullable;

public class TETorch extends TileEntity implements ILoggable {

    @Nullable
    private FluidGeneric fluid;
    private int fluidAmount;
    private long timePlaced;

    public TETorch() {
        super(EvolutionTileEntities.TE_TORCH.get());
    }

    @Override
    public Fluid getFluid() {
        return this.fluid == null ? Fluids.EMPTY : this.fluid;
    }

    @Override
    public int getFluidAmount() {
        return this.fluidAmount;
    }

    @Override
    public void setFluidAmount(int amount) {
        this.fluidAmount = amount;
        TEUtils.sendRenderUpdate(this);
    }

    public long getTimePlaced() {
        return this.timePlaced;
    }

    public void setTimePlaced(long timePlaced) {
        this.timePlaced = timePlaced;
        this.markDirty();
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.timePlaced = compound.getLong("TimePlaced");
        this.fluid = FluidGeneric.byId(compound.getByte("Fluid"));
        this.fluidAmount = compound.getInt("Amount");
    }

    @Override
    public void setAmountAndFluid(int amount, @Nullable FluidGeneric fluid) {
        this.fluid = fluid;
        this.setFluidAmount(amount);
    }

    public void setPlaceTime() {
        this.setTimePlaced(this.world.getDayTime());
    }

    @Override
    public String toString() {
        return "TETorch{" + "fluid=" + this.fluid + ", fluidAmount=" + this.fluidAmount + ", timePlaced=" + this.timePlaced + '}';
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLong("TimePlaced", this.timePlaced);
        compound.putInt("Amount", this.fluidAmount);
        compound.putByte("Fluid", this.fluid == null ? 0 : this.fluid.getId());
        return super.write(compound);
    }
}
