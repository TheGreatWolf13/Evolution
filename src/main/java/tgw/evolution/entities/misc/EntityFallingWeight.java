package tgw.evolution.entities.misc;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.entities.IEntitySpawnData;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.network.PacketSCCustomEntity;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.ClipContextMutable;
import tgw.evolution.util.physics.Fluid;
import tgw.evolution.util.physics.Physics;
import tgw.evolution.util.physics.SI;

import java.util.List;
import java.util.function.Consumer;

public class EntityFallingWeight extends Entity implements IEntitySpawnData {

    private static final ItemDropper DROPPER = new ItemDropper();
    public int fallTime;
    protected double mass = 500;
    protected BlockState state = EvolutionBlocks.DESTROY_9.defaultBlockState();
    private final ClipContextMutable clipContext = new ClipContextMutable();

    public EntityFallingWeight(EntityType<? extends EntityFallingWeight> type, Level level) {
        super(type, level);
    }

    public EntityFallingWeight(Level level, double x, double y, double z, BlockState state, double mass) {
        this(EvolutionEntities.FALLING_WEIGHT, level);
        this.state = state;
        this.mass = mass;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(0, 0, 0);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource initialSource) {
        if (this.level.isClientSide) {
            return false;
        }
        DamageSource source = this.getDamageSource();
        if (source == null) {
            Evolution.warn("No falling damage for block {}", this.state);
            return false;
        }
        double motionY = 20 * this.getDeltaMovement().y;
        double kinecticEnergy = this.mass * motionY * motionY / 2;
        List<Entity> entities = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        double weight;
        try (Physics physics = Physics.getInstance(this, Fluid.AIR)) {
            weight = this.mass * physics.calcAccGravity();
        }
        for (int i = 0, l = entities.size(); i < l; i++) {
            Entity entity = entities.get(i);
            double forceOfImpact = kinecticEnergy / entity.getBbHeight();
            double area = entity.getBbWidth() * entity.getBbWidth();
            double pressure = forceOfImpact / area;
            pressure += weight / area;
            double damage = pressure / 344_738 * 100;
            entity.hurt(source, (float) damage);
        }
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory category) {
        super.fillCrashReportCategory(category);
        category.setDetail("Imitating BlockState", this.state.toString());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new PacketSCCustomEntity<>(this);
    }

    @Override
    public double getBaseMass() {
        return this.mass;
    }

    /**
     * Returns the {@code BlockState} this entity is imitating.
     */
    public BlockState getBlockState() {
        return this.state;
    }

    @Override
    public float getFrictionModifier() {
        return 2.0f;
    }

    @Override
    public @Nullable HitboxEntity<? extends EntityFallingWeight> getHitboxes() {
        return null;
    }

    @Override
    public EntityData getSpawnData() {
        return new FallingWeightData(this);
    }

