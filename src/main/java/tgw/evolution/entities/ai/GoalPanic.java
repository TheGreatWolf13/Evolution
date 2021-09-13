package tgw.evolution.entities.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import tgw.evolution.entities.EntityGenericAgeable;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class GoalPanic extends Goal {

    protected final EntityGenericAgeable creature;
    protected final double speed;
    protected boolean isRunning;
    protected double randPosX;
    protected double randPosY;
    protected double randPosZ;

    public GoalPanic(EntityGenericAgeable creature, double speedIn) {
        this.creature = creature;
        this.speed = speedIn;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Nullable
    protected static BlockPos lookForWater(IBlockReader world, Entity entity, int horizontalRange, int verticalRange) {
        BlockPos entityPos = entity.blockPosition();
        int entityX = entityPos.getX();
        int entityY = entityPos.getY();
        int entityZ = entityPos.getZ();
        float maxRange = horizontalRange * horizontalRange * verticalRange * 2;
        BlockPos.Mutable chosenPos = null;
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (int x = entityX - horizontalRange; x <= entityX + horizontalRange; ++x) {
            for (int y = entityY - verticalRange; y <= entityY + verticalRange; ++y) {
                for (int z = entityZ - horizontalRange; z <= entityZ + horizontalRange; ++z) {
                    mutablePos.set(x, y, z);
                    if (world.getFluidState(mutablePos).is(FluidTags.WATER)) {
                        float distanceToBlock = (x - entityX) * (x - entityX) + (y - entityY) * (y - entityY) + (z - entityZ) * (z - entityZ);
                        if (distanceToBlock < maxRange) {
                            maxRange = distanceToBlock;
                            //noinspection ObjectAllocationInLoop
                            chosenPos = chosenPos == null ? new BlockPos.Mutable().set(mutablePos) : chosenPos.set(mutablePos);
                        }
                    }
                }
            }
        }
        return chosenPos;
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
        if (this.creature.isDead()) {
            return false;
        }
        if (this.creature.getLastHurtByMob() == null && !this.creature.isOnFire()) {
            return false;
        }
        if (this.creature.isOnFire()) {
            BlockPos blockpos = lookForWater(this.creature.level, this.creature, 5, 4);
            if (blockpos != null) {
                this.randPosX = blockpos.getX();
                this.randPosY = blockpos.getY();
                this.randPosZ = blockpos.getZ();
                return true;
            }
        }
        return this.findRandomPosition();
    }

    protected boolean findRandomPosition() {
        Vector3d vec3d = RandomPositionGenerator.getPos(this.creature, 5, 4);
        if (vec3d == null) {
            return false;
        }
        this.randPosX = vec3d.x;
        this.randPosY = vec3d.y;
        this.randPosZ = vec3d.z;
        return true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void start() {
        this.creature.getNavigation().moveTo(this.randPosX, this.randPosY, this.randPosZ, this.speed);
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.isRunning = false;
    }
}