package tgw.evolution.entities.projectiles;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockClimbingStake;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.ATTACHED;
import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class EntityHook extends EntityGenericProjectile<EntityHook> {

    private Direction facing = Direction.NORTH;

    public EntityHook(World world, LivingEntity thrower) {
        super(EvolutionEntities.HOOK.get(), thrower, world, 1);
        this.facing = thrower.getDirection();
    }

    @SuppressWarnings("unused")
    public EntityHook(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.HOOK.get(), worldIn);
    }

    public EntityHook(EntityType<EntityHook> type, World worldIn) {
        super(type, worldIn);
    }

    public static int tryPlaceRopes(World world, BlockPos pos, Direction support, int count) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        mutablePos.set(pos);
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
                if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
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
                if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (currentMovement != Direction.DOWN && BlockClimbingStake.canGoDown(world, mutablePos)) {
                currentMovement = Direction.DOWN;
                stateTemp = world.getBlockState(mutablePos.move(Direction.DOWN));
                if (stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
                        continue;
                    }
                    return ropeCount;
                }
                if (stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    return ropeCount;
                }
                if (stateTemp.getBlock() instanceof IReplaceable) {
                    BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp));
                }
                world.setBlockAndUpdate(mutablePos, EvolutionBlocks.ROPE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp));
            }
            if (currentMovement == Direction.DOWN) {
                world.setBlockAndUpdate(mutablePos, EvolutionBlocks.ROPE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
            world.setBlockAndUpdate(mutablePos, EvolutionBlocks.GROUND_ROPE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
            ropeCount++;
        }
        return ropeCount;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putByte("Facing", (byte) this.facing.get3DDataValue());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getArrowStack() {
        return new ItemStack(EvolutionItems.climbing_hook.get());
    }

    @Override
    protected SoundEvent getHitBlockSound() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Nullable
    @Override
    public HitboxEntity<EntityHook> getHitbox() {
        return null;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        Entity entity = rayTraceResult.getEntity();
        if (!this.hitEntities.contains(entity.getId())) {
            this.hitEntities.add(entity.getId());
            Entity shooter = this.getShooter();
            DamageSourceEv source = EvolutionDamage.causeHookDamage(this, shooter == null ? this : shooter);
            float damage = MathHelper.ceil(4 * this.getDeltaMovement().length());
            if (entity instanceof LivingEntity && entity.isAttackable()) {
                LivingEntity living = (LivingEntity) entity;
                float oldHealth = living.getHealth();
                living.hurt(source, damage);
                if (shooter instanceof ServerPlayerEntity) {
                    this.applyDamageRaw((ServerPlayerEntity) shooter, damage, source.getType());
                    this.applyDamageActual((ServerPlayerEntity) shooter, oldHealth - living.getHealth(), source.getType(), living);
                }
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply(-0.1, -0.1, -0.1));
            this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
            Evolution.usingPlaceholder(entity.level.getNearestPlayer(this, 128), "sound");
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.facing = Direction.from3DDataValue(compound.getByte("Facing"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.timeInGround > 0) {
            this.tryPlaceBlock();
            this.remove();
        }
    }

    @Override
    protected void tryDespawn() {
    }

    public void tryPlaceBlock() {
        BlockPos pos = this.blockPosition();
        BlockPos down = pos.below();
        if (this.level.isEmptyBlock(pos) && BlockUtils.hasSolidSide(this.level, down, Direction.UP)) {
            this.level.setBlockAndUpdate(this.blockPosition(),
                                         EvolutionBlocks.CLIMBING_HOOK.get()
                                                                      .defaultBlockState()
                                                                      .setValue(DIRECTION_HORIZONTAL, this.facing.getOpposite()));
            LivingEntity shooter = this.getShooter();
            if (this.getShooter() instanceof PlayerEntity) {
                ItemStack stack = shooter.getOffhandItem();
                if (stack.getItem() == EvolutionItems.rope.get()) {
                    int count = stack.getCount();
                    int placed = tryPlaceRopes(this.level, pos, this.facing, count);
                    if (placed > 0) {
                        stack.shrink(placed);
                        this.level.setBlockAndUpdate(this.blockPosition(),
                                                     EvolutionBlocks.CLIMBING_HOOK.get()
                                                                                  .defaultBlockState()
                                                                                  .setValue(DIRECTION_HORIZONTAL, this.facing.getOpposite())
                                                                                  .setValue(ATTACHED, true));
                    }
                }
            }
        }
        else {
            BlockUtils.dropItemStack(this.level, pos, this.getArrowStack());
        }
    }
}
