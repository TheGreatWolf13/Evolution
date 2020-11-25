package tgw.evolution.entities.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import tgw.evolution.entities.EntityGenericAgeable;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class PanicGoal extends Goal {

    protected final EntityGenericAgeable creature;
    protected final double speed;
    protected double randPosX;
    protected double randPosY;
    protected double randPosZ;

    public PanicGoal(EntityGenericAgeable creature, double speedIn) {
        this.creature = creature;
        this.speed = speedIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isDead()) {
            return false;
        }
        if (this.creature.getRevengeTarget() == null && !this.creature.isBurning()) {
            return false;
        }
        if (this.creature.isBurning()) {
            BlockPos blockpos = PanicGoal.getRandPos(this.creature.world, this.creature, 5, 4);
            if (blockpos != null) {
                this.randPosX = blockpos.getX();
                this.randPosY = blockpos.getY();
                this.randPosZ = blockpos.getZ();
                return true;
            }
        }
        return this.findRandomPosition();
    }

    @Nullable
    protected static BlockPos getRandPos(IBlockReader worldIn, Entity entityIn, int horizontalRange, int verticalRange) {
        BlockPos entityPos = new BlockPos(entityIn);
        int entityX = entityPos.getX();
        int entityY = entityPos.getY();
        int entityZ = entityPos.getZ();
        float maxRange = horizontalRange * horizontalRange * verticalRange * 2;
        BlockPos.MutableBlockPos chosenPos = null;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = entityX - horizontalRange; x <= entityX + horizontalRange; ++x) {
            for (int y = entityY - verticalRange; y <= entityY + verticalRange; ++y) {
                for (int z = entityZ - horizontalRange; z <= entityZ + horizontalRange; ++z) {
                    mutablePos.setPos(x, y, z);
                    if (worldIn.getFluidState(mutablePos).isTagged(FluidTags.WATER)) {
                        float distanceToBlock = (x - entityX) * (x - entityX) + (y - entityY) * (y - entityY) + (z - entityZ) * (z - entityZ);
                        if (distanceToBlock < maxRange) {
                            maxRange = distanceToBlock;
                            //noinspection ObjectAllocationInLoop
                            chosenPos = chosenPos == null ? chosenPos = new BlockPos.MutableBlockPos(mutablePos) : chosenPos.setPos(mutablePos);
                        }
                    }
                }
            }
        }
        return chosenPos;
    }

    protected boolean findRandomPosition() {
        Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.creature, 5, 4);
        if (vec3d == null) {
            return false;
        }
        this.randPosX = vec3d.x;
        this.randPosY = vec3d.y;
        this.randPosZ = vec3d.z;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.creature.isDead()) {
            return false;
        }
        return !this.creature.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
    }
}