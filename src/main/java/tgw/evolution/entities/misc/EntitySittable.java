package tgw.evolution.entities.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.ISittableBlock;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.world.util.LevelUtils;

public class EntitySittable extends Entity implements ISittableEntity {

    private @Range(from = 0, to = 100) byte comfort;
    private @Nullable BlockPos source;
    private float zOffset;

    public EntitySittable(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.blocksBuilding = true;
    }

    public EntitySittable(Level world, int x, int y, int z, double yOffset, float zOffset, @Range(from = 0, to = 100) int comfort) {
        this(EvolutionEntities.SIT, world);
        this.setPos(x + 0.5, y + yOffset, z + 0.5);
        this.source = new BlockPos(x, y, z);
        this.xo = x + 0.5;
        this.yo = y + yOffset;
        this.zo = z + 0.5;
        this.comfort = (byte) comfort;
        this.zOffset = zOffset;
    }

    public static boolean create(Level level, int x, int y, int z, Player player) {
        Block block = level.getBlockState_(x, y, z).getBlock();
        if (block instanceof ISittableBlock sittable) {
            if (!level.isClientSide) {
                AABB bb = player.getBoundingBox();
                if (LevelUtils.collidesWithSuffocatingBlock(level, player,
                                                            x + 1e-7, bb.minY + 1e-7, z + 1e-7,
                                                            x + 1 - 1e-7, bb.maxY - 1e-7, z + 1 - 1e-7)) {
                    return false;
                }
                EntitySittable seat = new EntitySittable(level, x, y, z, sittable.getYOffset(), sittable.getZOffset(), sittable.getComfort());
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
        compound.putFloat("ZOffset", this.zOffset);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Float.floatToIntBits(this.zOffset));
    }

    @Override
    public double getBaseMass() {
        return 0;
    }

    @Override
    public @Range(from = 0, to = 100) int getComfort() {
        return this.comfort;
    }

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
    public @Nullable HitboxEntity<? extends EntitySittable> getHitboxes() {
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
    public double getVolume() {
        return 0;
    }

    @Override
    public float getZOffset() {
        return this.zOffset;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.source = NbtUtils.readBlockPos(compound.getCompound("Source"));
        this.comfort = compound.getByte("Comfort");
        this.zOffset = compound.getFloat("ZOffset");
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.zOffset = Float.intBitsToFloat(packet.getData());
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
