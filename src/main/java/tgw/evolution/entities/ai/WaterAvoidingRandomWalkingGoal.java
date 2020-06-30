package tgw.evolution.entities.ai;

import javax.annotation.Nullable;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;
import tgw.evolution.entities.CreatureEntity;

public class WaterAvoidingRandomWalkingGoal extends RandomWalkingGoal {
	protected final float probability;

	public WaterAvoidingRandomWalkingGoal(CreatureEntity creature, double speedIn) {
		this(creature, speedIn, 0.001F);
	}

	public WaterAvoidingRandomWalkingGoal(CreatureEntity creature, double speedIn, float probabilityIn) {
		super(creature, speedIn);
		this.probability = probabilityIn;
	}

	@Override
	@Nullable
	protected Vec3d getPosition() {
		if (this.creature.isInWaterOrBubbleColumn()) {
			Vec3d vec3d = RandomPositionGenerator.getLandPos(this.creature, 15, 7);
			return vec3d == null ? super.getPosition() : vec3d;
		}
		return this.creature.getRNG().nextFloat() >= this.probability ? RandomPositionGenerator.getLandPos(this.creature, 10, 7) : super.getPosition();
	}
}