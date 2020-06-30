package tgw.evolution.entities.ai;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal;
import tgw.evolution.entities.AnimalEntity;

public class SleepGoal extends Goal {
	private final AnimalEntity entity;
	private final int timeDefined;
	private final int timeRandom;
	private int sleepTimer = 0;

	public SleepGoal(AnimalEntity entity, int timeDefined, int timeRandom) {
		this.entity = entity;
		this.timeDefined = timeDefined;
		this.timeRandom = timeRandom;
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
	}

	@Override
	public boolean shouldExecute() {
		return !this.entity.isDead() && !this.entity.slept && !this.entity.world.isDaytime() && (this.chanceForSleep() || this.entity.isSleeping());
	}
	
	public void setSleepTime(int sleepTime) {
		this.sleepTimer = sleepTime;
	}
	
	public int getSleepTimer() {
		return this.sleepTimer;
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
		return this.sleepingTime();
	}

	/**
	 * Returns true if the entity still has time to sleep. Else, returns false.
	 */
	private boolean sleepingTime() {
		if (this.sleepTimer > 0) {
			--this.sleepTimer;
			return true;
		} 
		this.entity.slept = true;
		return false;
	}

	@Override
	public void resetTask() {
		this.wakeUp();
	}
	
	/**
	 * Sets the time period for which the entity will sleep.
	 */
	public void setSleepTimer() {
		this.sleepTimer = this.entity.getRNG().nextBoolean() ? -this.entity.getRNG().nextInt(this.timeRandom) : this.entity.getRNG().nextInt(this.timeRandom);
		this.sleepTimer += this.timeDefined;
	}

	@Override
	public void startExecuting() {
		this.entity.setSleeping(true);
		this.entity.getNavigator().clearPath();
		this.entity.getMoveHelper().setMoveTo(this.entity.posX, this.entity.posY, this.entity.posZ, 0.0D);
	}
	
	/**
	 * Wakes the entity up.
	 */
	private void wakeUp() {
		this.entity.setSleeping(false);
	}
}
