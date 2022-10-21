package tgw.evolution.entities.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.ISittableBlock;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.math.AABBMutable;

public class EntitySittable extends Entity implements IEntityPatch<EntitySittable>, ISittableEntity {

    @Range(from = 0, to = 100)
    private byte comfort;
    @Nullable
    private BlockPos source;

    public EntitySittable(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.blocksBuilding = true;
    }

    public EntitySittable(Level world, BlockPos pos, double yOffset, @Range(from = 0, to = 100) int comfort) {
        this(EvolutionEntities.SIT.get(), world);
        this.setPos(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
        this.source = pos;
        this.xo = pos.getX() + 0.5;
        this.yo = pos.getY() + yOffset;
        this.zo = pos.getZ() + 0.5;
        this.comfort = (byte) comfort;
    }

    public EntitySittable(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(EvolutionEntities.SIT.get(), level);
    }

    public static boolean create(Level level, BlockPos pos, Player player) {
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof ISittableBlock sittable) {
            if (!level.isClientSide) {
                AABB bb = player.getBoundingBox();
                AABB bbToCheck = new AABBMutable(pos.getX(), bb.minY, pos.getZ(), pos.getX() + 1.0, bb.maxY, pos.getZ() + 1.0).deflateMutable(1.0E-7);
                if (level.collidesWithSuffocatingBlock(player, bbToCheck)) {
                    return false;
                }
                EntitySittable seat = new EntitySittable(level, pos, sittable.getYOffset(), sittable.getComfort());
                level.addFreshEntity(seat);
                player.startRiding(seat, false);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.source != null) {
            compound.put("Source", NbtUtils.writeBlockPos(this.source));
        }
        compound.putByte("Comfort", this.comfort);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public double getBaseMass() {
        return 0;
    }

    @Override
    public @Range(from = 0, to = 100) int getComfort() {
        return this.comfort;
    }

    //TODO
//    @Override
//    protected boolean canBeRidden(Entity entity) {
//        return true;
//    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity entity) {
        Direction facing = entity.getDirection();
        if (facing.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(entity);
        }
        int[][] offsets = DismountHelper.offsetsForDirection(facing.getCounterClockWise());
        BlockPos pos = this.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Pose pose : entity.getDismountPoses()) {
            AABB bb = entity.getLocalBoundsForPose(pose);
            for (int[] offset : offsets) {
                mutable.set(pos.getX() + offset[0], pos.getY(), pos.getZ() + offset[1]);
                double d0 = this.level.getBlockFloorHeight(mutable);
                if (DismountHelper.isBlockFloorValid(d0)) {
                    //noinspection ObjectAllocationInLoop
                    Vec3 vector3d = Vec3.upFromBottomCenterOf(mutable, d0);
                    if (DismountHelper.canDismountTo(this.level, entity, bb.move(vector3d))) {
                        entity.setPose(pose);
                        return vector3d;
                    }
                }
            }
        }
        return super.getDismountLocationForPassenger(entity);
    }

    @Override
    public float getFrictionModifier() {
        return 0;
    }

    @Override
    public @Nullable HitboxEntity<EntitySittable> getHitboxes() {
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
    public double getPassengersRidingOffset() {
        return 0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.source = NbtUtils.readBlockPos(compound.getCompound("Source"));
        this.comfort = compound.getByte("Comfort");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.source == null) {
            this.source = this.blockPosition();
        }
        if (!this.level.isClientSide) {
            if (!(this.level.getBlockState(this.source).getBlock() instanceof ISittableBlock)) {
                this.discard();
            }
            if (this.getPassengers().isEmpty()) {
                this.level.setBlockAndUpdate(this.source, this.level.getBlockState(this.source).setValue(EvolutionBStates.OCCUPIED, false));
                this.discard();
            }
        }
    }
}
