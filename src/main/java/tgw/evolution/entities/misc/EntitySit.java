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
import tgw.evolution.blocks.ISittable;
import tgw.evolution.entities.IEvolutionEntity;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public class EntitySit extends Entity implements IEvolutionEntity<EntitySit> {

    private BlockPos source;

    public EntitySit(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.blocksBuilding = true;
    }

    public EntitySit(Level world, BlockPos pos, double yOffset) {
        this(EvolutionEntities.SIT.get(), world);
        this.setPos(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
        this.source = pos;
        this.xo = pos.getX() + 0.5;
        this.yo = pos.getY() + yOffset;
        this.zo = pos.getZ() + 0.5;
    }

    public EntitySit(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(EvolutionEntities.SIT.get(), level);
    }

    public static boolean create(Level level, BlockPos pos, Player player) {
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof ISittable sittable) {
            if (!level.isClientSide) {
                EntitySit seat = new EntitySit(level, pos, sittable.getYOffset());
                level.addFreshEntity(seat);
                player.startRiding(seat, false);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Source", NbtUtils.writeBlockPos(this.source));
    }

    //TODO
//    @Override
//    protected boolean canBeRidden(Entity entity) {
//        return true;
//    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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

    @Nullable
    @Override
    public HitboxEntity<EntitySit> getHitbox() {
        return null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.source = NbtUtils.readBlockPos(compound.getCompound("Source"));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.source == null) {
            this.source = this.blockPosition();
        }
        if (!this.level.isClientSide) {
            if (!(this.level.getBlockState(this.source).getBlock() instanceof ISittable)) {
                this.discard();
            }
            if (this.getPassengers().isEmpty()) {
                this.level.setBlockAndUpdate(this.source, this.level.getBlockState(this.source).setValue(EvolutionBStates.OCCUPIED, false));
                this.discard();
            }
        }
    }
}
