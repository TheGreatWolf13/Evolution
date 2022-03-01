package tgw.evolution.entities.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import tgw.evolution.entities.EntityGenericAnimal;

import java.util.EnumSet;

public class GoalSleep extends Goal {

    private final EntityGenericAnimal entity;
    private final int timeFixed;
    private final int timeMargin;
    private int sleepTimer;

    public GoalSleep(EntityGenericAnimal entity, int timeFixed, int timeMargin) {
        this.entity = entity;
        this.timeFixed = timeFixed;
        this.timeMargin = timeMargin;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canContinueToUse() {
        return this.sleepingTick();
    }

    @Override
    public boolean canUse() {
        return !this.entity.isDead() && !this.entity.hasSlept() && !this.entity.level.isDay() && (this.chanceForSleep() || this.entity.isSleeping());
    }

    /**
     * Returns the chance for this entity to sleep in the given time.
     */
    public boolean chanceForSleep() {
        if (this.entity.level.getDayTime() % 1_000 != 0) {
            return false;
        }
        int timeTilNightEnds = (int) (24_000 - this.entity.level.getDayTime() % 24_000);
        double chance = this.sleepTimer / (double) timeTilNightEnds;
        return this.entity.getRandom().nextInt((int) (100 / chance)) < 100;
    }

    public int getSleepTimer() {
        return this.sleepTimer;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTimer = sleepTime;
    }

    /**
     * Sets the time period for which the entity will sleep, in ticks.
     */
    public void setSleepTimer() {
        this.sleepTimer = this.entity.getRandom().nextBoolean() ?
                          -this.entity.getRandom().nextInt(this.timeMargin) :
                          this.entity.getRandom().nextInt(this.timeMargin);
        this.sleepTimer += this.timeFixed;
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
    public void start() {
        this.entity.setSleeping(true);
        this.entity.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.wakeUp();
    }

    /**
     * Wakes the entity up.
     */
    private void wakeUp() {
        this.entity.setSleeping(false);
    }
}
