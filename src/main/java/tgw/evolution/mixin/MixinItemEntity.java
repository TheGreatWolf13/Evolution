package tgw.evolution.mixin;

import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {

    @Shadow private int age;
    @Shadow private int pickupDelay;

    public MixinItemEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    protected abstract boolean isMergable();

    @Shadow
    protected abstract void mergeWithNeighbours();

    @Shadow
    protected abstract void setUnderLavaMovement();

    @Shadow
    protected abstract void setUnderwaterMovement();

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
            Vec3 vec3 = this.getDeltaMovement();
            float f = this.getEyeHeight() - 0.111_111_11F;
            if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > f) {
                this.setUnderwaterMovement();
            }
            else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > f) {
                this.setUnderLavaMovement();
            }
            else if (!this.isNoGravity()) {
                Vec3 deltaMovement = this.getDeltaMovement();
                this.setDeltaMovement(deltaMovement.x, deltaMovement.y - 0.04, deltaMovement.z);
            }
            if (this.level.isClientSide) {
                this.noPhysics = false;
            }
            else {
                this.noPhysics = !this.level.noCollision(this, this.getBoundingBox().deflate(1.0E-7));
                if (this.noPhysics) {
                    this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2, this.getZ());
                }
            }
            if (!this.onGround ||
                this.getDeltaMovement().horizontalDistanceSqr() > 1e-5 ||
                (this.tickCount + this.getId()) % 4 == 0) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                float g = 0.98F;
                if (this.onGround) {
                    int x = Mth.floor(this.getX());
                    int y = Mth.floor(this.getY() - 1);
                    int z = Mth.floor(this.getZ());
                    g = this.level.getBlockState_(x, y, z).getBlock().getFriction() * 0.98F;
                }
                this.setDeltaMovement(this.getDeltaMovement().multiply(g, 0.98, g));
                if (this.onGround) {
                    Vec3 vec32 = this.getDeltaMovement();
                    if (vec32.y < 0.0D) {
                        this.setDeltaMovement(vec32.multiply(1.0D, -0.5D, 1.0D));
                    }
                }
            }
            boolean bl = Mth.floor(this.xo) != Mth.floor(this.getX()) ||
                         Mth.floor(this.yo) != Mth.floor(this.getY()) ||
                         Mth.floor(this.zo) != Mth.floor(this.getZ());
            int i = bl ? 2 : 40;
            if (this.tickCount % i == 0 && !this.level.isClientSide && this.isMergable()) {
                this.mergeWithNeighbours();
            }
            if (this.age != Short.MIN_VALUE) {
                ++this.age;
            }
            this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
            if (!this.level.isClientSide) {
                double d = this.getDeltaMovement().subtract(vec3).lengthSqr();
                if (d > 0.01D) {
                    this.hasImpulse = true;
                }
            }
            if (!this.level.isClientSide && this.age >= 6_000) {
                this.discard();
            }
        }
    }
}
