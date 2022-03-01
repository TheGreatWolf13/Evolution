package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.init.EvolutionTEs;

import javax.annotation.Nullable;

public class TETorch extends BlockEntity implements ILoggable {

    @Nullable
    private FluidGeneric fluid;
    private int fluidAmount;
    private long timePlaced;

    public TETorch(BlockPos pos, BlockState state) {
        super(EvolutionTEs.TORCH.get(), pos, state);
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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.timePlaced = tag.getLong("TimePlaced");
        this.fluid = FluidGeneric.byId(tag.getByte("Fluid"));
        this.fluidAmount = tag.getInt("Amount");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.handleUpdateTag(pkt.getTag());
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("TimePlaced", this.timePlaced);
        tag.putInt("Amount", this.fluidAmount);
        tag.putByte("Fluid", this.fluid == null ? 0 : this.fluid.getId());
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
