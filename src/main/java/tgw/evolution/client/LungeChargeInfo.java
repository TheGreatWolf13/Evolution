package tgw.evolution.client;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class LungeChargeInfo {

    private int mainhandDuration;
    private ItemStack mainhandStack = ItemStack.EMPTY;
    private int mainhandTime;
    private int offhandDuration;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private int offhandTime;

    public LungeChargeInfo(Hand hand, ItemStack lungingStack, int duration) {
        if (hand == Hand.MAIN_HAND) {
            this.mainhandDuration = duration;
            this.mainhandStack = lungingStack;
        }
        else {
            this.offhandDuration = duration;
            this.offhandStack = lungingStack;
        }
    }

    public void addInfo(Hand hand, ItemStack lungingStack, int duration) {
        if (hand == Hand.MAIN_HAND) {
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

    public void checkItem(Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND) {
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

    public float getLungeMult(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return (float) this.mainhandTime / this.mainhandDuration;
        }
        return (float) this.offhandTime / this.offhandDuration;
    }

    public boolean isLungeInProgress(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return !this.mainhandStack.isEmpty();
        }
        return !this.offhandStack.isEmpty();
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
