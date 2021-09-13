package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.items.ItemLog;
import tgw.evolution.util.WoodVariant;

import javax.annotation.Nullable;

public class TEChopping extends TileEntity implements ILoggable {

    private byte breakProgress;
    @Nullable
    private FluidGeneric fluid;
    private int fluidAmount;
    private byte id = -1;

    public TEChopping() {
        super(EvolutionTEs.CHOPPING.get());
    }

    public void breakLog(PlayerEntity player) {
        if (!this.level.isClientSide) {
            ItemStack stack = new ItemStack(WoodVariant.byId(this.id).getPlank(), 8);
            BlockUtils.dropItemStack(this.level, this.worldPosition, stack);
            player.getMainHandItem().hurtAndBreak(1, player, playerEntity -> playerEntity.getItemBySlot(EquipmentSlotType.MAINHAND));
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
    }

    public void dropLog() {
        if (this.hasLog() && !this.level.isClientSide) {
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.getItemStack());
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
    }

    public byte getBreakProgress() {
        return this.breakProgress;
    }

    @Override
    public Fluid getFluid() {
        return this.fluid == null ? Fluids.EMPTY : this.fluid;
    }

    @Override
    public int getFluidAmount() {
        return this.fluidAmount;
    }

    public ItemStack getItemStack() {
        return new ItemStack(WoodVariant.byId(this.id).getLogItem());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    public boolean hasLog() {
        return this.id != -1;
    }

    public int increaseBreakProgress() {
        this.breakProgress++;
        TEUtils.sendRenderUpdate(this);
        return this.breakProgress;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.id = compound.getByte("Wood");
        this.breakProgress = compound.getByte("Break");
        this.fluid = FluidGeneric.byId(compound.getByte("Fluid"));
        this.fluidAmount = compound.getInt("Amount");
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), packet.getTag());
        TEUtils.sendRenderUpdate(this);
    }

    public void removeStack(PlayerEntity player) {
        ItemStack stack = this.getItemStack();
        if (!this.level.isClientSide && !player.inventory.add(stack)) {
            BlockUtils.dropItemStack(this.level, this.worldPosition, stack);
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putByte("Wood", this.id);
        compound.putByte("Break", this.breakProgress);
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

    public void setStack(PlayerEntity player, Hand hand) {
        this.id = ((ItemLog) player.getItemInHand(hand).getItem()).variant.getId();
        TEUtils.sendRenderUpdate(this);
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }
    }

    @Override
    public String toString() {
        return "TEChopping{" +
               "breakProgress=" +
               this.breakProgress +
               ", fluid=" +
               this.fluid +
               ", fluidAmount=" +
               this.fluidAmount +
               ", id=" +
               this.id +
               '}';
    }
}
