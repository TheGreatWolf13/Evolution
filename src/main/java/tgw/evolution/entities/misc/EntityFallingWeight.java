package tgw.evolution.entities.misc;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
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
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IPhysics;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.earth.Gravity;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.ClipContextMutable;
import tgw.evolution.util.math.Vec3d;

import java.util.List;

public class EntityFallingWeight extends Entity implements IEntityAdditionalSpawnData, IEntityPatch<EntityFallingWeight> {

    private final ClipContextMutable clipContext = new ClipContextMutable(Vec3.ZERO, Vec3.ZERO, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                                                                          null);
    private final Vec3d from = new Vec3d();
    private final MutableBlockPos mutablePos = new MutableBlockPos();
    private final Vec3d to = new Vec3d();
    public int fallTime;
    private double mass = 500;
    private BlockState state = EvolutionBlocks.DESTROY_9.get().defaultBlockState();

    public EntityFallingWeight(EntityType<EntityFallingWeight> type, Level level) {
        super(type, level);
    }

    public EntityFallingWeight(Level level, double x, double y, double z, BlockState state, BlockPos pos) {
        this(EvolutionEntities.FALLING_WEIGHT.get(), level);
        this.state = state;
        this.mass = this.state.getBlock() instanceof IPhysics physics ? physics.getMass(this.level, pos, this.state) : 500;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @SuppressWarnings("unused")
    public EntityFallingWeight(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(EvolutionEntities.FALLING_WEIGHT.get(), level);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(this.state));
        tag.putInt("Time", this.fallTime);
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
        DamageSource source = null;
        Material material = this.state.getMaterial();
        if (material == Material.STONE || this.state.getBlock() instanceof BlockKnapping) {
            source = EvolutionDamage.FALLING_ROCK;
        }
        else if (material == Material.DIRT || material == Material.CLAY || material == Material.SAND) {
            source = EvolutionDamage.FALLING_SOIL;
        }
        else if (material == Material.WOOD) {
            source = EvolutionDamage.FALLING_WOOD;
        }
        else if (material == Material.METAL) {
            source = EvolutionDamage.FALLING_METAL;
        }
        if (source == null) {
            Evolution.warn("No falling damage for block {}", this.state);
            return false;
        }
        double motionY = 20 * this.getDeltaMovement().y;
        double kinecticEnergy = this.mass * motionY * motionY / 2;
        List<Entity> entities = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        for (int i = 0, l = entities.size(); i < l; i++) {
            Entity entity = entities.get(i);
            double forceOfImpact = kinecticEnergy / entity.getBbHeight();
            double area = entity.getBbWidth() * entity.getBbWidth();
            double pressure = forceOfImpact / area;
            pressure += this.mass * Gravity.gravity(this.level.dimensionType()) / area;
            double damage = pressure / 344_738 * 100;
            entity.hurt(source, (float) damage);
        }
        return false;
    }

    @Override
    protected void defineSynchedData() {
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
        return NetworkHooks.getEntitySpawningPacket(this);
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
    public @Nullable HitboxEntity<EntityFallingWeight> getHitboxes() {
        return null;
    }

    @Override
    public double getLegSlowdown() {
        return 0;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.state = NbtUtils.readBlockState(tag.getCompound("BlockState"));
        this.fallTime = tag.getInt("Time");
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        if (tag != null) {
            this.state = NbtUtils.readBlockState(tag);
        }
        this.mass = buffer.readDouble();
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
        double gravity = 0;
        if (!this.isNoGravity()) {
            gravity = Gravity.gravity(this.level.dimensionType());
        }
        double horizontalDrag = this.isInWater() ? Gravity.horizontalWaterDrag(this) / this.mass : Gravity.horizontalDrag(this) / this.mass;
        double verticalDrag = this.isInWater() ? Gravity.verticalWaterDrag(this) / this.mass : Gravity.verticalDrag(this) / this.mass;
        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
        if (Math.abs(dragX) > Math.abs(motionX)) {
            dragX = motionX;
        }
        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
        if (Math.abs(dragY) > Math.abs(motionY)) {
            dragY = motionY;
        }
        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
        if (Math.abs(dragZ) > Math.abs(motionZ)) {
            dragZ = motionZ;
        }
        motionX -= dragX;
        motionY += -gravity - dragY;
        motionZ -= dragZ;
        if (Math.abs(motionX) < 1e-6) {
            motionX = 0;
        }
        if (Math.abs(motionY) < 1e-6) {
            motionY = 0;
        }
        if (Math.abs(motionZ) < 1e-6) {
            motionZ = 0;
        }
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.mutablePos.set(this.blockPosition());
        boolean isInWater = this.level.getFluidState(this.mutablePos).is(FluidTags.WATER);
        double motionLenSqr = this.getDeltaMovement().lengthSqr();
        if (motionLenSqr > 1) {
            BlockHitResult hitResult = this.level.clip(
                    this.clipContext.set(this.from.set(this.xo, this.yo, this.zo), this.to.set(this.getX(), this.getY(), this.getZ()),
                                         ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
            if (hitResult.getType() != HitResult.Type.MISS && this.level.getFluidState(hitResult.getBlockPos()).is(FluidTags.WATER)) {
                this.mutablePos.set(hitResult.getBlockPos());
                isInWater = true;
            }
        }
        if (BlockUtils.isReplaceable(BlockUtils.getBlockState(this.level, this.getX(), this.getY() - 0.01, this.getZ()))) {
            if (!isInWater) {
                this.onGround = false;
                return;
            }
        }
        BlockState state = this.level.getBlockState(this.mutablePos);
        if ((!isInWater || this.onGround) && state.getBlock() != Blocks.MOVING_PISTON) {
            this.discard();
            if (BlockUtils.isReplaceable(state)) {
                BlockUtils.destroyBlock(this.level, this.mutablePos);
                this.level.setBlockAndUpdate(this.mutablePos, this.state);
            }
            else if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS) && !this.level.isClientSide) {
                List<ItemStack> drops = Block.getDrops(this.state, (ServerLevel) this.level, this.mutablePos.immutable(), null);
                for (int i = 0, len = drops.size(); i < len; i++) {
                    this.spawnAtLocation(drops.get(i));
                }
            }
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeBlockState(this.state));
        buffer.writeDouble(this.mass);
    }
}