package tgw.evolution.entities.ai;

import net.minecraft.entity.ai.goal.Goal;
import tgw.evolution.entities.EntityGenericAnimal;

import java.util.EnumSet;

public class SleepGoal extends Goal {

    private final EntityGenericAnimal entity;
    private final int timeFixed;
    private final int timeMargin;
    private int sleepTimer;

    public SleepGoal(EntityGenericAnimal entity, int timeFixed, int timeMargin) {
        this.entity = entity;
        this.timeFixed = timeFixed;
        this.timeMargin = timeMargin;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean shouldExecute() {
        return !this.entity.isDead() &&
               !this.entity.hasSlept() &&
               !this.entity.world.isDaytime() &&
               (this.chanceForSleep() || this.entity.isSleeping());
    }

    /**
     * Returns the chance for this entity to sleep in the given time.
     */
    public boolean chanceForSleep() {
        if (this.entity.world.getDayTime() % 1000 != 0) {
            return false;
        }
        int timeTilNightEnds = (int) (24000 - this.entity.world.getDayTime() % 24000);
        double chance = this.sleepTimer / (double) timeTilNightEnds;
        return this.entity.getRNG().nextInt((int) (100 / chance)) < 100;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.sleepingTick();
    }

    /**
     * Returns true if the entity still has time to sleep. Else, returns false.
     */
    private boolean sleepingTick() {
        if (this.sleepTimer > 0) {
            --this.sleepTimer;
            return true;
        }
        this.entity.setSlept();
        return false;
    }

    @Override
    public void startExecuting() {
        this.entity.setSleeping(true);
        this.entity.getNavigator().clearPath();
//        this.entity.getMoveHelper().setMoveTo(this.entity.posX, this.entity.posY, this.entity.posZ, 0.0);
    }

    @Override
    public void resetTask() {
        this.wakeUp();
    }

    /**
     * Wakes the entity up.
     */
    private void wakeUp() {
        this.entity.setSleeping(false);
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTimer = sleepTime;
    }

    public int getSleepTimer() {
        return this.sleepTimer;
    }

    /**
     * Sets the time period for which the entity will sleep, in ticks.
     */
    public void setSleepTimer() {
        this.sleepTimer = this.entity.getRNG().nextBoolean() ?
                          -this.entity.getRNG().nextInt(this.timeMargin) :
                          this.entity.getRNG().nextInt(this.timeMargin);
        this.sleepTimer += this.timeFixed;
    }
}
