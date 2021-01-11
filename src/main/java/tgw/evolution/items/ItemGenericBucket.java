package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IBlockFluidContainer;
import tgw.evolution.blocks.fluids.BlockGenericFluid;
import tgw.evolution.blocks.fluids.FluidGeneric;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class ItemGenericBucket extends ItemEv implements IItemFluidContainer {

    private final Supplier<? extends Fluid> fluid;

    public ItemGenericBucket(Supplier<? extends Fluid> fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
    }

    private static ItemStack fillBucket(ItemStack emptyBucket, PlayerEntity player, Item fullBucket, int amount) {
        if (player.abilities.isCreativeMode) {
            return emptyBucket;
        }
        emptyBucket.shrink(1);
        ItemStack filledBucket = IItemFluidContainer.getStack((IItemFluidContainer) fullBucket, amount);
        if (emptyBucket.isEmpty()) {
            return filledBucket;
        }
        if (!player.inventory.addItemStackToInventory(filledBucket)) {
            player.dropItem(filledBucket, false);
        }
        return emptyBucket;
    }

    public abstract ItemStack emptyBucket();

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(this.getDefaultInstance());
        }
    }

    @Override
    public ItemStack getDefaultInstance() {
        if (this.emptyBucket().getItem() == this) {
            return new ItemStack(this);
        }
        return this.getFullStack();
    }

    @Override
    public Fluid getFluid() {
        return this.fluid.get();
    }

    public abstract Item getFullBucket(Fluid fluid);

    @Override
    public ItemStack getStack(int amount) {
        if (amount == 0) {
            return new ItemStack(this);
        }
        if (amount > this.getMaxAmount()) {
            amount = this.getMaxAmount();
        }
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("Amount", amount);
        ItemStack stack = new ItemStack(this);
        stack.setTag(tag);
        return stack;
    }

    @Override
    public ItemStack getStackAfterPlacement(PlayerEntity player, ItemStack fullBucket, int amountPlaced) {
        if (player.isCreative()) {
            return fullBucket;
        }
        int amount = this.getAmount(fullBucket) - amountPlaced;
        if (amount == 0) {
            return this.emptyBucket();
        }
        ItemStack stack = new ItemStack(fullBucket.getItem());
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("Amount", amount);
        stack.setTag(nbt);
        return stack;
    }

    @Override
    public String getTranslationKey() {
        if (this.emptyBucket().getItem() == this) {
            return super.getTranslationKey();
        }
        return this.emptyBucket().getItem().getTranslationKey();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getHeldItem(hand);
        RayTraceResult rayTrace = rayTrace(world, player, !this.isFull(stackInHand) ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
        if (rayTrace.getType() == RayTraceResult.Type.MISS) {
            return new ActionResult<>(ActionResultType.PASS, stackInHand);
        }
        if (rayTrace.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.PASS, stackInHand);
        }
        BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) rayTrace;
        BlockPos pos = blockRayTrace.getPos();
        if (world.isBlockModifiable(player, pos) && player.canPlayerEdit(pos, blockRayTrace.getFace(), stackInHand)) {
            BlockState stateAtPos = world.getBlockState(pos);
            //The bucket is empty
            if (this.getFluid() == Fluids.EMPTY) {
                if (stateAtPos.getBlock() instanceof IBlockFluidContainer) {
                    Fluid fluid = ((IBlockFluidContainer) stateAtPos.getBlock()).getFluid(world, pos);
                    if (fluid != Fluids.EMPTY) {
                        int amount = ((IBlockFluidContainer) stateAtPos.getBlock()).getAmountRemoved(world, pos, this.getMaxAmount());
                        if (amount > 0) {
                            player.addStat(Stats.ITEM_USED.get(this));
                            SoundEvent sound = this.getFluid().getAttributes().getEmptySound();
                            if (sound == null) {
                                sound = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                            }
                            player.playSound(sound, 1.0F, 1.0F);
                            ItemStack fillStack = fillBucket(stackInHand, player, this.getFullBucket(fluid), amount);
                            if (!world.isRemote) {
                                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayerEntity) player, new ItemStack(this.getFullBucket(fluid)));
                            }
                            return new ActionResult<>(ActionResultType.SUCCESS, fillStack);
                        }
                    }
                }
                return new ActionResult<>(ActionResultType.FAIL, stackInHand);
            }
            //The bucket is not full, but has fluid
            if (!this.isFull(stackInHand) &&
                stateAtPos.getBlock() instanceof IBlockFluidContainer &&
                this.getFluid() == ((IBlockFluidContainer) stateAtPos.getBlock()).getFluid(world, pos)) {
                int amount = ((IBlockFluidContainer) stateAtPos.getBlock()).getAmountRemoved(world, pos, this.getMissingAmount(stackInHand));
                if (amount > 0) {
                    player.addStat(Stats.ITEM_USED.get(this));
                    SoundEvent sound = this.getFluid().getAttributes().getEmptySound();
                    if (sound == null) {
                        sound = this.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                    }
                    player.playSound(sound, 1.0F, 1.0F);
                    ItemStack fillStack = fillBucket(stackInHand, player, this.getFullBucket(this.getFluid()), this.getAmount(stackInHand) + amount);
                    if (!world.isRemote) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayerEntity) player, new ItemStack(this.getFullBucket(this.getFluid())));
                    }
                    return new ActionResult<>(ActionResultType.SUCCESS, fillStack);
                }
            }
            BlockPos movedPos = stateAtPos.getBlock() instanceof IBlockFluidContainer &&
                                this.getFluid() == ((IBlockFluidContainer) stateAtPos.getBlock()).getFluid(world, pos) ?
                                pos :
                                blockRayTrace.getPos().offset(blockRayTrace.getFace());
            int placed = this.tryPlaceContainedLiquid(player, world, movedPos, blockRayTrace, stackInHand);
            if (placed > 0) {
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, movedPos, stackInHand);
                }
                player.addStat(Stats.ITEM_USED.get(this));
                return new ActionResult<>(ActionResultType.SUCCESS, this.getStackAfterPlacement(player, stackInHand, placed));
            }
            return new ActionResult<>(ActionResultType.FAIL, stackInHand);
        }
        return new ActionResult<>(ActionResultType.FAIL, stackInHand);
    }

    public void playEmptySound(@Nullable PlayerEntity player, IWorld world, BlockPos pos) {
        SoundEvent sound = this.getFluid().getAttributes().getEmptySound();
        if (sound == null) {
            sound = this.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        }
        world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * @return How much of the fluid was placed.
     */
    public int tryPlaceContainedLiquid(@Nullable PlayerEntity player,
                                       World world,
                                       BlockPos pos,
                                       @Nullable BlockRayTraceResult blockRayTrace,
                                       ItemStack stackInHand) {
        if (!(this.getFluid() instanceof FluidGeneric)) {
            return 0;
        }
        BlockState stateAtPos = world.getBlockState(pos);
        Material materialAtPos = stateAtPos.getMaterial();
        boolean isReplaceable = BlockUtils.canBeReplacedByFluid(stateAtPos);
        if (world.isAirBlock(pos) || isReplaceable) {
            int placed = 0;
            if (this.getFluid().isIn(FluidTags.WATER) && world.dimension.doesWaterVaporize()) {
                int posX = pos.getX();
                int posY = pos.getY();
                int posZ = pos.getZ();
                world.playSound(player,
                                pos,
                                SoundEvents.BLOCK_FIRE_EXTINGUISH,
                                SoundCategory.BLOCKS,
                                0.5F,
                                2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                for (int l = 0; l < 8; ++l) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, posX + Math.random(), posY + Math.random(), posZ + Math.random(), 0, 0, 0);
                }
            }
            else if (stateAtPos.getBlock() instanceof IBlockFluidContainer) {
                placed = ((IBlockFluidContainer) stateAtPos.getBlock()).receiveFluid(world,
                                                                                     pos,
                                                                                     stateAtPos,
                                                                                     (FluidGeneric) this.getFluid(),
                                                                                     this.getAmount(stackInHand));
                if (placed > 0) {
                    this.playEmptySound(player, world, pos);
                }
            }
            else {
                if (!world.isRemote && isReplaceable && !materialAtPos.isLiquid()) {
                    world.destroyBlock(pos, true);
                }
                this.playEmptySound(player, world, pos);
                BlockGenericFluid.place(world, pos, (FluidGeneric) this.getFluid(), this.getAmount(stackInHand));
                return this.getAmount(stackInHand);
            }
            return placed;
        }
        return blockRayTrace != null ?
               this.tryPlaceContainedLiquid(player, world, blockRayTrace.getPos().offset(blockRayTrace.getFace()), null, stackInHand) :
               0;
    }
}
