package tgw.evolution.util.physics;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.entities.projectiles.IAerodynamicEntity;
import tgw.evolution.items.IEvolutionItem;
import tgw.evolution.patches.IBlockPatch;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.ILocked;
import tgw.evolution.util.math.MathHelper;

public final class Physics implements ILocked {

    private static final ThreadLocal<Physics> CACHE = ThreadLocal.withInitial(Physics::new);

    /**
     * Sir Isaac Newton's Universal Gravitational Constant.
     */
    private static final double GRAVITATIONAL_CONSTANT = 6.674_3e-11 * SI.CUBIC_METER / SI.KILOGRAM / SI.SECOND / SI.SECOND;
    private static final double EARTH_RADIUS_EQUATOR = 6_378.137_0e3 * SI.METER;
    private static final double EARTH_RADIUS_POLE = 6_356.752_3e3 * SI.METER;
    private static final double EARTH_MASS = 5.972e24 * SI.KILOGRAM;
    private static final double EARTH_ROTATION_RATE = Math.PI / 43_200 * SI.RADIAN / SI.SECOND;
    private final BlockPos.MutableBlockPos helperPos = new BlockPos.MutableBlockPos();
    private double cachedAccX;
    private double cachedAccY;
    private double cachedAccZ;
    private double cachedCentY = Double.NaN;
    private double cachedCorY = Double.NaN;
    private float cachedCosLat = Float.NaN;
    private double cachedGravity = Double.NaN;
    private float cachedLatitude = Float.NaN;
    private double cachedRadius = Double.NaN;
    private float cachedSinLat = Float.NaN;
    private double coefDrag;
    private @Nullable Fluid fluid;
    private boolean hasCachedAcceleration;
    private @Nullable Level level;
    private boolean locked;
    private double sizeX;
    private double sizeY;
    private double sizeZ;
    private double velX;
    private double velY;
    private double velZ;
    private double x;
    private double y;
    private double z;

    private Physics() {
    }

    private static double coefOfDrag(Entity entity) {
        if (entity instanceof IAerodynamicEntity) {
            return 0.04;
        }
        return 1.05;
    }

    public static Physics getInstance(Level level,
                                      double x,
                                      double y,
                                      double z,
                                      double velX,
                                      double velY,
                                      double velZ,
                                      double sizeX,
                                      double sizeY,
                                      double sizeZ,
                                      double coefDrag,
                                      Fluid fluid) {
        Physics physics = CACHE.get();
        assert !physics.isLocked() : "The local instance of Physics is locked, you probably forgot to unlock it! Use it with try-with-resources to " +
                                     "unlock automatically.";
        physics.level = level;
        physics.x = x;
        physics.y = y;
        physics.z = z;
        physics.velX = velX;
        physics.velY = velY;
        physics.velZ = velZ;
        physics.sizeX = sizeX;
        physics.sizeY = sizeY;
        physics.sizeZ = sizeZ;
        physics.coefDrag = coefDrag;
        physics.fluid = fluid;
        physics.lock();
        return physics;
    }

    public static Physics getInstance(Entity entity, Fluid fluid) {
        Vec3 vel = entity.getDeltaMovement();
        return getInstance(entity.level, entity.getX(), entity.getY(), entity.getZ(), vel.x, vel.y, vel.z, entity.getBbWidth(), entity.getBbHeight(),
                           entity.getBbWidth(), coefOfDrag(entity), fluid);
    }

    /**
     * The actual equation is {@code sqrt((a^4*cos^2(lat)+b^4*sin^2(lat))/(a^2*cos^2(lat)+b^2*sin^2(lat)))} <br>
     * A very good approximation of this formula for values of {@code a} and {@code b} very close to one another is: {@code (a-b) * cos^2(lat) + b}
     * <br>
     * In both these equations, the values of {@code a} and {@code b} are, respectively, the equatorial and polar radii of the planet.
     */
    private double baseRadius() {
        float cosLat = this.cosLatitude();
        return (EARTH_RADIUS_EQUATOR - EARTH_RADIUS_POLE) * cosLat * cosLat + EARTH_RADIUS_POLE;
    }

    public void calcAccAbsolute(Entity entity, Vec3 direction, double magnitude) {
        double lengthSqr = direction.lengthSqr();
        this.hasCachedAcceleration = true;
        if (lengthSqr < 1.0E-8) {
            this.cachedAccX = 0;
            this.cachedAccY = 0;
            this.cachedAccZ = 0;
            return;
        }
        if (entity.getPose() == Pose.CROUCHING) {
            if (!(entity instanceof Player && ((Player) entity).getAbilities().flying)) {
                magnitude *= 0.3;
            }
        }
        if (entity instanceof Player) {
            if (entity.getPose() == Pose.SWIMMING && !entity.isInWater()) {
                magnitude *= 0.3;
            }
        }
        if (entity instanceof LivingEntity living && living.isUsingItem()) {
            Item activeItem = living.getUseItem().getItem();
            if (activeItem instanceof IEvolutionItem item) {
                magnitude *= item.useItemSlowDownRate();
            }
        }
        double norm = Mth.fastInvSqrt(lengthSqr);
        double accX = direction.x * norm * magnitude;
        double accY = direction.y * norm * magnitude;
        double accZ = direction.z * norm * magnitude;
        float sinFacing = MathHelper.sinDeg(entity.getYRot());
        float cosFacing = MathHelper.cosDeg(entity.getYRot());
        this.cachedAccX = accX * cosFacing - accZ * sinFacing;
        this.cachedAccY = accY;
        this.cachedAccZ = accZ * cosFacing + accX * sinFacing;
    }

