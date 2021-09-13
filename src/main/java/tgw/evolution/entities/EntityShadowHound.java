//package tgw.evolution.entities;
//
//import net.minecraft.entity.EntityType;
//import net.minecraft.entity.SharedMonsterAttributes;
//import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.world.World;
//import tgw.evolution.entities.ai.*;
//import tgw.evolution.init.EvolutionEntities;
//import tgw.evolution.util.Time;
//
//public class EntityShadowHound extends MonsterEntity {
//
//    public boolean isInAttackMode;
//    public int attackCooldown;
//    public short hideCooldown;
//
//    public EntityShadowHound(EntityType<EntityShadowHound> type, World worldIn) {
//        super(type, worldIn);
//    }
//
//    public EntityShadowHound(World worldIn) {
//        super(EvolutionEntities.SHADOWHOUND.get(), worldIn);
//    }
//
//    @Override
//    protected void registerAttributes() {
//        super.registerAttributes();
//        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6);
//        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23);
//        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8);
//        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32);
//    }
//
//    @Override
//    protected void registerGoals() {
//        this.goalSelector.addGoal(5, new GoalWaterAvoidingRandomWalking(this, 1.0D));
//        this.goalSelector.addGoal(6, new GoalLookRandomly(this));
//        this.goalSelector.addGoal(3, new ShadowHoundAttackGoal(this, 2.5, false));
//        this.goalSelector.addGoal(2, new AvoidShadowHoundEntityGoal<>(this, PlayerEntity.class, 16, 1.4, 1.8));
//        this.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
//        this.goalSelector.addGoal(1, new ShadowHoundHideInBlockGoal(this));
//    }
//
//    @Override
//    public int skeletonTime() {
//        return 12 * Time.HOUR_IN_TICKS;
//    }
//
//    @Override
//    public void livingTick() {
//        this.attackCooldown = this.attackCooldown == 0 ? 0 : this.attackCooldown--;
//        this.hideCooldown = this.hideCooldown == 0 ? 0 : this.hideCooldown--;
//        super.livingTick();
//    }
//
//    @Override
//    public void writeAdditional(CompoundNBT compound) {
//        compound.putShort("HideCooldown", this.hideCooldown);
//        super.writeAdditional(compound);
//    }
//
//    @Override
//    public void readAdditional(CompoundNBT compound) {
//        this.hideCooldown = compound.getShort("HideCooldown");
//        super.readAdditional(compound);
//    }
//}
