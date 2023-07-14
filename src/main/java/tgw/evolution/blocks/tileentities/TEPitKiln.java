package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.constants.WoodVariant;
import tgw.evolution.util.math.DirectionDiagonal;

public class TEPitKiln extends BlockEntity {

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

    public TEPitKiln(BlockPos pos, BlockState state) {
        super(EvolutionTEs.PIT_KILN, pos, state);
    }

    public void checkEmpty() {
        assert this.level != null;
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
        return new ItemStack(WoodVariant.byId(this.logs[index]).get(EvolutionItems.FIREWOODS));
    }

    public byte[] getLogs() {
        return this.logs;
    }

    public ItemStack getStack(DirectionDiagonal direction) {
        return switch (direction) {
            case NORTH_EAST -> this.neStack;
            case NORTH_WEST -> this.nwStack;
            case SOUTH_EAST -> this.seStack;
            case SOUTH_WEST -> this.swStack;
        };
    }

    public long getTimeStart() {
        return this.timeStart;
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
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
    public void load(CompoundTag tag) {
        super.load(tag);
        this.flags = tag.getByte("Flags");
        this.logs = tag.getByteArray("Logs");
        this.nwStack = ItemStack.of(tag.getCompound("NW"));
        if (!this.isSingle()) {
            this.neStack = ItemStack.of(tag.getCompound("NE"));
            this.seStack = ItemStack.of(tag.getCompound("SE"));
            this.swStack = ItemStack.of(tag.getCompound("SW"));
        }
        if (this.isBurning()) {
            this.timeStart = tag.getLong("TimeStart");
        }
    }

    public void onRemoved() {
        assert this.level != null;
        if (!this.level.isClientSide) {
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.nwStack);
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.neStack);
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.swStack);
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.seStack);
            for (int i = 0; i < 8; i++) {
                if (this.logs[i] != -1) {
                    //noinspection ObjectAllocationInLoop
                    BlockUtils.dropItemStack(this.level, this.worldPosition,
                                             new ItemStack(WoodVariant.byId(this.logs[i]).get(EvolutionItems.FIREWOODS)));
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByteArray("Logs", this.logs);
        tag.putByte("Flags", this.flags);
        tag.put("NW", this.nwStack.save(new CompoundTag()));
        if (!this.isSingle()) {
            tag.put("NE", this.neStack.save(new CompoundTag()));
            tag.put("SW", this.swStack.save(new CompoundTag()));
            tag.put("SE", this.seStack.save(new CompoundTag()));
        }
        if (this.isBurning()) {
            tag.putLong("TimeStart", this.timeStart);
        }
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
            case NORTH_WEST -> this.setNWStack(stack);
            case NORTH_EAST -> this.setNEStack(stack);
            case SOUTH_WEST -> this.setSWStack(stack);
            case SOUTH_EAST -> this.setSEStack(stack);
        }
    }

    public void start() {
        this.setBurning(true);
        assert this.level != null;
        this.timeStart = this.level.getDayTime();
        this.setChanged();
    }
}
