package tgw.evolution.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.*;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.Gravity;

import javax.annotation.Nullable;

public class EntityHook extends AbstractArrowEntity {

    private final double gravity;
    private boolean dealtDamage;
    private Direction facing = Direction.NORTH;

    public EntityHook(EntityType<EntityHook> type, World worldIn) {
        super(type, worldIn);
        this.gravity = -Gravity.gravity(worldIn.getDimension());
    }

    public EntityHook(World worldIn, LivingEntity thrower) {
        super(EvolutionEntities.HOOK.get(), thrower, worldIn);
        this.gravity = -Gravity.gravity(worldIn.getDimension());
        this.facing = thrower.getHorizontalFacing();
    }

    @SuppressWarnings("unused")
    public EntityHook(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.HOOK.get(), worldIn);
    }

    public static int tryPlaceRopes(World world, BlockPos pos, Direction support, int count) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        Direction currentMovement = support.getOpposite();
        int ropeCount = 0;
        for (int distance = 1; distance <= 5; distance++) {
            if (ropeCount == count) {
                return count;
            }
            mutablePos.move(currentMovement);
            BlockState stateTemp = world.getBlockState(mutablePos);
            if (!BlockUtils.isReplaceable(stateTemp)) {
                return ropeCount;
            }
            if (currentMovement == Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (stateTemp.get(BlockRope.DIRECTION) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                if (!((IReplaceable) stateTemp.getBlock()).canBeReplacedByRope(stateTemp)) {
                    return ropeCount;
                }
            }
            if (currentMovement != Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                if (stateTemp.get(BlockRopeGround.ORIGIN) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (currentMovement != Direction.DOWN && BlockClimbingStake.canGoDown(world, mutablePos)) {
                currentMovement = Direction.DOWN;
                stateTemp = world.getBlockState(mutablePos.move(Direction.DOWN));
                if (stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (stateTemp.get(BlockRope.DIRECTION) == support) {
                        continue;
                    }
                    return ropeCount;
                }
                if (stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    return ropeCount;
                }
                if (stateTemp.getBlock() instanceof IReplaceable) {
                    BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(stateTemp));
                }
                world.setBlockState(mutablePos, EvolutionBlocks.ROPE.get().getDefaultState().with(BlockRope.DIRECTION, support));
                ropeCount++;
                continue;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(stateTemp));
            }
            if (currentMovement == Direction.DOWN) {
                world.setBlockState(mutablePos, EvolutionBlocks.ROPE.get().getDefaultState().with(BlockRope.DIRECTION, support));
                ropeCount++;
                continue;
            }
            world.setBlockState(mutablePos, EvolutionBlocks.GROUND_ROPE.get().getDefaultState().with(BlockRopeGround.ORIGIN, support));
            ropeCount++;
        }
        return ropeCount;
    }

    @Override
    protected ItemStack getArrowStack() {
        return new ItemStack(EvolutionItems.climbing_hook.get());
    }

    @Override
    @Nullable
    protected EntityRayTraceResult rayTraceEntities(Vec3d startVec, Vec3d endVec) {
        return this.dealtDamage ? null : super.rayTraceEntities(startVec, endVec);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        Entity entity = rayTraceResult.getEntity();
        Entity shooter = this.getShooter();
        DamageSource damagesource = EvolutionDamage.causeHookDamage(this, shooter == null ? this : shooter);
        this.dealtDamage = true;
        //TODO trident hit sound
        SoundEvent soundevent = SoundEvents.ITEM_TRIDENT_HIT;
        float damage = MathHelper.ceil(Math.max(4 * this.getMotion().length(), 0));
        if (entity.attackEntityFrom(damagesource, damage) && entity instanceof LivingEntity) {
            LivingEntity livingentity1 = (LivingEntity) entity;
            this.arrowHit(livingentity1);
        }
        this.setMotion(this.getMotion().mul(-0.01D, -0.1D, -0.01D));
        this.playSound(soundevent, 1.0F, 1.0F);
    }

    @Override
    protected float getWaterDrag() {
        return 0.8F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    protected void tryDespawn() {
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("DealtDamage", this.dealtDamage);
        compound.putByte("Facing", (byte) this.facing.getIndex());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.dealtDamage = compound.getBoolean("DealtDamage");
        this.facing = Direction.byIndex(compound.getByte("Facing"));
    }

    //TODO trident hit ground sound
    @Override
    protected SoundEvent getHitEntitySound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    public void tick() {
        if (this.timeInGround > 0) {
            this.dealtDamage = true;
            this.tryPlaceBlock();
            this.remove();
        }
        if (!this.hasNoGravity() && !this.getNoClip()) {
            Vec3d vec3d3 = this.getMotion();
            this.setMotion(vec3d3.x, vec3d3.y + 0.05F + this.gravity, vec3d3.z);
        }
        super.tick();
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void tryPlaceBlock() {
        BlockPos pos = this.getPosition();
        BlockPos down = pos.down();
        if (this.world.isAirBlock(pos) && Block.hasSolidSide(this.world.getBlockState(down), this.world, down, Direction.UP)) {
            this.world.setBlockState(this.getPosition(), EvolutionBlocks.CLIMBING_HOOK.get().getDefaultState().with(BlockClimbingHook.ROPE_DIRECTION, this.facing.getOpposite()));
            Entity shooter = this.getShooter();
            if (this.getShooter() instanceof PlayerEntity) {
                ItemStack stack = ((PlayerEntity) shooter).getHeldItemOffhand();
                if (stack.getItem() == EvolutionItems.rope.get()) {
                    int count = stack.getCount();
                    int placed = tryPlaceRopes(this.world, pos, this.facing, count);
                    if (placed > 0) {
                        stack.shrink(placed);
                        this.world.setBlockState(this.getPosition(), EvolutionBlocks.CLIMBING_HOOK.get().getDefaultState().with(BlockClimbingHook.ROPE_DIRECTION, this.facing.getOpposite()).with(BlockClimbingHook.ATTACHED, true));
                    }
                }
            }
        }
        else {
            BlockUtils.dropItemStack(this.world, pos, this.getArrowStack());
        }
    }
}
