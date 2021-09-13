package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.init.EvolutionTEs;

import javax.annotation.Nullable;

public class TETorch extends TileEntity implements ILoggable {

    @Nullable
    private FluidGeneric fluid;
    private int fluidAmount;
    private long timePlaced;

    public TETorch() {
        super(EvolutionTEs.TORCH.get());
    }

    @Override
    public Fluid getFluid() {
        return this.fluid == null ? Fluids.EMPTY : this.fluid;
    }

    @Override
    public int getFluidAmount() {
        return this.fluidAmount;
    }

    public long getTimePlaced() {
        return this.timePlaced;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.timePlaced = compound.getLong("TimePlaced");
        this.fluid = FluidGeneric.byId(compound.getByte("Fluid"));
        this.fluidAmount = compound.getInt("Amount");
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), pkt.getTag());
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putLong("TimePlaced", this.timePlaced);
        compound.putInt("Amount", this.fluidAmount);
        compound.putByte("Fluid", this.fluid == null ? 0 : this.fluid.getId());
        return super.save(compound);
    }

    @Override
    public void setAmountAndFluid(int amount, @Nullable FluidGeneric fluid) {
        this.fluid = fluid;
        this.setFluidAmount(amount);
    }

    @Override
    public void setFluidAmount(int amount) {
        this.fluidAmount = amount;
        TEUtils.sendRenderUpdate(this);
    }

    public void setPlaceTime() {
        this.setTimePlaced(this.level.getDayTime());
    }

    public void setTimePlaced(long timePlaced) {
        this.timePlaced = timePlaced;
        this.setChanged();
    }

    @Override
    public String toString() {
        return "TETorch{" + "fluid=" + this.fluid + ", fluidAmount=" + this.fluidAmount + ", timePlaced=" + this.timePlaced + '}';
    }
}
