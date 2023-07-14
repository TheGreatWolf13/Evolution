//package tgw.evolution.entities.ai;
//
//import net.minecraft.world.entity.ai.util.LandRandomPos;
//import net.minecraft.world.phys.Vec3;
//import tgw.evolution.entities.EntityGenericCreature;
//
//import org.jetbrains.annotations.Nullable;
//
//public class GoalWaterAvoidingRandomWalking extends GoalRandomWalking {
//    protected final float probability;
//
//    public GoalWaterAvoidingRandomWalking(EntityGenericCreature creature, double speedIn) {
//        this(creature, speedIn, 0.001F);
//    }
//
//    public GoalWaterAvoidingRandomWalking(EntityGenericCreature creature, double speedIn, float probabilityIn) {
//        super(creature, speedIn);
//        this.probability = probabilityIn;
//    }
//
//    @Override
//    @Nullable
//    protected Vec3 getPosition() {
//        if (this.creature.isInWaterOrBubble()) {
//            Vec3 vec3d = LandRandomPos.getPos(this.creature, 15, 7);
//            return vec3d == null ? super.getPosition() : vec3d;
//        }
//        return this.creature.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.creature, 10, 7) : super.getPosition();
//    }
//}