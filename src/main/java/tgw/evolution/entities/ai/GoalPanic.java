package tgw.evolution.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.entities.EntityGenericAgeable;

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
    protected static BlockPos lookForWater(BlockGetter level, EntityAccess entity, int horizontalRange, int verticalRange) {
        BlockPos entityPos = entity.blockPosition();
        int entityX = entityPos.getX();
        int entityY = entityPos.getY();
        int entityZ = entityPos.getZ();
        float maxRange = horizontalRange * horizontalRange * verticalRange * 2;
        BlockPos.MutableBlockPos chosenPos = null;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = entityX - horizontalRange; x <= entityX + horizontalRange; ++x) {
            for (int y = entityY - verticalRange; y <= entityY + verticalRange; ++y) {
                for (int z = entityZ - horizontalRange; z <= entityZ + horizontalRange; ++z) {
                    mutablePos.set(x, y, z);
                    if (level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                        float distanceToBlock = (x - entityX) * (x - entityX) + (y - entityY) * (y - entityY) + (z - entityZ) * (z - entityZ);
                        if (distanceToBlock < maxRange) {
                            maxRange = distanceToBlock;
                            //noinspection ObjectAllocationInLoop
                            chosenPos = chosenPos == null ? new BlockPos.MutableBlockPos().set(mutablePos) : chosenPos.set(mutablePos);
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
        Vec3 vec3d = DefaultRandomPos.getPos(this.creature, 5, 4);
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