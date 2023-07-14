package tgw.evolution.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.Evolution;

import javax.annotation.Nullable;

public abstract class BasicContainer implements Container {

    protected final NonNullList<ItemStack> items;

    public BasicContainer(int size) {
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public final void clearContent() {
        this.items.clear();
    }

    public final void deserializeNBT(@Nullable CompoundTag tag) {
        if (tag == null) {
            return;
        }
        this.items.clear();
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0, len = list.size(); i < len; i++) {
            CompoundTag itemTags = list.getCompound(i);
            int slot = itemTags.getInt("Slot");
            if (slot >= 0 && slot < this.items.size()) {
                ItemStack stack = ItemStack.of(itemTags);
                this.items.set(slot, stack);
            }
            else {
                Evolution.warn("Item doesn't fit into holder with size {} and will be deleted: {}", this.items.size(), slot);
            }
        }
    }

    @Override
    public final int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        this.validateSlotIndex(slot);
        return this.items.get(slot);
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
    }

    /**
     * @return The {@link ItemStack} that couldn't be inserted and remains in the mouse.
     */
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!this.isItemValid(slot, stack)) {
            return stack;
        }
        this.validateSlotIndex(slot);
        ItemStack existing = this.items.get(slot);
        int limit = this.getStackLimit(slot, stack);
        if (!existing.isEmpty()) {
//            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
//                return stack;
//            }

            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                this.items.set(slot,/* reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : */stack);
            }
            else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            this.onContentsChanged(slot);
        }
        return /*reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : */ ItemStack.EMPTY;
    }

    @Override
    public final boolean isEmpty() {
        for (int i = 0, len = this.items.size(); i < len; ++i) {
            if (!this.items.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public boolean needsHandler(int slot) {
        return false;
    }

    protected void onContentsChanged(int slot) {
    }

    public abstract void onTake(int slot, Player player, ItemStack stackTaken, ItemStack newStack);

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        this.validateSlotIndex(slot);
        ItemStack existing = this.items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            this.items.set(slot, ItemStack.EMPTY);
            this.onContentsChanged(slot);
            return existing;
        }
        this.items.set(slot,/* ItemHandlerHelper.copyStackWithSize(*/existing/*, existing.getCount() - toExtract)*/);
        this.onContentsChanged(slot);
        return /*ItemHandlerHelper.copyStackWithSize(*/existing/*, toExtract)*/;
    }

    @Override
    public final ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.items.get(slot);
        this.items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    public final CompoundTag serializeNBT() {
        ListTag list = new ListTag();
        for (int i = 0; i < this.items.size(); i++) {
            if (!this.items.get(i).isEmpty()) {
                //noinspection ObjectAllocationInLoop
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                this.items.get(i).save(itemTag);
                list.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", list);
        return nbt;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.validateSlotIndex(slot);
        this.items.set(slot, stack);
        this.onContentsChanged(slot);
    }

    @Override
    public abstract boolean stillValid(Player player);

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.items.size()) {
            throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + this.items.size() + ")");
        }
    }
}
