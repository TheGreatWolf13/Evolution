//package tgw.evolution.entities.ai;
//
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityPredicate;
//import net.minecraft.entity.EntityType;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.entity.ai.RandomPositionGenerator;
//import net.minecraft.entity.ai.goal.Goal;
//import net.minecraft.pathfinding.Path;
//import net.minecraft.pathfinding.PathNavigator;
//import net.minecraft.util.EntityPredicates;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.Vec3d;
//import tgw.evolution.entities.CreatureEntity;
//import tgw.evolution.entities.EntityShadowHound;
//import tgw.evolution.init.EvolutionEntities;
//
//import java.util.EnumSet;
//import java.util.List;
//import java.util.function.Predicate;
//
//public class AvoidShadowHoundEntityGoal<T extends LivingEntity> extends Goal {
//
//    protected final EntityShadowHound entity;
//    protected final float avoidDistance;
//    protected final PathNavigator navigation;
//    protected final Class<T> classToAvoid;
//    protected final Predicate<LivingEntity> avoidTargetSelector;
//    protected final Predicate<LivingEntity> field_203784_k;
//    private final double farSpeed;
//    private final double nearSpeed;
//    private final EntityPredicate builtTargetSelector;
//    protected T avoidTarget;
//    protected Path path;
//
//    public AvoidShadowHoundEntityGoal(EntityShadowHound entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double
//    nearSpeedIn) {
//        this(entityIn, classToAvoidIn, entity -> true, avoidDistanceIn, farSpeedIn, nearSpeedIn, EntityPredicates.CAN_AI_TARGET::test);
//    }
//
//    public AvoidShadowHoundEntityGoal(EntityShadowHound entityIn, Class<T> avoidClass, Predicate<LivingEntity> targetPredicate, float distance,
//    double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> p_i48859_9_) {
//        this.entity = entityIn;
//        this.classToAvoid = avoidClass;
//        this.avoidTargetSelector = targetPredicate;
//        this.avoidDistance = distance;
//        this.farSpeed = nearSpeedIn;
//        this.nearSpeed = farSpeedIn;
//        this.field_203784_k = p_i48859_9_;
//        this.navigation = entityIn.getNavigator();
//        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
//        this.builtTargetSelector = new EntityPredicate().setDistance(distance).setCustomPredicate(p_i48859_9_.and(targetPredicate));
//    }
//
//    /**
//     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
//     * method as well.
//     */
//    @Override
//    public boolean shouldExecute() {
//        if (this.entity.isDead()) {
//            return false;
//        }
//        List<Entity> list = this.entity.world.getEntitiesInAABBexcluding(this.entity, new AxisAlignedBB(this.entity.posX - 12, this.entity.posY -
//        2, this.entity.posZ - 12, this.entity.posX + 12, this.entity.posY + 2, this.entity.posZ + 12), entityIn -> {
//            EntityType<?> type = entityIn.getType();
//            return type == EvolutionEntities.SHADOWHOUND.get() && !((CreatureEntity) entityIn).isDead();
//        });
//        if (list.size() >= 2 && this.entity.attackCooldown == 0) {
//            for (Entity entity : list) {
//                ((EntityShadowHound) entity).isInAttackMode = true;
//            }
//            this.entity.isInAttackMode = true;
//            return false;
//        }
//        this.avoidTarget = this.entity.world.func_225318_b(this.classToAvoid, this.builtTargetSelector, this.entity, this.entity.posX, this
//        .entity.posY, this.entity.posZ, this.entity.getBoundingBox().grow(this.avoidDistance, 3.0D, this.avoidDistance));
//        if (this.avoidTarget == null) {
//            return false;
//        }
//        Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 16, 7, new Vec3d(this.avoidTarget.posX, this.avoidTarget
//        .posY, this.avoidTarget.posZ));
//        if (vec3d == null) {
//            return false;
//        }
//        if (this.avoidTarget.getDistanceSq(vec3d.x, vec3d.y, vec3d.z) < this.avoidTarget.getDistanceSq(this.entity)) {
//            return false;
//        }
//        this.path = this.navigation.func_225466_a(vec3d.x, vec3d.y, vec3d.z, 0);
//        return this.path != null;
//    }
//
//    /**
//     * Returns whether an in-progress EntityAIBase should continue executing
//     */
//    @Override
//    public boolean shouldContinueExecuting() {
//        if (this.entity.isDead()) {
//            return false;
//        }
//        List<Entity> list = this.entity.world.getEntitiesInAABBexcluding(this.entity, new AxisAlignedBB(this.entity.posX - 12, this.entity.posY -
//        2, this.entity.posZ - 12, this.entity.posX + 12, this.entity.posY + 2, this.entity.posZ + 12), entityIn -> {
//            EntityType<?> type = entityIn.getType();
//            return type == EvolutionEntities.SHADOWHOUND.get() && !((CreatureEntity) entityIn).isDead();
//        });
//        if (list.size() >= 2 && this.entity.attackCooldown == 0) {
//            for (Entity entity : list) {
//                ((EntityShadowHound) entity).isInAttackMode = true;
//            }
//            this.entity.isInAttackMode = true;
//            return false;
//        }
//        return !this.navigation.noPath();
//    }
//
//    /**
//     * Execute a one shot task or start executing a continuous task
//     */
//    @Override
//    public void startExecuting() {
//        this.navigation.setPath(this.path, this.farSpeed);
//    }
//
//    /**
//     * Reset the task's internal state. Called when this task is interrupted by another one
//     */
//    @Override
//    public void resetTask() {
//        this.avoidTarget = null;
//    }
//
//    /**
//     * Keep ticking a continuous task that has already been started
//     */
//    @Override
//    public void tick() {
//        this.entity.isInAttackMode = false;
//        if (this.entity.isDead()) {
//            this.resetTask();
//            this.navigation.clearPath();
//            return;
//        }
//        if (this.entity.getDistanceSq(this.avoidTarget) < 49.0D) {
//            this.entity.getNavigator().setSpeed(this.nearSpeed);
//        }
//        else {
//            this.entity.getNavigator().setSpeed(this.farSpeed);
//        }
//    }
//}