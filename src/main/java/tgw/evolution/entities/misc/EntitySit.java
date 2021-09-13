package tgw.evolution.entities.misc;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.TransportationHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.ISittable;
import tgw.evolution.entities.IEvolutionEntity;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public class EntitySit extends Entity implements IEvolutionEntity<EntitySit> {

    private BlockPos source;

    @SuppressWarnings("unused")
    public EntitySit(FMLPlayMessages.SpawnEntity spawn, World world) {
        this(EvolutionEntities.SIT.get(), world);
    }

    public EntitySit(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.noPhysics = true;
        this.blocksBuilding = true;
    }

    public EntitySit(World world, BlockPos pos, double yOffset) {
        this(EvolutionEntities.SIT.get(), world);
        this.setPos(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
        this.source = pos;
        this.xo = pos.getX() + 0.5;
        this.yo = pos.getY() + yOffset;
        this.zo = pos.getZ() + 0.5;
    }

    public static boolean create(World world, BlockPos pos, PlayerEntity player) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof ISittable) {
            if (!world.isClientSide) {
                EntitySit seat = new EntitySit(world, pos, ((ISittable) block).getYOffset());
                world.addFreshEntity(seat);
                player.startRiding(seat, false);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        compound.put("Source", NBTUtil.writeBlockPos(this.source));
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
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public Vector3d getDismountLocationForPassenger(LivingEntity entity) {
        Direction facing = entity.getDirection();
        if (facing.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(entity);
        }
        int[][] offsets = TransportationHelper.offsetsForDirection(facing.getCounterClockWise());
        BlockPos pos = this.blockPosition();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Pose pose : entity.getDismountPoses()) {
            AxisAlignedBB bb = entity.getLocalBoundsForPose(pose);
            for (int[] offset : offsets) {
                mutable.set(pos.getX() + offset[0], pos.getY(), pos.getZ() + offset[1]);
                double d0 = this.level.getBlockFloorHeight(mutable);
                if (TransportationHelper.isBlockFloorValid(d0)) {
                    //noinspection ObjectAllocationInLoop
                    Vector3d vector3d = Vector3d.upFromBottomCenterOf(mutable, d0);
                    if (TransportationHelper.canDismountTo(this.level, entity, bb.move(vector3d))) {
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
    protected void readAdditionalSaveData(CompoundNBT compound) {
        this.source = NBTUtil.readBlockPos(compound.getCompound("Source"));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.source == null) {
            this.source = this.blockPosition();
        }
        if (!this.level.isClientSide) {
            if (!(this.level.getBlockState(this.source).getBlock() instanceof ISittable)) {
                this.remove();
            }
            if (this.getPassengers().isEmpty()) {
                this.level.setBlockAndUpdate(this.source, this.level.getBlockState(this.source).setValue(EvolutionBStates.OCCUPIED, false));
                this.remove();
            }
        }
    }
}
