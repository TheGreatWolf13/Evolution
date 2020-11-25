package tgw.evolution.entities;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.BlockLeaves;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.Gravity;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFallingTimber extends Entity implements IEntityAdditionalSpawnData {

    private static final int FALL_HURT_MAX = 1_000;
    private static final float FALL_HURT_AMOUNT = 2.0F;
    public int fallTime;
    private BlockState state = EvolutionBlocks.DESTROY_9.get().getDefaultState();
    private BlockState newState = EvolutionBlocks.DESTROY_9.get().getDefaultState();
    private boolean hurtEntities;
    private int delay;
    private boolean isLog;
    private double distance;
    private double originalX;
    private double originalZ;
    private boolean fixMotion;

    public EntityFallingTimber(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.FALLING_TIMBER.get(), worldIn);
    }

    public EntityFallingTimber(EntityType<?> type, World worldIn) {
        super(type, worldIn);
    }

    public EntityFallingTimber(World worldIn,
                               double x,
                               double y,
                               double z,
                               BlockState fallingBlockState,
                               BlockState newState,
                               boolean isLog,
                               double distance,
                               int delay) {
        this(EvolutionEntities.FALLING_TIMBER.get(), worldIn);
        this.state = fallingBlockState;
        this.newState = newState;
        this.preventEntitySpawning = true;
        this.setPosition(x, y, z);
        this.setMotion(Vec3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.distance = distance;
        this.originalX = x;
        this.originalZ = z;
        this.delay = delay;
        this.isLog = isLog;
        this.hurtEntities = isLog;
    }

    @Override
    protected void registerData() {
    }

    @Override
    public void tick() {
        if (this.state.isAir()) {
            this.remove();
        }
        else {
            if (this.delay > 0) {
                this.delay--;
                return;
            }
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            Block block = this.state.getBlock();
            if (this.fallTime++ == 0) {
                BlockPos blockpos = new BlockPos(this);
                if (this.world.getBlockState(blockpos).getBlock() == block) {
                    this.world.removeBlock(blockpos, false);
                }
                else if (!this.world.isRemote) {
                    this.remove();
                    return;
                }
            }
            this.move(MoverType.SELF, this.getMotion());
            if (!this.world.isRemote) {
                if (!this.fixMotion &&
                    (Math.abs(this.posX - this.originalX) >= this.distance || Math.abs(this.posZ - this.originalZ) >= this.distance)) {
                    this.setMotion(0, this.getMotion().getY(), 0);
                    this.fixMotion = true;
                }
                if (this.fixMotion) {
                    this.state = this.newState;
                    this.setMotion(this.getMotion().add(0, -Gravity.gravity(this.world.getDimension()), 0));
                }
                BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
                if (this.fallTime > 80) {
                    this.world.setBlockState(pos, this.state, 11);
                    this.remove();
                    return;
                }
                if (this.world.getBlockState(pos.down()).getBlock() instanceof BlockLeaves) {
                    if (this.state.getBlock() instanceof BlockLeaves) {
                        this.world.setBlockState(pos, this.state);
                        this.remove();
                    }
                }
                if (this.world.getBlockState(pos).getBlock() instanceof BlockLeaves) {
                    if (!(this.state.getBlock() instanceof BlockLeaves)) {
                        this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        this.playSound(SoundEvents.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
                    }
                }
                if (!this.onGround) {
                    if (this.fallTime > 100 && !this.world.isRemote && (pos.getY() < 1 || pos.getY() > 256)) {
                        if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                            this.entityDropItem(block);
                        }
                        this.remove();
                    }
                }
                else {
                    BlockState iblockstate = this.world.getBlockState(pos);
                    if (this.world.isAirBlock(new BlockPos(this.posX, this.posY - 0.01F, this.posZ))) //Forge: Don't indent below.
                    {
                        if (FallingBlock.canFallThrough(this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.01F, this.posZ)))) {
                            this.onGround = false;
                            return;
                        }
                    }
                    if (iblockstate.getBlock() != Blocks.MOVING_PISTON) {
                        this.remove();
                        if (BlockUtils.isReplaceable(iblockstate) &&
                            !FallingBlock.canFallThrough(this.world.getBlockState(pos.down())) &&
                            this.world.setBlockState(pos, this.state, 3)) {
                            if (BlockUtils.isReplaceable(iblockstate)) {
                                if (!(iblockstate.getBlock() instanceof BlockLeaves)) {
                                    this.entityDropItem(iblockstate.getBlock());
                                }
                            }
                        }
                        else if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                            if (!(block instanceof BlockLeaves)) {
                                this.entityDropItem(block);
                            }
                        }

                    }
                }
            }
        }
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.isAlive() && this.isLog ? this.getBoundingBox() : null;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (this.hurtEntities) {
            int i = MathHelper.ceil(distance - 1.0F);
            if (i > 0) {
                List<Entity> list = Lists.newArrayList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox()));
                DamageSource damagesource = EvolutionDamage.FALLING_TREE;
                for (Entity entity : list) {
                    entity.attackEntityFrom(damagesource, Math.min(MathHelper.floor(i * FALL_HURT_AMOUNT), FALL_HURT_MAX));
                }
            }
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isLog;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.state = NBTUtil.readBlockState(compound.getCompound("State"));
        this.newState = NBTUtil.readBlockState(compound.getCompound("NewState"));
        this.fallTime = compound.getInt("Time");
        this.hurtEntities = compound.getBoolean("HurtEntities");
        this.isLog = this.hurtEntities;
        this.distance = compound.getDouble("Distance");
        this.originalX = compound.getDouble("OriginX");
        this.originalZ = compound.getDouble("OriginZ");
        this.delay = compound.getByte("Delay");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.put("State", NBTUtil.writeBlockState(this.state));
        compound.put("NewState", NBTUtil.writeBlockState(this.newState));
        compound.putInt("Time", this.fallTime);
        compound.putBoolean("HurtEntities", this.hurtEntities);
        compound.putDouble("Distance", this.distance);
        compound.putDouble("OriginX", this.originalX);
        compound.putDouble("OriginZ", this.originalZ);
        compound.putByte("Delay", (byte) this.delay);
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    public void fillCrashReport(CrashReportCategory category) {
        super.fillCrashReport(category);
        category.func_71507_a("Immitating BlockState", this.state.toString());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public boolean ignoreItemEntityData() {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public BlockState getBlockState() {
        return this.state;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeCompoundTag(NBTUtil.writeBlockState(this.state));
        buffer.writeCompoundTag(NBTUtil.writeBlockState(this.newState));
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.state = NBTUtil.readBlockState(buffer.readCompoundTag());
        this.newState = NBTUtil.readBlockState(buffer.readCompoundTag());
    }
}