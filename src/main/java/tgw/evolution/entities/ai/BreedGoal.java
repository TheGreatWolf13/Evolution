//package tgw.evolution.entities.ai;
//
//import java.util.EnumSet;
//import java.util.List;
//import javax.annotation.Nullable;
//import net.minecraft.entity.EntityPredicate;
//import net.minecraft.entity.ai.goal.Goal;
//import net.minecraft.entity.item.ExperienceOrbEntity;
//import net.minecraft.world.GameRules;
//import net.minecraft.world.World;
//import net.minecraftforge.common.MinecraftForge;
//import tgw.evolution.entities.AgeableEntity;
//import tgw.evolution.entities.AnimalEntity;
//import tgw.evolution.entities.event.BabyEntitySpawnEvent;
//
//public class BreedGoal extends Goal {
//	private static final EntityPredicate field_220689_d = new EntityPredicate().setDistance(8.0D).allowInvulnerable().allowFriendlyFire()
//	.setLineOfSiteRequired();
//	protected final AnimalEntity animal;
//	private final Class<? extends AnimalEntity> mateClass;
//	protected final World world;
//	protected AnimalEntity mate;
//	private int spawnBabyDelay;
//	private final double moveSpeed;
//
//	public BreedGoal(AnimalEntity animal, double speedIn) {
//		this(animal, speedIn, animal.getPartnerClass());
//	}
//
//	public BreedGoal(AnimalEntity animalEntity, double moveSpeed, Class<? extends AnimalEntity> mateClass) {
//		this.animal = animalEntity;
//		this.world = animalEntity.world;
//		this.mateClass = mateClass;
//		this.moveSpeed = moveSpeed;
//		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
//	}
//
//	@Override
//	public boolean shouldExecute() {
//		if (this.animal.isDead() || !this.animal.isInLove() || this.animal.isSleeping()) {
//			return false;
//		}
//		this.mate = this.getNearbyMate();
//		return this.mate != null;
//	}
//
//	@Override
//	public boolean shouldContinueExecuting() {
//		return !this.mate.isDead() && this.mate.isInLove() && this.spawnBabyDelay < 60;
//	}
//
//	@Override
//	public void resetTask() {
//		this.mate = null;
//		this.spawnBabyDelay = 0;
//	}
//
//	/**
//	 * Keep ticking a continuous task that has already been started
//	 */
//	@Override
//	public void tick() {
//		this.animal.getLookController().setLookPositionWithEntity(this.mate, 10.0F, this.animal.getVerticalFaceSpeed());
//		this.animal.getNavigator().tryMoveToEntityLiving(this.mate, this.moveSpeed);
//		++this.spawnBabyDelay;
//		if (this.spawnBabyDelay >= 60 && this.animal.getDistanceSq(this.mate) < 9.0D) {
//			this.spawnBaby();
//		}
//	}
//
//	/**
//	 * Loops through nearby animals and finds another animal of the same type that can be mated with. Returns the first
//	 * valid mate found.
//	 */
//	@Nullable
//	private AnimalEntity getNearbyMate() {
//		List<AnimalEntity> list = this.world.getTargettableEntitiesWithinAABB(this.mateClass, field_220689_d, this.animal, this.animal
//		.getBoundingBox().grow(8.0D));
//		double d0 = Double.MAX_VALUE;
//		AnimalEntity animalentity = null;
//		for(AnimalEntity animalentity1 : list) {
//			if (this.animal.canMateWith(animalentity1) && this.animal.getDistanceSq(animalentity1) < d0) {
//				animalentity = animalentity1;
//				d0 = this.animal.getDistanceSq(animalentity1);
//			}
//		}
//		return animalentity;
//	}
//
//	/**
//	 * Spawns a baby animal of the same type.
//	 */
//	protected void spawnBaby() {
//		AgeableEntity ageableentity = this.animal.createChild(this.mate);
//		BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(this.animal, this.mate, ageableentity);
//		boolean cancelled = MinecraftForge.EVENT_BUS.post(event);
//		ageableentity = event.getChild();
//		if (cancelled) {
//			//TODO love
//			//Reset the "inLove" state for the animals
////			this.animal.modifyFood(-100);
////			this.mate.modifyFood(-10);
//			this.animal.resetInLove();
//			this.mate.resetInLove();
//			return;
//		}
//		if (ageableentity != null) {
//			//TODO love
////			this.animal.modifyFood(-100);
////			this.mate.modifyFood(-10);
//			this.animal.resetInLove();
//			this.mate.resetInLove();
//			ageableentity.setAge(0);
//			ageableentity.setLocationAndAngles(this.animal.posX, this.animal.posY, this.animal.posZ, 0.0F, 0.0F);
//			this.world.addEntity(ageableentity);
//			this.world.setEntityState(this.animal, (byte)18);
//			if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
//				this.world.addEntity(new ExperienceOrbEntity(this.world, this.animal.posX, this.animal.posY, this.animal.posZ, this.animal.getRNG()
//				.nextInt(7) + 1));
//			}
//		}
//	}
//}