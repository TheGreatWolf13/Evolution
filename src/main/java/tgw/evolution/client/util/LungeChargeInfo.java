package tgw.evolution.client.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class LungeChargeInfo {

    private int mainhandDuration;
    private ItemStack mainhandStack = ItemStack.EMPTY;
    private int mainhandTime;
    private int offhandDuration;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private int offhandTime;

    public LungeChargeInfo(InteractionHand hand, ItemStack lungingStack, int duration) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandDuration = duration;
            this.mainhandStack = lungingStack;
        }
        else {
            this.offhandDuration = duration;
            this.offhandStack = lungingStack;
        }
    }

    public void addInfo(InteractionHand hand, ItemStack lungingStack, int duration) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandDuration = duration;
            this.mainhandStack = lungingStack;
            this.mainhandTime = 0;
        }
        else {
            this.offhandDuration = duration;
            this.offhandStack = lungingStack;
            this.offhandTime = 0;
        }
    }

    public void checkItem(InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            if (!this.mainhandStack.equals(stack, false)) {
                this.mainhandStack = ItemStack.EMPTY;
            }
        }
        else {
            if (!this.offhandStack.equals(stack, false)) {
                this.offhandStack = ItemStack.EMPTY;
            }
        }
    }

    public float getLungeMult(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return (float) this.mainhandTime / this.mainhandDuration;
        }
        return (float) this.offhandTime / this.offhandDuration;
    }

    public boolean isLungeInProgress(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return !this.mainhandStack.isEmpty();
        }
        return !this.offhandStack.isEmpty();
    }

    public void resetHand(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandStack = ItemStack.EMPTY;
        }
        else {
            this.offhandStack = ItemStack.EMPTY;
        }
    }

    public boolean shouldBeRemoved() {
        if (this.mainhandStack.isEmpty()) {
            return this.offhandStack.isEmpty();
        }
        return false;
    }

    public void tick() {
        if (!this.mainhandStack.isEmpty()) {
            this.mainhandTime++;
            if (this.mainhandTime >= this.mainhandDuration) {
                this.mainhandStack = ItemStack.EMPTY;
            }
        }
        if (!this.offhandStack.isEmpty()) {
            this.offhandTime++;
            if (this.offhandTime >= this.offhandDuration) {
                this.offhandStack = ItemStack.EMPTY;
            }
        }
    }
}
