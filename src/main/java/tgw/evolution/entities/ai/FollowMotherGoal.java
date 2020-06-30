package tgw.evolution.entities.ai;

import java.util.List;

import net.minecraft.entity.ai.goal.Goal;
import tgw.evolution.entities.AnimalEntity;

public class FollowMotherGoal extends Goal {
	private final AnimalEntity childEntity;
	private AnimalEntity motherEntity;
	private final double moveSpeed;
	private int delayCounter;

	public FollowMotherGoal(AnimalEntity animal, double speed) {
		this.childEntity = animal;
		this.moveSpeed = speed;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (!this.childEntity.isChild()) {
			return false;
		}
		List<AnimalEntity> list = this.childEntity.world.getEntitiesWithinAABB(this.childEntity.getFemaleClass(), this.childEntity.getBoundingBox().grow(8.0D, 4.0D, 8.0D));
		AnimalEntity animalentity = null;
		double d0 = Double.MAX_VALUE;
		for(AnimalEntity animalentity1 : list) {
			if (!animalentity1.isChild()) {
				double d1 = this.childEntity.getDistanceSq(animalentity1);
				if (!(d1 > d0)) {
					d0 = d1;
					animalentity = animalentity1;
				}
			}
		}
		if (animalentity == null) {
			return false;
		}
        if (d0 < 9.0D) {
            return false;
        }
        this.motherEntity = animalentity;
        return true;
    }

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		if (!this.childEntity.isChild()) {
			return false;
		}
        if (!this.motherEntity.isAlive()) {
            return false;
        }
        double d0 = this.childEntity.getDistanceSq(this.motherEntity);
        return !(d0 < 9.0D) && !(d0 > 256.0D);
    }

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		this.delayCounter = 0;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	@Override
	public void resetTask() {
		this.motherEntity = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	@Override
	public void tick() {
		if (--this.delayCounter <= 0) {
			this.delayCounter = 10;
			this.childEntity.getNavigator().tryMoveToEntityLiving(this.motherEntity, this.moveSpeed);
		}
	}
}