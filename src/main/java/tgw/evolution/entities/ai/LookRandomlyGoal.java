package tgw.evolution.entities.ai;

import net.minecraft.entity.ai.goal.Goal;
import tgw.evolution.entities.EntityGenericCreature;

import java.util.EnumSet;

public class LookRandomlyGoal extends Goal {
    private final EntityGenericCreature entity;
    private double lookX;
    private double lookZ;
    private int idleTime;

    public LookRandomlyGoal(EntityGenericCreature entitylivingIn) {
        this.entity = entitylivingIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        return this.entity.getRNG().nextFloat() < 0.02F && !this.entity.isDead() && !this.entity.isSleeping();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.idleTime >= 0 && !this.entity.isDead() && !this.entity.isSleeping();
    }

    @Override
    public void startExecuting() {
        double d0 = Math.PI * 2D * this.entity.getRNG().nextDouble();
        this.lookX = Math.cos(d0);
        this.lookZ = Math.sin(d0);
        this.idleTime = 20 + this.entity.getRNG().nextInt(20);
    }

    @Override
    public void tick() {
        --this.idleTime;
        this.entity.getLookController()
                   .setLookPosition(this.entity.posX + this.lookX, this.entity.posY + this.entity.getEyeHeight(), this.entity.posZ + this.lookZ);
    }
}