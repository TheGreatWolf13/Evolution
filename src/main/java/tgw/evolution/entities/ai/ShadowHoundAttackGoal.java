package tgw.evolution.entities.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import tgw.evolution.entities.EntityShadowHound;

import java.util.EnumSet;

public class ShadowHoundAttackGoal extends Goal {

    protected final EntityShadowHound attacker;
    private final double speedTowardsTarget;
    private final boolean longMemory;
    protected int attackTick;
    private Path path;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    private long timeOfLastAttack;
    private int failedPathFindingPenalty;

    public ShadowHoundAttackGoal(EntityShadowHound creature, double speedIn, boolean useLongMemory) {
        this.attacker = creature;
        this.speedTowardsTarget = speedIn;
        this.longMemory = useLongMemory;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean shouldExecute() {
        if (this.attacker.isDead()) {
            return false;
        }
        if (!this.attacker.isInAttackMode || this.attacker.attackCooldown > 0) {
            return false;
        }
        long i = this.attacker.world.getGameTime();
        if (i - this.timeOfLastAttack < 20L) {
            return false;
        }
        this.timeOfLastAttack = i;
        LivingEntity livingentity = this.attacker.getAttackTarget();
        if (livingentity == null) {
            return false;
        }
        if (!livingentity.isAlive()) {
            return false;
        }
        if (--this.delayCounter <= 0) {
            /*.func_75494_a = getPathToLivingEntity()*/
            this.path = this.attacker.getNavigator().func_75494_a(livingentity, 0);
            this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
            return this.path != null;
        }
        return true;

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
        if (this.attacker.isDead()) {
            return false;
        }
        LivingEntity livingentity = this.attacker.getAttackTarget();
        if (livingentity == null) {
            return false;
        }
        if (!livingentity.isAlive()) {
            return false;
        }
        if (!this.longMemory) {
            return !this.attacker.getNavigator().noPath();
        }
        return this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(livingentity)) && (!(livingentity instanceof PlayerEntity) || !livingentity.isSpectator() && !((PlayerEntity) livingentity).isCreative());
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        this.attacker.getNavigator().setPath(this.path, this.speedTowardsTarget);
        this.attacker.setAggroed(true);
        this.delayCounter = 0;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void resetTask() {
        LivingEntity livingentity = this.attacker.getAttackTarget();
        if (!EntityPredicates.CAN_AI_TARGET.test(livingentity)) {
            this.attacker.setAttackTarget(null);
        }
        this.attacker.setAggroed(false);
        this.attacker.getNavigator().clearPath();
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void tick() {
        LivingEntity livingentity = this.attacker.getAttackTarget();
        if (livingentity == null) {
            return;
        }
        this.attacker.getLookController().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);
        double d0 = this.attacker.getDistanceSq(livingentity.posX, livingentity.getBoundingBox().minY, livingentity.posZ);
        --this.delayCounter;
        if ((this.longMemory || this.attacker.getEntitySenses().canSee(livingentity)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || livingentity.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.attacker.getRNG().nextFloat() < 0.05F)) {
            this.targetX = livingentity.posX;
            this.targetY = livingentity.getBoundingBox().minY;
            this.targetZ = livingentity.posZ;
            this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
            this.delayCounter += this.failedPathFindingPenalty;
            if (this.attacker.getNavigator().getPath() != null) {
                PathPoint finalPathPoint = this.attacker.getNavigator().getPath().getFinalPathPoint();
                if (finalPathPoint != null && livingentity.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1) {
                    this.failedPathFindingPenalty = 0;
                }
                else {
                    this.failedPathFindingPenalty += 10;
                }
            }
            else {
                this.failedPathFindingPenalty += 10;
            }
            if (d0 > 1024.0D) {
                this.delayCounter += 10;
            }
            else if (d0 > 256.0D) {
                this.delayCounter += 5;
            }
            if (!this.attacker.getNavigator().tryMoveToEntityLiving(livingentity, this.speedTowardsTarget)) {
                this.delayCounter += 15;
            }
        }
        this.attackTick = Math.max(this.attackTick - 1, 0);
        this.checkAndPerformAttack(livingentity, d0);
    }

    protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);
        if (distToEnemySqr <= d0 && this.attackTick <= 0) {
            this.attackTick = 20;
            this.attacker.swingArm(Hand.MAIN_HAND);
            this.attacker.attackEntityAsMob(enemy);
            this.attacker.attackCooldown = 100;
        }
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        return this.attacker.getWidth() * 2.0F * this.attacker.getWidth() * 2.0F + attackTarget.getWidth();
    }
}