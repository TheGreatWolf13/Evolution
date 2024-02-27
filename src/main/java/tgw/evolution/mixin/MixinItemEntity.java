package tgw.evolution.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.physics.Fluid;
import tgw.evolution.util.physics.Physics;
import tgw.evolution.util.time.RealTime;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {

    @Shadow @Final private static EntityDataAccessor<ItemStack> DATA_ITEM;
    @Shadow private int age;
    @Shadow private int pickupDelay;

    public MixinItemEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public double getBaseMass() {
        return 2;
    }

    @Shadow
    public abstract ItemStack getItem();

    @Override
    public short getLightEmission() {
        return this.getEntityData().get(DATA_ITEM).getLightEmission();
    }

    @Override
    public double getVolume() {
        return 0.25 * super.getVolume();
    }

    @Shadow
    protected abstract boolean isMergable();

    @Shadow
    protected abstract void mergeWithNeighbours();

    @Override
    @Overwrite
    public void tick() {
        if (this.getItem().isEmpty()) {
            this.discard();
        }
        else {
            super.tick();
            if (this.pickupDelay > 0 && this.pickupDelay != Short.MAX_VALUE) {
                --this.pickupDelay;
            }
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            //Physics
            Vec3 velocity = this.getDeltaMovement();
            double velX = velocity.x;
            double velY = velocity.y;
            double velZ = velocity.z;
            try (Physics physics = Physics.getInstance(this, this.isInWater() ? Fluid.WATER : this.isInLava() ? Fluid.LAVA : Fluid.AIR)) {
                double accY = 0;
                if (!this.isNoGravity()) {
                    accY += physics.calcAccGravity();
                }
                if (!this.isOnGround()) {
                    accY += physics.calcForceBuoyancy(this) / this.getBaseMass();
                }
                //Pseudo-forces
                double accCoriolisX = physics.calcAccCoriolisX();
                double accCoriolisY = physics.calcAccCoriolisY();
                double accCoriolisZ = physics.calcAccCoriolisZ();
                double accCentrifugalY = physics.calcAccCentrifugalY();
                double accCentrifugalZ = physics.calcAccCentrifugalZ();
                //Dissipative Forces
                double dissipativeX = 0;
                double dissipativeZ = 0;
                if (this.isOnGround() && (velX != 0 || velZ != 0)) {
                    double norm = Mth.fastInvSqrt(velX * velX + velZ * velZ);
                    double frictionAcc = physics.calcAccNormal() * physics.calcKineticFrictionCoef(this);
                    double frictionX = velX * norm * frictionAcc;
                    double frictionZ = velZ * norm * frictionAcc;
                    dissipativeX = frictionX;
                    if (Math.abs(dissipativeX) > Math.abs(velX)) {
                        dissipativeX = velX;
                    }
                    dissipativeZ = frictionZ;
                    if (Math.abs(dissipativeZ) > Math.abs(velZ)) {
                        dissipativeZ = velZ;
                    }
                }
                //Update Motion
                velX += -dissipativeX + accCoriolisX;
                velY += accY + accCoriolisY + accCentrifugalY;
                velZ += -dissipativeZ + accCoriolisZ + accCentrifugalZ;
            }
            this.setDeltaMovement(velX, velY, velZ);
            if (!this.onGround || this.getDeltaMovement().horizontalDistanceSqr() > 1e-5 || (this.tickCount + this.getId()) % 4 == 0) {
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
            //End of physics
            if (!this.level.isClientSide) {
                boolean changedBlock = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
                if (this.tickCount % (changedBlock ? 2 : 40) == 0 && this.isMergable()) {
                    this.mergeWithNeighbours();
                }
            }
            if (this.age != Short.MIN_VALUE) {
                ++this.age;
            }
            this.hasImpulse |= this.isInAnyFluid();
            if (!this.level.isClientSide) {
                Vec3 newVelocity = this.getDeltaMovement();
                double dx = newVelocity.x - velocity.x;
                double dy = newVelocity.y - velocity.y;
                double dz = newVelocity.z - velocity.z;
                if (dx * dx + dy * dy + dz * dz > 0.01) {
                    this.hasImpulse = true;
                }
                if (this.age >= 5 * RealTime.MIN_TO_TICKS) {
                    this.discard();
                }
            }
        }
    }
}
