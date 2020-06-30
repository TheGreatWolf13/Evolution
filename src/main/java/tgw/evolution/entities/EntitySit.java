package tgw.evolution.entities;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.ISittable;
import tgw.evolution.init.EvolutionEntities;

public class EntitySit extends Entity {

    private BlockPos source;

    public EntitySit(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.noClip = true;
    }

    @SuppressWarnings("unused")
    public EntitySit(FMLPlayMessages.SpawnEntity spawn, World worldIn) {
        this(EvolutionEntities.SIT.get(), worldIn);
    }

    public EntitySit(World worldIn, BlockPos pos, double yOffset) {
        this(EvolutionEntities.SIT.get(), worldIn);
        this.setPosition(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
        this.source = pos;
        this.prevPosX = pos.getX() + 0.5;
        this.prevPosY = pos.getY() + yOffset;
        this.prevPosZ = pos.getZ() + 0.5;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.source == null) {
            this.source = this.getPosition();
        }
        if (!this.world.isRemote) {
            if (this.getPassengers().isEmpty()) {
                this.world.setBlockState(this.source, this.world.getBlockState(this.source).with(BlockStateProperties.OCCUPIED, false));
                this.remove();
            }
            if (!(this.world.getBlockState(this.source).getBlock() instanceof ISittable)) {
                this.remove();
            }
        }
    }

    @Override
    public double getMountedYOffset() {
        return 0;
    }

    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return true;
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.source = NBTUtil.readBlockPos(compound.getCompound("Source"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.put("Source", NBTUtil.writeBlockPos(this.source));
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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
}