    @Override
    public double getVolume() {
        return 1 * SI.CUBIC_METER;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void tick() {
        if (this.state.isAir()) {
            this.discard();
            return;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        try (Physics physics = Physics.getInstance(this, this.isInWater() ? Fluid.WATER : this.isInLava() ? Fluid.LAVA : Fluid.AIR)) {
            double accY = 0;
            if (!this.isNoGravity()) {
                accY += physics.calcAccGravity();
            }
            if (!this.isOnGround()) {
                accY += physics.calcForceBuoyancy(this) / this.mass;
            }
            //Pseudo-forces
            double accCoriolisY = physics.calcAccCoriolisY();
            double accCentrifugalY = physics.calcAccCentrifugalY();
            //Dissipative Forces
            double dissipativeX = 0;
            double dissipativeZ = 0;
            if (this.isOnGround() && (motionX != 0 || motionZ != 0)) {
                double norm = Mth.fastInvSqrt(motionX * motionX + motionZ * motionZ);
                double frictionAcc = physics.calcAccNormal() * physics.calcKineticFrictionCoef(this);
                double frictionX = motionX * norm * frictionAcc;
                double frictionZ = motionZ * norm * frictionAcc;
                dissipativeX = frictionX;
                if (Math.abs(dissipativeX) > Math.abs(motionX)) {
                    dissipativeX = motionX;
                }
                dissipativeZ = frictionZ;
                if (Math.abs(dissipativeZ) > Math.abs(motionZ)) {
                    dissipativeZ = motionZ;
                }
            }
            //Update Motion
            motionX -= dissipativeX;
            motionY += accY + accCoriolisY + accCentrifugalY;
            motionZ -= dissipativeZ;
        }
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
        BlockPos pos = this.blockPosition();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isInWater = this.level.getFluidState_(x, y, z).is(FluidTags.WATER);
        double motionLenSqr = this.getDeltaMovement().lengthSqr();
        if (motionLenSqr > 1) {
            BlockHitResult hitResult = this.level.clip(this.clipContext.set(this.xo, this.yo, this.zo, this.getX(), this.getY(), this.getZ(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
            if (hitResult.getType() != HitResult.Type.MISS) {
                int hitX = hitResult.posX();
                int hitY = hitResult.posY();
                int hitZ = hitResult.posZ();
                if (this.level.getFluidState_(hitX, hitY, hitZ).is(FluidTags.WATER)) {
                    x = hitX;
                    y = hitY;
                    z = hitZ;
                    isInWater = true;
                }
            }
        }
        if (BlockUtils.isReplaceable(this.level.getBlockState_(Mth.floor(this.getX()), Mth.floor(this.getY() - 0.01), Mth.floor(this.getZ())))) {
            if (!isInWater) {
                this.onGround = false;
                return;
            }
        }
        BlockState state = this.level.getBlockState_(x, y, z);
        if ((!isInWater || this.onGround) && state.getBlock() != Blocks.MOVING_PISTON) {
            this.discard();
            if (!this.level.isClientSide) {
                if (BlockUtils.isReplaceable(state)) {
                    this.level.destroyBlock_(x, y, z, true);
                    this.level.setBlockAndUpdate_(x, y, z, this.state);
                }
                else if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    try (ItemDropper dropper = DROPPER.setup(this)) {
                        this.state.dropLoot((ServerLevel) this.level, x, y, z, ItemStack.EMPTY, null, null, this.level.random, dropper);
                    }
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(this.state));
        tag.putInt("Time", this.fallTime);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.state = NbtUtils.readBlockState(tag.getCompound("BlockState"));
        this.fallTime = tag.getInt("Time");
    }

    private @Nullable DamageSource getDamageSource() {
        Material material = this.state.getMaterial();
        if (material == Material.STONE || this.state.getBlock() instanceof BlockKnapping) {
            return EvolutionDamage.FALLING_ROCK;
        }
        if (material == Material.DIRT || material == Material.CLAY || material == Material.SAND) {
            return EvolutionDamage.FALLING_SOIL;
        }
        if (material == Material.WOOD) {
            return EvolutionDamage.FALLING_WOOD;
        }
        if (material == Material.METAL) {
            return EvolutionDamage.FALLING_METAL;
        }
        return null;
    }

    public static class FallingWeightData<T extends EntityFallingWeight> extends EntityData<T> {

        private final float mass;
        private final BlockState state;

        public FallingWeightData(T entity) {
            this.state = entity.state;
            this.mass = (float) entity.mass;
        }

        public FallingWeightData(FriendlyByteBuf buf) {
            this.state = Block.stateById(buf.readVarInt());
            this.mass = buf.readFloat();
        }

        @Override
        public void read(T entity) {
            entity.state = this.state;
            entity.mass = this.mass;
        }

        @Override
        public void writeToBuffer(FriendlyByteBuf buf) {
            buf.writeVarInt(Block.getId(this.state));
            buf.writeFloat(this.mass);
        }
    }

    public static final class ItemDropper implements Consumer<ItemStack>, AutoCloseable {

        private @Nullable EntityFallingWeight entity;

        @Override
        public void accept(ItemStack stack) {
            assert this.entity != null;
            this.entity.spawnAtLocation(stack);
        }

        @Override
        public void close() {
            this.entity = null;
        }

        public ItemDropper setup(EntityFallingWeight entity) {
            assert this.entity == null;
            this.entity = entity;
            return this;
        }
    }
}