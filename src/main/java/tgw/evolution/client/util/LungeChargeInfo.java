package tgw.evolution.client.util;

import net.minecraft.world.item.ItemStack;

public class LungeChargeInfo {

    private int duration;
    private ItemStack stack = ItemStack.EMPTY;
    private int time;

    public LungeChargeInfo(ItemStack lungingStack, int duration) {
        this.duration = duration;
        this.stack = lungingStack;
    }

    public void addInfo(ItemStack lungingStack, int duration) {
        this.duration = duration;
        this.stack = lungingStack;
        this.time = 0;
    }

    public void checkItem(ItemStack stack) {
        if (!this.stack.equals(stack, false)) {
            this.stack = ItemStack.EMPTY;
        }
    }

    public float getLungeMult() {
        return (float) this.time / this.duration;
    }

    public boolean isLungeInProgress() {
        return !this.stack.isEmpty();
    }

    public void resetHand() {
        this.stack = ItemStack.EMPTY;
    }

    public boolean shouldBeRemoved() {
        return this.stack.isEmpty();
    }

    public void tick() {
        if (!this.stack.isEmpty()) {
            this.time++;
            if (this.time >= this.duration) {
                this.stack = ItemStack.EMPTY;
            }
        }
    }
}
