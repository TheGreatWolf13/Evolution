package tgw.evolution.entities.ai;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.vector.Vector3d;
import tgw.evolution.entities.EntityGenericCreature;

import javax.annotation.Nullable;

public class GoalWaterAvoidingRandomWalking extends GoalRandomWalking {
    protected final float probability;

    public GoalWaterAvoidingRandomWalking(EntityGenericCreature creature, double speedIn) {
        this(creature, speedIn, 0.001F);
    }

    public GoalWaterAvoidingRandomWalking(EntityGenericCreature creature, double speedIn, float probabilityIn) {
        super(creature, speedIn);
        this.probability = probabilityIn;
    }

    @Override
    @Nullable
    protected Vector3d getPosition() {
        if (this.creature.isInWaterOrBubble()) {
            Vector3d vec3d = RandomPositionGenerator.getLandPos(this.creature, 15, 7);
            return vec3d == null ? super.getPosition() : vec3d;
        }
        return this.creature.getRandom().nextFloat() >= this.probability ?
               RandomPositionGenerator.getLandPos(this.creature, 10, 7) :
               super.getPosition();
    }
}