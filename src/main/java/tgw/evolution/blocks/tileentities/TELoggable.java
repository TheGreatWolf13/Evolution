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

public class TELoggable extends TileEntity implements ILoggable {

    @Nullable
    private FluidGeneric fluid;
    private int fluidAmount;

    public TELoggable() {
        super(EvolutionTEs.LOGGABLE.get());
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
        this.fluidAmount = compound.getInt("Amount");
        this.fluid = FluidGeneric.byId(compound.getByte("Fluid"));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), packet.getTag());
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
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
    public void setFluidAmount(int fluidAmount) {
        this.fluidAmount = fluidAmount;
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public String toString() {
        return "TELoggable{" + "fluid=" + this.fluid + ", fluidAmount=" + this.fluidAmount + '}';
    }
}
