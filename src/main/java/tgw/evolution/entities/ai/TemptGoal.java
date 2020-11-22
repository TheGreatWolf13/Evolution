//package tgw.evolution.entities.ai;
//
//import net.minecraft.entity.EntityPredicate;
//import net.minecraft.entity.ai.goal.Goal;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.pathfinding.GroundPathNavigator;
//import tgw.evolution.entities.AnimalEntity;
//
//import java.util.EnumSet;
//
//public class TemptGoal extends Goal {
//
//    private static final EntityPredicate ENTITY_PREDICATE = new EntityPredicate().setDistance(10.0D).allowInvulnerable().allowFriendlyFire()
//    .setSkipAttackChecks().setLineOfSiteRequired();
//    protected final AnimalEntity creature;
//    private final double speed;
//    private final boolean scaredByPlayerMovement;
//    protected PlayerEntity closestPlayer;
//    private double targetX;
//    private double targetY;
//    private double targetZ;
//    private double pitch;
//    private double yaw;
//    private int delayTemptCounter;
//
//    public TemptGoal(AnimalEntity creatureIn, double speedIn, boolean scaredByPlayerMovement) {
//        this.creature = creatureIn;
//        this.speed = speedIn;
//        this.scaredByPlayerMovement = scaredByPlayerMovement;
//        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
//        if (!(creatureIn.getNavigator() instanceof GroundPathNavigator)) {
//            throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
//        }
//    }
//
//    @Override
//    public boolean shouldExecute() {
//        if (this.creature.isDead() || this.creature.isSleeping()) {
//            return false;
//        }
//        if (this.delayTemptCounter > 0) {
//            --this.delayTemptCounter;
//            return false;
//        }
//        this.closestPlayer = this.creature.world.getClosestPlayer(ENTITY_PREDICATE, this.creature);
//        if (this.closestPlayer == null) {
//            return false;
//        }
//        return AnimalEntity.isEdibleItem(this.closestPlayer.getHeldItemMainhand()) || AnimalEntity.isEdibleItem(this.closestPlayer
//        .getHeldItemOffhand());
//    }
//
//    @Override
//    public boolean shouldContinueExecuting() {
//        if (this.creature.isDead()) {
//            return false;
//        }
//        if (this.scaredByPlayerMovement) {
//            if (this.creature.getDistanceSq(this.closestPlayer) < 36.0D) {
//                if (this.closestPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
//                    return false;
//                }
//                if (Math.abs(this.closestPlayer.rotationPitch - this.pitch) > 5.0D || Math.abs(this.closestPlayer.rotationYaw - this.yaw) > 5.0D) {
//                    return false;
//                }
//            }
//            else {
//                this.targetX = this.closestPlayer.posX;
//                this.targetY = this.closestPlayer.posY;
//                this.targetZ = this.closestPlayer.posZ;
//            }
//            this.pitch = this.closestPlayer.rotationPitch;
//            this.yaw = this.closestPlayer.rotationYaw;
//        }
//        return this.shouldExecute();
//    }
//
//    protected boolean isScaredByPlayerMovement() {
//        return this.scaredByPlayerMovement;
//    }
//
//    @Override
//    public void startExecuting() {
//        this.targetX = this.closestPlayer.posX;
//        this.targetY = this.closestPlayer.posY;
//        this.targetZ = this.closestPlayer.posZ;
//    }
//
//    @Override
//    public void resetTask() {
//        this.closestPlayer = null;
//        this.creature.getNavigator().clearPath();
//        this.delayTemptCounter = 100;
//    }
//
//    @Override
//    public void tick() {
//        this.creature.getLookController().setLookPositionWithEntity(this.closestPlayer, this.creature.getHorizontalFaceSpeed() + 20, this
//        .creature.getVerticalFaceSpeed());
//        if (this.creature.getDistanceSq(this.closestPlayer) < 6.25D) {
//            this.creature.getNavigator().clearPath();
//        }
//        else {
//            this.creature.getNavigator().tryMoveToEntityLiving(this.closestPlayer, this.speed);
//        }
//    }
//}