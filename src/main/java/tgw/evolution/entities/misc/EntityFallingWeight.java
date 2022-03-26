package tgw.evolution.entities.misc;

import com.google.common.collect.Lists;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.*;
import tgw.evolution.entities.IEvolutionEntity;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.earth.Gravity;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFallingWeight extends Entity implements IEntityAdditionalSpawnData, IEvolutionEntity<EntityFallingWeight> {

    private final MutableBlockPos mutablePos = new MutableBlockPos();
    public int fallTime;
    private int mass = 500;
    private BlockState state = EvolutionBlocks.DESTROY_9.get().defaultBlockState();

    public EntityFallingWeight(EntityType<EntityFallingWeight> type, Level level) {
        super(type, level);
    }

    public EntityFallingWeight(Level level, double x, double y, double z, BlockState state) {
        this(EvolutionEntities.FALLING_WEIGHT.get(), level);
        this.state = state;
        this.mass = this.state.getBlock() instanceof BlockMass ? ((BlockMass) this.state.getBlock()).getMass(this.state) : 500;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

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
        category.setDetail("Imitating BlockState", this.state.toString());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    //TODO
//    @Override
//    public boolean func_241845_aY() {
//        return this.isAlive();
//    }

    /**
     * Returns the {@code BlockState} this entity is imitating.
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

//    @Override
//    protected boolean isMovementNoisy() {
//        return false;
//    }

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
        this.state = NbtUtils.readBlockState(buffer.readNbt());
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
        Block carryingBlock = this.state.getBlock();
        if (this.fallTime++ == 0) {
            BlockPos pos = this.blockPosition();
            if (this.level.getBlockState(pos).getBlock() == carryingBlock) {
                this.level.removeBlock(pos, false);
            }
            else if (!this.level.isClientSide) {
                this.discard();
                return;
            }
        }
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
        this.mutablePos.set(this.getX(), this.getY(), this.getZ());
        if (this.level.getBlockState(this.mutablePos.below()).getBlock() instanceof BlockLeaves) {
            if (this.state.getBlock() instanceof BlockLeaves) {
                this.level.setBlockAndUpdate(this.mutablePos, this.state);
                this.discard();
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
            BlockHitResult raytraceresult = this.level.clip(
                    new ClipContext(new Vec3(this.xo, this.yo, this.zo), new Vec3(this.getX(), this.getY(), this.getZ()), ClipContext.Block.COLLIDER,
                                    ClipContext.Fluid.SOURCE_ONLY, this));
            if (raytraceresult.getType() != HitResult.Type.MISS && this.level.getFluidState(raytraceresult.getBlockPos()).is(FluidTags.WATER)) {
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
                this.discard();
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
                this.discard();
                if (BlockUtils.isReplaceable(state)) {
//                    NonNullList<ItemStack> drops;
//                    if (state.getBlock() instanceof IReplaceable) {
//                        drops = ((IReplaceable) state.getBlock()).getDrops(this.level, this.mutablePos, state);
//                    }
//                    else {
//                        drops = NonNullList.of(ItemStack.EMPTY, new ItemStack(state.getBlock()));
//                    }
//                    if (this.level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
//                        for (ItemStack stack : drops) {
//                            this.spawnAtLocation(stack);
//                        }
//                    }
                    BlockUtils.destroyBlock(this.level, this.mutablePos);
                    this.level.setBlockAndUpdate(this.mutablePos, this.state);
                }
                else if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    if (carryingBlock instanceof BlockCobblestone cobble) {
                        this.spawnAtLocation(new ItemStack(cobble.getVariant().getRock(), 4));
                    }
                    else {
                        this.spawnAtLocation(carryingBlock);
                    }
                }
            }
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeBlockState(this.state));
    }
}