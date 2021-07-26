package tgw.evolution.entities.misc;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
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
        this.noClip = true;
        this.preventEntitySpawning = true;
    }

    public EntitySit(World world, BlockPos pos, double yOffset) {
        this(EvolutionEntities.SIT.get(), world);
        this.setPosition(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
        this.source = pos;
        this.prevPosX = pos.getX() + 0.5;
        this.prevPosY = pos.getY() + yOffset;
        this.prevPosZ = pos.getZ() + 0.5;
    }

    public static boolean create(World world, BlockPos pos, PlayerEntity player) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof ISittable) {
            if (!world.isRemote) {
                EntitySit seat = new EntitySit(world, pos, ((ISittable) block).getYOffset());
                world.addEntity(seat);
                player.startRiding(seat, false);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Nullable
    @Override
    public HitboxEntity<EntitySit> getHitbox() {
        return null;
    }

    @Override
    public double getMountedYOffset() {
        return 0;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.source = NBTUtil.readBlockPos(compound.getCompound("Source"));
    }

    @Override
    protected void registerData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.source == null) {
            this.source = this.getPosition();
        }
        if (!this.world.isRemote) {
            if (!(this.world.getBlockState(this.source).getBlock() instanceof ISittable)) {
                this.remove();
            }
            if (this.getPassengers().isEmpty()) {
                this.world.setBlockState(this.source, this.world.getBlockState(this.source).with(EvolutionBStates.OCCUPIED, false));
                this.remove();
            }
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.put("Source", NBTUtil.writeBlockPos(this.source));
    }
}
