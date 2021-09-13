package tgw.evolution.entities.ai;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.vector.Vector3d;
import tgw.evolution.entities.EntityGenericCreature;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class GoalRandomWalking extends Goal {
    protected final EntityGenericCreature creature;
    protected final double speed;
    protected int executionChance;
    protected boolean mustUpdate;
    protected double x;
    protected double y;
    protected double z;

    public GoalRandomWalking(EntityGenericCreature creatureIn, double speedIn) {
        this(creatureIn, speedIn, 120);
    }

    public GoalRandomWalking(EntityGenericCreature creatureIn, double speedIn, int chance) {
        this.creature = creatureIn;
        this.speed = speedIn;
        this.executionChance = chance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        if (this.creature.isDead()) {
            return false;
        }
        return !this.creature.getNavigation().isDone();
    }

    @Override
    public boolean canUse() {
        if (this.creature.isDead() || this.creature.isSleeping()) {
            return false;
        }
        if (this.creature.isVehicle()) {
            return false;
        }
        if (!this.mustUpdate) {
            if (this.creature.getNoActionTime() >= 100) {
                return false;
            }
            if (this.creature.getRandom().nextInt(this.executionChance) != 0) {
                return false;
            }
        }
        Vector3d vec3d = this.getPosition();
        if (vec3d == null) {
            return false;
        }
        this.x = vec3d.x;
        this.y = vec3d.y;
        this.z = vec3d.z;
        this.mustUpdate = false;
        return true;
    }

    @Nullable
    protected Vector3d getPosition() {
        return RandomPositionGenerator.getPos(this.creature, 10, 7);
    }

    /**
     * Makes task to bypass chance
     */
    public void makeUpdate() {
        this.mustUpdate = true;
    }

    /**
     * Changes task random possibility for execution
     */
    public void setExecutionChance(int newchance) {
        this.executionChance = newchance;
    }

    @Override
    public void start() {
        this.creature.getNavigation().moveTo(this.x, this.y, this.z, this.speed);
    }
}