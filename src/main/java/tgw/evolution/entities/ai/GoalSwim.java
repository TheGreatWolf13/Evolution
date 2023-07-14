//package tgw.evolution.entities.ai;
//
//import net.minecraft.tags.FluidTags;
//import net.minecraft.world.entity.ai.goal.Goal;
//import tgw.evolution.entities.EntityGenericCreature;
//
//import java.util.EnumSet;
//
//public class GoalSwim extends Goal {
//    private final EntityGenericCreature entity;
//
//    public GoalSwim(EntityGenericCreature entity) {
//        this.entity = entity;
//        this.setFlags(EnumSet.of(Goal.Flag.JUMP));
//        entity.getNavigation().setCanFloat(true);
//    }
//
//    @Override
//    public boolean canUse() {
//        if (this.entity.isDead()) {
//            return false;
//        }
//        return this.entity.isInWater() && this.entity.getFluidHeight(FluidTags.WATER) > this.entity.getFluidJumpThreshold() || this.entity
//        .isInLava();
//    }
//
//    @Override
//    public void tick() {
//        if (this.entity.getRandom().nextFloat() < 0.8F) {
//            this.entity.getJumpControl().jump();
//        }
//    }
//}