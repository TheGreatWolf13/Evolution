package tgw.evolution.blocks.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionTileEntities;
import tgw.evolution.util.DirectionDiagonal;
import tgw.evolution.util.EnumWoodVariant;

import javax.annotation.Nullable;

public class TEPitKiln extends TileEntity {

    public boolean burning;
    public boolean finished;
    public byte[] logs = {-1, -1, -1, -1, -1, -1, -1, -1};
    public ItemStack neStack = ItemStack.EMPTY;
    public ItemStack nwStack = ItemStack.EMPTY;
    public ItemStack seStack = ItemStack.EMPTY;
    public boolean single;
    public ItemStack swStack = ItemStack.EMPTY;
    public long timeStart = -1;

    public TEPitKiln() {
        super(EvolutionTileEntities.TE_PIT_KILN.get());
    }

    public void checkEmpty() {
        if (this.world.isRemote) {
            return;
        }
        if (this.single) {
            if (this.nwStack.isEmpty()) {
                this.world.removeBlock(this.pos, false);
            }
            return;
        }
        if (this.neStack.isEmpty() && this.nwStack.isEmpty() && this.seStack.isEmpty() && this.swStack.isEmpty()) {
            this.world.removeBlock(this.pos, false);
        }
    }

    public void finish() {
        this.finished = true;
        for (int i = 0; i < 8; i++) {
            this.logs[i] = -1;
        }
        //TODO manage stacks
        this.markDirty();
    }

    public ItemStack getStack(DirectionDiagonal direction) {
        switch (direction) {
            case NORTH_EAST:
                return this.neStack;
            case NORTH_WEST:
                return this.nwStack;
            case SOUTH_EAST:
                return this.seStack;
            case SOUTH_WEST:
                return this.swStack;
        }
        throw new IllegalStateException("This enum does not exist: " + direction);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    public void onRemoved() {
        if (!this.world.isRemote) {
            BlockUtils.dropItemStack(this.world, this.pos, this.nwStack);
            BlockUtils.dropItemStack(this.world, this.pos, this.neStack);
            BlockUtils.dropItemStack(this.world, this.pos, this.swStack);
            BlockUtils.dropItemStack(this.world, this.pos, this.seStack);
            for (int i = 0; i < 8; i++) {
                if (this.logs[i] != -1) {
                    //noinspection ObjectAllocationInLoop
                    BlockUtils.dropItemStack(this.world, this.pos, new ItemStack(EnumWoodVariant.byId(this.logs[i]).getLog()));
                }
                else {
                    break;
                }
            }
        }
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.single = compound.getBoolean("Single");
        this.logs = compound.getByteArray("Logs");
        this.burning = compound.getBoolean("Burning");
        this.finished = compound.getBoolean("Finished");
        this.nwStack = ItemStack.read(compound.getCompound("NW"));
        if (!this.single) {
            this.neStack = ItemStack.read(compound.getCompound("NE"));
            this.seStack = ItemStack.read(compound.getCompound("SE"));
            this.swStack = ItemStack.read(compound.getCompound("SW"));
        }
        if (this.burning) {
            this.timeStart = compound.getLong("TimeStart");
        }
    }

    public void reset() {
        this.burning = false;
        this.timeStart = -1;
        this.markDirty();
    }

    public void sendRenderUpdate() {
        super.markDirty();
        this.world.notifyBlockUpdate(this.pos,
                                     this.world.getBlockState(this.pos),
                                     this.world.getBlockState(this.pos),
                                     Constants.BlockFlags.RERENDER_MAIN_THREAD);
    }

    public void setNeStack(ItemStack stack) {
        this.neStack = stack.copy();
        this.neStack.setCount(1);
        stack.shrink(1);
    }

    public void setNwStack(ItemStack stack) {
        this.nwStack = stack.copy();
        this.nwStack.setCount(1);
        stack.shrink(1);
    }

    public void setSeStack(ItemStack stack) {
        this.seStack = stack.copy();
        this.seStack.setCount(1);
        stack.shrink(1);
    }

    public void setStack(ItemStack stack, DirectionDiagonal diagonal) {
        switch (diagonal) {
            case NORTH_WEST:
                this.setNwStack(stack);
                break;
            case NORTH_EAST:
                this.setNeStack(stack);
                break;
            case SOUTH_WEST:
                this.setSwStack(stack);
                break;
            case SOUTH_EAST:
                this.setSeStack(stack);
                break;
        }
    }

    public void setSwStack(ItemStack stack) {
        this.swStack = stack.copy();
        this.swStack.setCount(1);
        stack.shrink(1);
    }

    public void start() {
        this.burning = true;
        this.timeStart = this.world.getDayTime();
        this.markDirty();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putByteArray("Logs", this.logs);
        compound.putBoolean("Single", this.single);
        compound.putBoolean("Finished", this.finished);
        compound.putBoolean("Burning", this.burning);
        compound.put("NW", this.nwStack.serializeNBT());
        if (!this.single) {
            compound.put("NE", this.neStack.serializeNBT());
            compound.put("SW", this.swStack.serializeNBT());
            compound.put("SE", this.seStack.serializeNBT());
        }
        if (this.burning) {
            compound.putLong("TimeStart", this.timeStart);
        }
        return super.write(compound);
    }
}
