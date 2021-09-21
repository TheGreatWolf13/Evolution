package tgw.evolution.entities.misc;

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
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.*;
import tgw.evolution.entities.IEvolutionEntity;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFallingWeight extends Entity implements IEntityAdditionalSpawnData, IEvolutionEntity<EntityFallingWeight> {

    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
    public int fallTime;
    private int mass = 500;
    private BlockState state = EvolutionBlocks.DESTROY_9.get().defaultBlockState();

    public EntityFallingWeight(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawn, World world) {
        this(EvolutionEntities.FALLING_WEIGHT.get(), world);
    }

    public EntityFallingWeight(EntityType<EntityFallingWeight> type, World world) {
        super(type, world);
    }

    public EntityFallingWeight(World world, double x, double y, double z, BlockState state) {
        this(EvolutionEntities.FALLING_WEIGHT.get(), world);
        this.state = state;
        this.mass = this.state.getBlock() instanceof BlockMass ? ((BlockMass) this.state.getBlock()).getMass(this.state) : 500;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vector3d.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        compound.put("BlockState", NBTUtil.writeBlockState(this.state));
        compound.putInt("Time", this.fallTime);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) {
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
            Evolution.LOGGER.warn("No falling damage for block {}", this.state);
            return false;
        }
        float motionY = 20.0F * (float) this.getDeltaMovement().y;
        List<Entity> list = Lists.newArrayList(this.level.getEntities(this, this.getBoundingBox()));
        float kinecticEnergy = this.mass * motionY * motionY / 2;
        for (Entity entity : list) {
            float forceOfImpact = kinecticEnergy / entity.getBbHeight();
            float area = entity.getBbWidth() * entity.getBbWidth();
            float pressure = forceOfImpact / area;
            pressure += this.mass * Gravity.gravity(this.level.dimensionType()) / area;
            float damage = pressure / 344_738.0F * 100.0F;
            entity.hurt(source, damage);
        }
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory category) {
        super.fillCrashReportCategory(category);
        category.setDetail("Immitating BlockState", this.state.toString());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    //TODO
//    @Override
//    public boolean func_241845_aY() {
//        return this.isAlive();
//    }

    /**
     * Returns the {@code BlockState} this entity is immitating.
     */
    public BlockState getBlockState() {
        return this.state;
    }

    @Nullable
    @Override
    public HitboxEntity<EntityFallingWeight> getHitbox() {
        return null;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        this.state = NBTUtil.readBlockState(compound.getCompound("BlockState"));
        this.fallTime = compound.getInt("Time");
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.state = NBTUtil.readBlockState(buffer.readNbt());
    }

    @Override
    public void tick() {
        if (this.state.isAir()) {
            this.remove();
            return;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Block carryingBlock = this.state.getBlock();
        if (this.fallTime++ == 0) {
            BlockPos pos = this.blockPosition();
            if (this.level.getBlockState(pos).getBlock() == carryingBlock) {
                this.level.removeBlock(pos, false);
            }
            else if (!this.level.isClientSide) {
                this.remove();
                return;
            }
        }
        Vector3d motion = this.getDeltaMovement();
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
        this.mutablePos.set(this.getX(), this.getY(), this.getZ());
        if (this.level.getBlockState(this.mutablePos.below()).getBlock() instanceof BlockLeaves) {
            if (this.state.getBlock() instanceof BlockLeaves) {
                this.level.setBlockAndUpdate(this.mutablePos, this.state);
                this.remove();
            }
        }
        if (this.level.getBlockState(this.mutablePos).getBlock() instanceof BlockLeaves) {
            if (!(this.state.getBlock() instanceof BlockLeaves)) {
                this.level.setBlockAndUpdate(this.mutablePos, Blocks.AIR.defaultBlockState());
                this.playSound(SoundEvents.GRASS_BREAK, 1.0f, 1.0f);
            }
        }
        this.mutablePos.set(this.blockPosition());
        boolean isInWater = this.level.getFluidState(this.mutablePos).is(FluidTags.WATER);
        double d0 = this.getDeltaMovement().lengthSqr();
        if (d0 > 1) {
            BlockRayTraceResult raytraceresult = this.level.clip(new RayTraceContext(new Vector3d(this.xo, this.yo, this.zo),
                                                                                     new Vector3d(this.getX(), this.getY(), this.getZ()),
                                                                                     RayTraceContext.BlockMode.COLLIDER,
                                                                                     RayTraceContext.FluidMode.SOURCE_ONLY,
                                                                                     this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS && this.level.getFluidState(raytraceresult.getBlockPos()).is(FluidTags.WATER)) {
                this.mutablePos.set(raytraceresult.getBlockPos());
                isInWater = true;
            }
        }
        if (!this.onGround && !isInWater) {
            if (this.fallTime > 100 && !this.level.isClientSide && (this.mutablePos.getY() < 1 || this.mutablePos.getY() > 256) ||
                this.fallTime > 6_000) {
                if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    if (carryingBlock instanceof BlockCobblestone) {
                        this.spawnAtLocation(new ItemStack(((BlockCobblestone) carryingBlock).getVariant().getRock(), 4));
                    }
                    else {
                        this.spawnAtLocation(carryingBlock);
                    }
                }
                this.remove();
            }
        }
        else {
            BlockState state = this.level.getBlockState(this.mutablePos);
            BlockPos posDown = new BlockPos(this.getX(), this.getY() - 0.01, this.getZ());
            if (this.level.isEmptyBlock(posDown)) {
                if (!isInWater && FallingBlock.isFree(this.level.getBlockState(posDown))) {
                    this.onGround = false;
                    return;
                }
            }
            if ((!isInWater || this.onGround) && state.getBlock() != Blocks.MOVING_PISTON) {
                this.remove();
                if (BlockUtils.isReplaceable(state)) {
                    NonNullList<ItemStack> drops;
                    if (state.getBlock() instanceof IReplaceable) {
                        drops = ((IReplaceable) state.getBlock()).getDrops(this.level, this.mutablePos, state);
                    }
                    else {
                        drops = NonNullList.of(new ItemStack(state.getBlock()));
                    }
                    if (this.level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
                        for (ItemStack stack : drops) {
                            this.spawnAtLocation(stack);
                        }
                    }
                    this.level.setBlockAndUpdate(this.mutablePos, this.state);
                }
                else if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    if (carryingBlock instanceof BlockCobblestone) {
                        this.spawnAtLocation(new ItemStack(((BlockCobblestone) carryingBlock).getVariant().getRock(), 4));
                    }
                    else {
                        this.spawnAtLocation(carryingBlock);
                    }
                }
            }
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeNbt(NBTUtil.writeBlockState(this.state));
    }
}