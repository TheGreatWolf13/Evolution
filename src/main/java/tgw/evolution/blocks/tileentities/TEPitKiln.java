package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.DirectionDiagonal;
import tgw.evolution.util.WoodVariant;

import javax.annotation.Nullable;

public class TEPitKiln extends TileEntity {

    /**
     * Bit 0: burning;<br>
     * Bit 1: finished;<br>
     * Bit 2: single;<br>
     */
    private byte flags;
    private byte[] logs = {-1, -1, -1, -1, -1, -1, -1, -1};
    private ItemStack neStack = ItemStack.EMPTY;
    private ItemStack nwStack = ItemStack.EMPTY;
    private ItemStack seStack = ItemStack.EMPTY;
    private ItemStack swStack = ItemStack.EMPTY;
    private long timeStart = -1;

    public TEPitKiln() {
        super(EvolutionTEs.PIT_KILN.get());
    }

    public void checkEmpty() {
        if (this.level.isClientSide) {
            return;
        }
        if (this.isSingle()) {
            if (this.nwStack.isEmpty()) {
                this.level.removeBlock(this.worldPosition, false);
            }
            return;
        }
        if (this.neStack.isEmpty() && this.nwStack.isEmpty() && this.seStack.isEmpty() && this.swStack.isEmpty()) {
            this.level.removeBlock(this.worldPosition, false);
        }
    }

    public void finish() {
        this.setFinished(true);
        for (int i = 0; i < 8; i++) {
            this.logs[i] = -1;
        }
        //TODO manage stacks
        this.setChanged();
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
        return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    public boolean hasFinished() {
        return (this.flags & 2) != 0;
    }

    private boolean isBurning() {
        return (this.flags & 1) != 0;
    }

    public boolean isSingle() {
        return (this.flags & 4) != 0;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.flags = compound.getByte("Flags");
        this.logs = compound.getByteArray("Logs");
        this.nwStack = ItemStack.of(compound.getCompound("NW"));
        if (!this.isSingle()) {
            this.neStack = ItemStack.of(compound.getCompound("NE"));
            this.seStack = ItemStack.of(compound.getCompound("SE"));
            this.swStack = ItemStack.of(compound.getCompound("SW"));
        }
        if (this.isBurning()) {
            this.timeStart = compound.getLong("TimeStart");
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), pkt.getTag());
    }

    public void onRemoved() {
        if (!this.level.isClientSide) {
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.nwStack);
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.neStack);
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.swStack);
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.seStack);
            for (int i = 0; i < 8; i++) {
                if (this.logs[i] != -1) {
                    //noinspection ObjectAllocationInLoop
                    BlockUtils.dropItemStack(this.level, this.worldPosition, new ItemStack(WoodVariant.byId(this.logs[i]).getLogItem()));
                }
                else {
                    break;
                }
            }
        }
    }

    public void reset() {
        this.setBurning(false);
        this.timeStart = -1;
        this.setChanged();
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putByteArray("Logs", this.logs);
        compound.putByte("Flags", this.flags);
        compound.put("NW", this.nwStack.serializeNBT());
        if (!this.isSingle()) {
            compound.put("NE", this.neStack.serializeNBT());
            compound.put("SW", this.swStack.serializeNBT());
            compound.put("SE", this.seStack.serializeNBT());
        }
        if (this.isBurning()) {
            compound.putLong("TimeStart", this.timeStart);
        }
        return super.save(compound);
    }

    private void setBurning(boolean burning) {
        if (this.isBurning() != burning) {
            this.flags ^= 1;
        }
    }

    private void setFinished(boolean finished) {
        if (this.hasFinished() != finished) {
            this.flags ^= 1 << 1;
        }
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

    public void setSingle(boolean single) {
        if (this.isSingle() != single) {
            this.flags ^= 1 << 2;
        }
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
        this.setBurning(true);
        this.timeStart = this.level.getDayTime();
        this.setChanged();
    }
}
