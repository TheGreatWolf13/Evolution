package tgw.evolution.entities;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.BlockCobblestone;
import tgw.evolution.blocks.BlockLeaves;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.Gravity;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFallingWeight extends Entity implements IEntityAdditionalSpawnData {

    private static final int FALL_HURT_MAX = 1000;
    private static final float FALL_HURT_AMOUNT = 2.0F;
    private final double verticalDrag;
    private final double horizontalDrag;
    private final double gravity;
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    public int fallTime;
    private BlockState state = EvolutionBlocks.DESTROY_9.get().getDefaultState();

    public EntityFallingWeight(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawn, World worldIn) {
        this(EvolutionEntities.FALLING_WEIGHT.get(), worldIn);
    }

    public EntityFallingWeight(EntityType<EntityFallingWeight> type, World worldIn) {
        super(type, worldIn);
        this.verticalDrag = 1/*Gravity.verticalDrag(this.world.getDimension(), this.getWidth())*/;
        this.horizontalDrag = 1/*Gravity.horizontalDrag(this.world.getDimension(), this.getWidth(), this.getHeight())*/;
        this.gravity = -Gravity.gravity(this.world.getDimension());
    }

    public EntityFallingWeight(World worldIn, double x, double y, double z, BlockState state) {
        this(EvolutionEntities.FALLING_WEIGHT.get(), worldIn);
        this.state = state;
        this.preventEntitySpawning = true;
        this.setPosition(x, y, z);
        this.setMotion(Vec3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    protected void registerData() {
    }

    @Override
    public void tick() {
        if (this.state.isAir()) {
            this.remove();
            return;
        }
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        Block carryingBlock = this.state.getBlock();
        if (this.fallTime++ == 0) {
            BlockPos blockpos = new BlockPos(this);
            if (this.world.getBlockState(blockpos).getBlock() == carryingBlock) {
                this.world.removeBlock(blockpos, false);
            }
            else if (!this.world.isRemote) {
                this.remove();
                return;
            }
        }
        if (!this.hasNoGravity()) {
            Vec3d motion = this.getMotion();
            this.setMotion(motion.x * this.horizontalDrag, (motion.y + this.gravity) * this.verticalDrag, motion.z * this.horizontalDrag);
        }
        this.move(MoverType.SELF, this.getMotion());
        this.mutablePos.setPos(this.posX, this.posY, this.posZ);
        if (this.world.getBlockState(this.mutablePos.down()).getBlock() instanceof BlockLeaves) {
            if (this.state.getBlock() instanceof BlockLeaves) {
                this.world.setBlockState(this.mutablePos, this.state);
                this.remove();
            }
        }
        if (this.world.getBlockState(this.mutablePos).getBlock() instanceof BlockLeaves) {
            if (!(this.state.getBlock() instanceof BlockLeaves)) {
                this.world.setBlockState(this.mutablePos, Blocks.AIR.getDefaultState());
                this.playSound(SoundEvents.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
            }
        }
        this.mutablePos.setPos(this);
        boolean isInWater = this.world.getFluidState(this.mutablePos).isTagged(FluidTags.WATER);
        double d0 = this.getMotion().lengthSquared();
        if (d0 > 1.0D) {
            BlockRayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ),
                                                                                               new Vec3d(this.posX, this.posY, this.posZ),
                                                                                               RayTraceContext.BlockMode.COLLIDER,
                                                                                               RayTraceContext.FluidMode.SOURCE_ONLY,
                                                                                               this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS && this.world.getFluidState(raytraceresult.getPos()).isTagged(FluidTags.WATER)) {
                this.mutablePos.setPos(raytraceresult.getPos());
                isInWater = true;
            }
        }
        if (!this.onGround && !isInWater) {
            if (this.fallTime > 100 && !this.world.isRemote && (this.mutablePos.getY() < 1 || this.mutablePos.getY() > 256) || this.fallTime > 6000) {
                if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                    if (carryingBlock instanceof BlockCobblestone) {
                        this.entityDropItem(new ItemStack(((BlockCobblestone) carryingBlock).getVariant().getRock(), 4));
                    }
                    else {
                        this.entityDropItem(carryingBlock);
                    }
                }
                this.remove();
            }
        }
        else {
            BlockState state = this.world.getBlockState(this.mutablePos);
            BlockPos posDown = new BlockPos(this.posX, this.posY - 0.01f, this.posZ);
            if (this.world.isAirBlock(posDown)) {
                if (!isInWater && FallingBlock.canFallThrough(this.world.getBlockState(posDown))) {
                    this.onGround = false;
                    return;
                }
            }
            this.setMotion(this.getMotion().mul(0.7, 0.7, 0.7));
            if ((!isInWater || this.onGround) && state.getBlock() != Blocks.MOVING_PISTON) {
                this.remove();
                if (BlockUtils.isReplaceable(state)) {
                    ItemStack stack;
                    if (state.getBlock() instanceof IReplaceable) {
                        stack = ((IReplaceable) state.getBlock()).getDrops(state);
                    }
                    else {
                        stack = new ItemStack(state.getBlock());
                    }
                    if (this.world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
                        this.entityDropItem(stack);
                    }
                    this.world.setBlockState(this.mutablePos, this.state, 3);
                }
                else if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                    if (carryingBlock instanceof BlockCobblestone) {
                        this.entityDropItem(new ItemStack(((BlockCobblestone) carryingBlock).getVariant().getRock(), 4));
                    }
                    else {
                        this.entityDropItem(carryingBlock);
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
        return this.isAlive() ? this.getBoundingBox() : null;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        int i = MathHelper.ceil(distance - 1.0F);
        if (i > 0) {
            List<Entity> list = Lists.newArrayList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox()));
            boolean isRock = this.state.getMaterial() == Material.ROCK;
            boolean isWood = this.state.getMaterial() == Material.WOOD;
            boolean isSoil = this.state.getMaterial() == Material.EARTH ||
                             this.state.getMaterial() == Material.CLAY ||
                             this.state.getMaterial() == Material.SAND;
            DamageSource damagesource = DamageSource.FALLING_BLOCK;
            if (isRock) {
                damagesource = EvolutionDamage.FALLING_ROCK;
            }
            else if (isSoil) {
                damagesource = EvolutionDamage.FALLING_SOIL;
            }
            else if (isWood) {
                damagesource = EvolutionDamage.FALLING_WOOD;
            }
            for (Entity entity : list) {
                entity.attackEntityFrom(damagesource, Math.min(MathHelper.floor(i * FALL_HURT_AMOUNT), FALL_HURT_MAX));
            }
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.state = NBTUtil.readBlockState(compound.getCompound("BlockState"));
        this.fallTime = compound.getInt("Time");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.put("BlockState", NBTUtil.writeBlockState(this.state));
        compound.putInt("Time", this.fallTime);
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
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /**
     * Returns the {@code BlockState} this entity is immitating.
     */
    public BlockState getBlockState() {
        return this.state;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeCompoundTag(NBTUtil.writeBlockState(this.state));
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.state = NBTUtil.readBlockState(buffer.readCompoundTag());
    }
}