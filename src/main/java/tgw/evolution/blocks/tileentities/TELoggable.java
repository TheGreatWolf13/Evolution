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

public class TELoggable extends BlockEntity implements ILoggable {

    @Nullable
    private FluidGeneric fluid;
    private int fluidAmount;

    public TELoggable(BlockPos pos, BlockState state) {
        super(EvolutionTEs.LOGGABLE.get(), pos, state);
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
        this.fluidAmount = tag.getInt("Amount");
        this.fluid = FluidGeneric.byId(tag.getByte("Fluid"));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.handleUpdateTag(packet.getTag());
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Amount", this.fluidAmount);
        tag.putByte("Fluid", this.fluid == null ? 0 : this.fluid.getId());
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
