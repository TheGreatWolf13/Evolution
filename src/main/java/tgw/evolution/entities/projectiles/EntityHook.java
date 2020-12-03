package tgw.evolution.entities.projectiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import tgw.evolution.Evolution;
import tgw.evolution.blocks.*;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionItems;

import javax.annotation.Nullable;

public class EntityHook extends EntityGenericProjectile {

    private boolean dealtDamage;
    private Direction facing = Direction.NORTH;

    public EntityHook(World worldIn, LivingEntity thrower) {
        super(EvolutionEntities.HOOK.get(), thrower, worldIn, 1);
        this.facing = thrower.getHorizontalFacing();
    }

    @SuppressWarnings("unused")
    public EntityHook(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.HOOK.get(), worldIn);
    }

    public EntityHook(EntityType<EntityHook> type, World worldIn) {
        super(type, worldIn);
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
                    BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp));
                }
                world.setBlockState(mutablePos, EvolutionBlocks.ROPE.get().getDefaultState().with(BlockRope.DIRECTION, support));
                ropeCount++;
                continue;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp));
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
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getArrowStack() {
        return new ItemStack(EvolutionItems.climbing_hook.get());
    }

    @Override
    protected SoundEvent getHitEntitySound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        Entity entity = rayTraceResult.getEntity();
        Entity shooter = this.getShooter();
        DamageSource source = EvolutionDamage.causeHookDamage(this, shooter == null ? this : shooter);
        this.dealtDamage = true;
        SoundEvent sound = SoundEvents.ITEM_TRIDENT_HIT;
        float damage = MathHelper.ceil(4 * this.getMotion().length());
        if (entity instanceof LivingEntity && entity.canBeAttackedWithItem()) {
            entity.attackEntityFrom(source, damage);
        }
        this.setMotion(this.getMotion().mul(-0.1, -0.1, -0.1));
        this.playSound(sound, 1.0F, 1.0F);
        Evolution.usingPlaceholder(entity.world.getClosestPlayer(this, 1_000), "sound");
    }

    @Override
    @Nullable
    protected EntityRayTraceResult rayTraceEntities(Vec3d startVec, Vec3d endVec) {
        return this.dealtDamage ? null : super.rayTraceEntities(startVec, endVec);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.dealtDamage = compound.getBoolean("DealtDamage");
        this.facing = Direction.byIndex(compound.getByte("Facing"));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.timeInGround > 0) {
            this.dealtDamage = true;
            this.tryPlaceBlock();
            this.remove();
        }
    }

    @Override
    protected void tryDespawn() {
    }

    public void tryPlaceBlock() {
        BlockPos pos = this.getPosition();
        BlockPos down = pos.down();
        if (this.world.isAirBlock(pos) && Block.hasSolidSide(this.world.getBlockState(down), this.world, down, Direction.UP)) {
            this.world.setBlockState(this.getPosition(),
                                     EvolutionBlocks.CLIMBING_HOOK.get()
                                                                  .getDefaultState()
                                                                  .with(BlockClimbingHook.ROPE_DIRECTION, this.facing.getOpposite()));
            LivingEntity shooter = this.getShooter();
            if (this.getShooter() instanceof PlayerEntity) {
                ItemStack stack = shooter.getHeldItemOffhand();
                if (stack.getItem() == EvolutionItems.rope.get()) {
                    int count = stack.getCount();
                    int placed = tryPlaceRopes(this.world, pos, this.facing, count);
                    if (placed > 0) {
                        stack.shrink(placed);
                        this.world.setBlockState(this.getPosition(),
                                                 EvolutionBlocks.CLIMBING_HOOK.get()
                                                                              .getDefaultState()
                                                                              .with(BlockClimbingHook.ROPE_DIRECTION, this.facing.getOpposite())
                                                                              .with(BlockClimbingHook.ATTACHED, true));
                    }
                }
            }
        }
        else {
            BlockUtils.dropItemStack(this.world, pos, this.getArrowStack());
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("DealtDamage", this.dealtDamage);
        compound.putByte("Facing", (byte) this.facing.getIndex());
    }
}
