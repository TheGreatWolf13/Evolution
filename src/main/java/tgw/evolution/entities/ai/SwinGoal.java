package tgw.evolution.entities.ai;

import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import tgw.evolution.entities.CreatureEntity;

public class SwinGoal extends Goal {
	private final CreatureEntity entity;

	public SwinGoal(CreatureEntity entityIn) {
		this.entity = entityIn;
		this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP));
		entityIn.getNavigator().setCanSwim(true);
	}

	@Override
	public boolean shouldExecute() {
		double d0 = this.entity.getEyeHeight() < 0.4D ? 0.2D : 0.4D;
		if (this.entity.isDead()) {
			return false;
		}
		return this.entity.isInWater() && this.entity.getSubmergedHeight() > d0 || this.entity.isInLava();
	}

	@Override
	public void tick() {
		if (this.entity.getRNG().nextFloat() < 0.8F) {
			this.entity.getJumpController().setJumping();
		}
	}
}