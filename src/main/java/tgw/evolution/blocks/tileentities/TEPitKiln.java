package tgw.evolution.blocks.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionTileEntities;
import tgw.evolution.util.DirectionDiagonal;
import tgw.evolution.util.WoodVariant;

import javax.annotation.Nullable;

public class TEPitKiln extends TileEntity {

    private boolean burning;
    private boolean finished;
    private byte[] logs = {-1, -1, -1, -1, -1, -1, -1, -1};
    private ItemStack neStack = ItemStack.EMPTY;
    private ItemStack nwStack = ItemStack.EMPTY;
    private ItemStack seStack = ItemStack.EMPTY;
    private boolean single;
    private ItemStack swStack = ItemStack.EMPTY;
    private long timeStart = -1;

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

    public ItemStack getLogStack(int index) {
        return new ItemStack(WoodVariant.byId(this.logs[index]).getLogItem());
    }

    public byte[] getLogs() {
        return this.logs;
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

    public long getTimeStart() {
        return this.timeStart;
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

    public boolean hasFinished() {
        return this.finished;
    }

    public boolean isSingle() {
        return this.single;
    }

    public void setSingle(boolean single) {
        this.single = single;
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
                    BlockUtils.dropItemStack(this.world, this.pos, new ItemStack(WoodVariant.byId(this.logs[i]).getLogItem()));
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

    public void setLog(int index, byte id) {
        this.logs[index] = id;
        TEUtils.sendRenderUpdate(this);
    }

    public void setNEStack(ItemStack stack) {
        this.neStack = stack.copy();
        this.neStack.setCount(1);
        stack.shrink(1);
    }

    public void setNWStack(ItemStack stack) {
        this.nwStack = stack.copy();
        this.nwStack.setCount(1);
        stack.shrink(1);
    }

    public void setSEStack(ItemStack stack) {
        this.seStack = stack.copy();
        this.seStack.setCount(1);
        stack.shrink(1);
    }

    public void setSWStack(ItemStack stack) {
        this.swStack = stack.copy();
        this.swStack.setCount(1);
        stack.shrink(1);
    }

    public void setStack(ItemStack stack, DirectionDiagonal diagonal) {
        switch (diagonal) {
            case NORTH_WEST:
                this.setNWStack(stack);
                break;
            case NORTH_EAST:
                this.setNEStack(stack);
                break;
            case SOUTH_WEST:
                this.setSWStack(stack);
                break;
            case SOUTH_EAST:
                this.setSEStack(stack);
                break;
        }
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
