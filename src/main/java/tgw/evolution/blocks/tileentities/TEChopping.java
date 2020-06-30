package tgw.evolution.blocks.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionTileEntities;
import tgw.evolution.util.EnumWoodVariant;

public class TEChopping extends TileEntity {

    public byte id = -1;
    public byte breakProgress = 0;

    public TEChopping() {
        super(EvolutionTileEntities.TE_CHOPPING.get());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.id = compound.getByte("wood");
        this.breakProgress = compound.getByte("break");
    }

    public void sendRenderUpdate() {
        super.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), Constants.BlockFlags.RERENDER_MAIN_THREAD);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putByte("wood", this.id);
        compound.putByte("break", this.breakProgress);
        return super.write(compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    public void onRemoved() {
        if (this.id != -1 && !this.world.isRemote) {
            ItemStack stack = new ItemStack(EnumWoodVariant.byId(this.id).getLog());
            BlockUtils.dropItemStack(this.world, this.pos, stack);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 1, this.getUpdateTag());
    }
}