    public double calcAccCentrifugalY() {
        if (Double.isNaN(this.cachedCentY)) {
            double omega = this.rotationRate();
            float cosLat = this.cosLatitude();
            this.cachedCentY = omega * omega * this.radius() * cosLat * cosLat;
        }
        return this.cachedCorY;
    }

    public double calcAccCentrifugalZ() {
        double omega = this.rotationRate();
        return omega * omega * this.radius() * this.sinLatitude() * this.cosLatitude();
    }

    public double calcAccCoriolisX() {
        return -2 * this.rotationRate() * (this.velY * this.cosLatitude() + this.velZ * this.sinLatitude());
    }

    public double calcAccCoriolisY() {
        if (Double.isNaN(this.cachedCorY)) {
            this.cachedCorY = 2 * this.rotationRate() * this.velX * this.cosLatitude();
        }
        return this.cachedCorY;
    }

    public double calcAccCoriolisZ() {
        return 2 * this.rotationRate() * this.velX * this.sinLatitude();
    }

    public double calcAccGravity() {
        if (Double.isNaN(this.cachedGravity)) {
            double radius = this.radius();
            this.cachedGravity = -GRAVITATIONAL_CONSTANT * EARTH_MASS / (radius * radius);
        }
        return this.cachedGravity;
    }

    public double calcAccLift(double windVelX, double windVelZ, double wingArea, double liftCoef, float angleOfAttack, boolean symmetrical) {
        assert this.fluid != null;
        assert -90 <= angleOfAttack && angleOfAttack <= 90 : "Angle of Attack out of range: " + angleOfAttack;
        if (this.fluid == Fluid.VACUUM) {
            return 0;
        }
        boolean positive = angleOfAttack >= 0;
        if (!positive) {
            angleOfAttack = Math.abs(angleOfAttack);
        }
        double f;
        if (angleOfAttack >= 30) {
            f = 0;
        }
        else {
            float aOASqr = angleOfAttack * angleOfAttack;
            f = 0.1 * angleOfAttack + 0.006_5 * aOASqr - 0.000_7 * aOASqr * angleOfAttack + 0.000_012_407 * aOASqr * aOASqr;
        }
        if (!symmetrical) {
            f += 0.5;
        }
        liftCoef *= f;
        if (liftCoef == 0) {
            return 0;
        }
        if (!positive) {
            liftCoef = -liftCoef;
        }
        double relVelX = this.velX - windVelX;
        double relVelZ = this.velZ - windVelZ;
        double relativeVelocitySqr = relVelX * relVelX + relVelZ * relVelZ;
        return 0.5 * this.fluid.density() * relativeVelocitySqr * wingArea * MathHelper.cosDeg(angleOfAttack) * liftCoef;
    }

    public double calcAccMagnitude(Entity entity, double slowdown) {
        if (entity instanceof LivingEntity living &&
            (entity instanceof FlyingAnimal flying && flying.isFlying() || entity instanceof Player player && player.getAbilities().flying)) {
            return living.flyingSpeed;
        }
        if (entity.isOnGround() || entity instanceof LivingEntity living && living.onClimbable()) {
            //noinspection CastConflictsWithInstanceof
            double acceleration = ((IEntityPatch) entity).getAcceleration() * slowdown;
            if (acceleration == 0) {
                return 0;
            }
            float f = this.calcStaticFrictionCoef(entity);
            if (entity.isSprinting()) {
                f *= 1.1;
            }
            return Math.min(acceleration, this.calcAccNormal() * f);
        }
        if (((IEntityPatch) entity).getNoJumpDelay() > 3) {
            double acceleration = ((IEntityPatch) entity).getAcceleration() * slowdown;
            if (acceleration == 0) {
                return 0;
            }
            return Math.min(acceleration, this.calcAccNormal() * 0.075);
        }
        return 0;
    }

    public double calcAccNormal() {
        return -(this.calcAccGravity() + this.calcAccCoriolisY() + this.calcAccCentrifugalY());
    }

