package tgw.evolution.client.util;

import net.minecraft.world.InteractionHand;

public class LungeAttackInfo {

    private boolean isMainhandLunging;
    private boolean isOffhandLunging;
    private int mainhandTime;
    private int offhandTime;

    public LungeAttackInfo(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.isMainhandLunging = true;
        }
        else {
            this.isOffhandLunging = true;
        }
    }

    public void addInfo(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandTime = 0;
            this.isMainhandLunging = true;
        }
        else {
            this.offhandTime = 0;
            this.isOffhandLunging = true;
        }
    }

    public float getLungeMult(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return this.mainhandTime / 4.0f;
        }
        return this.offhandTime / 4.0f;
    }

    public boolean isLungeInProgress(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return this.isMainhandLunging;
        }
        return this.isOffhandLunging;
    }

    public boolean shouldBeRemoved() {
        if (!this.isMainhandLunging) {
            return !this.isOffhandLunging;
        }
        return false;
    }

    public void tick() {
        if (this.isMainhandLunging) {
            this.mainhandTime++;
            if (this.mainhandTime == 4) {
                this.isMainhandLunging = false;
            }
        }
        if (this.isOffhandLunging) {
            this.offhandTime++;
            if (this.offhandTime == 4) {
                this.isOffhandLunging = false;
            }
        }
    }
}
