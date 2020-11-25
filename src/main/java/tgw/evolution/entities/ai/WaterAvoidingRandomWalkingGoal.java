package tgw.evolution.entities.ai;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;
import tgw.evolution.entities.EntityGenericCreature;

import javax.annotation.Nullable;

public class WaterAvoidingRandomWalkingGoal extends RandomWalkingGoal {
    protected final float probability;

    public WaterAvoidingRandomWalkingGoal(EntityGenericCreature creature, double speedIn) {
        this(creature, speedIn, 0.001F);
    }

    public WaterAvoidingRandomWalkingGoal(EntityGenericCreature creature, double speedIn, float probabilityIn) {
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
        return this.creature.getRNG().nextFloat() >= this.probability ?
               RandomPositionGenerator.getLandPos(this.creature, 10, 7) :
               super.getPosition();
    }
}