    public double calcForceBuoyancy(Entity entity) {
        assert this.fluid != null;
        if (this.fluid == Fluid.VACUUM) {
            return 0;
        }
        double submergedHeight;
        if (this.fluid == Fluid.AIR) {
            submergedHeight = this.sizeY;
        }
        else {
            assert this.fluid.tag() != null;
            submergedHeight = entity.getFluidHeight(this.fluid.tag());
        }
        double volumeDisplaced = this.sizeX * this.sizeZ * submergedHeight * ((IEntityPatch) entity).getVolumeCorrectionFactor();
        return -this.calcAccGravity() * this.fluid.density() * volumeDisplaced;
    }

    public double calcForceDragX(double windVelX) {
        assert this.fluid != null;
        if (this.fluid == Fluid.VACUUM) {
            return 0;
        }
        double relVel = windVelX - this.velX;
        return Math.signum(relVel) * 0.5 * this.fluid.density() * this.sizeZ * this.sizeY * this.coefDrag * relVel * relVel;
    }

    public double calcForceDragY(double windVelY) {
        assert this.fluid != null;
        if (this.fluid == Fluid.VACUUM) {
            return 0;
        }
        double relVel = windVelY - this.velY;
        return Math.signum(relVel) * 0.5 * this.fluid.density() * this.sizeZ * this.sizeX * this.coefDrag * relVel * relVel;
    }

    public double calcForceDragZ(double windVelZ) {
        assert this.fluid != null;
        if (this.fluid == Fluid.VACUUM) {
            return 0;
        }
        double relVel = windVelZ - this.velZ;
        return Math.signum(relVel) * 0.5 * this.fluid.density() * this.sizeY * this.sizeX * this.coefDrag * relVel * relVel;
    }

    public float calcKineticFrictionCoef(Entity entity) {
        return this.calcStaticFrictionCoef(entity) * 0.8f;
    }

    public float calcStaticFrictionCoef(Entity entity) {
        if (!entity.isOnGround()) {
            return 0;
        }
        assert this.level != null;
        BlockState state = this.level.getBlockState(((IEntityPatch) entity).getFrictionPos());
        if (state.isAir() || state.getCollisionShape(this.level, this.helperPos).isEmpty()) {
            return 0;
        }
        float frictionCoef = ((IBlockPatch) state.getBlock()).getFrictionCoefficient(state) * ((IEntityPatch) entity).getFrictionModifier();
        if (entity.getFluidHeight(FluidTags.WATER) > 0) {
            frictionCoef -= 0.1f;
            if (frictionCoef < 0.01F) {
                return 0.01F;
            }
        }
        return frictionCoef;
    }

    @Override
    public void close() {
        this.locked = false;
        this.level = null;
        this.fluid = null;
        this.cachedLatitude = Float.NaN;
        this.cachedSinLat = Float.NaN;
        this.cachedCosLat = Float.NaN;
        this.cachedRadius = Double.NaN;
        this.cachedGravity = Double.NaN;
        this.cachedCentY = Double.NaN;
        this.cachedCorY = Double.NaN;
        this.hasCachedAcceleration = false;
    }

    public float cosLatitude() {
        if (Float.isNaN(this.cachedCosLat)) {
            this.cachedCosLat = MathHelper.cosDeg(this.latitude());
        }
        return this.cachedCosLat;
    }

    public double getAccAbsoluteX() {
        if (this.hasCachedAcceleration) {
            return this.cachedAccX;
        }
        throw new IllegalStateException(
                "Accelerations have not been calculated yet. Please call Physics#calcAccAbsolute(Entity, Vec3, double) first.");
    }

    public double getAccAbsoluteY() {
        if (this.hasCachedAcceleration) {
            return this.cachedAccY;
        }
        throw new IllegalStateException(
                "Accelerations have not been calculated yet. Please call Physics#calcAccAbsolute(Entity, Vec3, double) first.");
    }

    public double getAccAbsoluteZ() {
        if (this.hasCachedAcceleration) {
            return this.cachedAccZ;
        }
        throw new IllegalStateException(
                "Accelerations have not been calculated yet. Please call Physics#calcAccAbsolute(Entity, Vec3, double) first.");
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public float latitude() {
        assert this.level != null;
        if (Float.isNaN(this.cachedLatitude)) {
            this.cachedLatitude = PlanetsHelper.calculateLatitude(this.level, this.z);
        }
        return this.cachedLatitude;
    }

    @Override
    public void lock() {
        this.locked = true;
    }

    public double radius() {
        assert this.level != null;
        if (Double.isNaN(this.cachedRadius)) {
            double baseRadius = this.baseRadius();
            double localDeltaRadius = this.y - this.level.getSeaLevel();
            this.cachedRadius = baseRadius + localDeltaRadius;
        }
        return this.cachedRadius;
    }

    public double rotationRate() {
        return EARTH_ROTATION_RATE;
    }

    public float sinLatitude() {
        if (Float.isNaN(this.cachedSinLat)) {
            this.cachedSinLat = MathHelper.sinDeg(this.latitude());
        }
        return this.cachedSinLat;
    }
}
