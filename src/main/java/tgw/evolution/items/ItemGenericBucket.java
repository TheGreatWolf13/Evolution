package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IBlockFluidContainer;
import tgw.evolution.blocks.fluids.BlockGenericFluid;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.util.BlockUtils;

import java.util.function.Supplier;

public abstract class ItemGenericBucket extends ItemEv implements IItemFluidContainer {

    private final Supplier<? extends Fluid> fluid;

    public ItemGenericBucket(Supplier<? extends Fluid> fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
    }

    private static ItemStack fillBucket(ItemStack emptyBucket, Player player, Item fullBucket, int amount) {
        if (player.getAbilities().instabuild) {
            return emptyBucket;
        }
        emptyBucket.shrink(1);
        ItemStack filledBucket = IItemFluidContainer.getStack((IItemFluidContainer) fullBucket, amount);
        if (emptyBucket.isEmpty()) {
            return filledBucket;
        }
        if (!player.getInventory().add(filledBucket)) {
            player.drop(filledBucket, false);
        }
        return emptyBucket;
    }

    public abstract ItemStack emptyBucket();

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
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
    public String getDescriptionId() {
        if (this.emptyBucket().getItem() == this) {
            return super.getDescriptionId();
        }
        return this.emptyBucket().getItem().getDescriptionId();
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
        CompoundTag tag = new CompoundTag();
        tag.putInt("Amount", amount);
        ItemStack stack = new ItemStack(this);
        stack.setTag(tag);
        return stack;
    }

    @Override
    public ItemStack getStackAfterPlacement(Player player, ItemStack fullBucket, int amountPlaced) {
        if (player.isCreative()) {
            return fullBucket;
        }
        int amount = this.getAmount(fullBucket) - amountPlaced;
        if (amount == 0) {
            return this.emptyBucket();
        }
        ItemStack stack = new ItemStack(fullBucket.getItem());
        CompoundTag tag = new CompoundTag();
        tag.putInt("Amount", amount);
        stack.setTag(tag);
        return stack;
    }

    public void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos) {
        SoundEvent sound = this.getFluid().getAttributes().getEmptySound();
        if (sound == null) {
            sound = this.getFluid().is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * @return How much of the fluid was placed.
     */
    public int tryPlaceContainedLiquid(@Nullable Player player,
                                       Level level,
                                       BlockPos pos,
                                       @Nullable BlockHitResult blockHitResult,
                                       ItemStack stackInHand) {
        if (!(this.getFluid() instanceof FluidGeneric)) {
            return 0;
        }
        BlockState stateAtPos = level.getBlockState(pos);
        Material materialAtPos = stateAtPos.getMaterial();
        boolean isReplaceable = BlockUtils.canBeReplacedByFluid(stateAtPos);
        if (level.isEmptyBlock(pos) || isReplaceable) {
            int placed = 0;
            if (this.getFluid().is(FluidTags.WATER) && level.dimensionType().ultraWarm()) {
                int posX = pos.getX();
                int posY = pos.getY();
                int posZ = pos.getZ();
                level.playSound(player,
                                pos,
                                SoundEvents.FIRE_EXTINGUISH,
                                SoundSource.BLOCKS,
                                0.5F,
                                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

                for (int l = 0; l < 8; ++l) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, posX + Math.random(), posY + Math.random(), posZ + Math.random(), 0, 0, 0);
                }
            }
            else if (stateAtPos.getBlock() instanceof IBlockFluidContainer blockFluidContainer) {
                placed = blockFluidContainer.receiveFluid(level, pos, stateAtPos, (FluidGeneric) this.getFluid(), this.getAmount(stackInHand));
                if (placed > 0) {
                    this.playEmptySound(player, level, pos);
                }
            }
            else {
                if (!level.isClientSide && isReplaceable && !materialAtPos.isLiquid()) {
                    level.destroyBlock(pos, true);
                }
                this.playEmptySound(player, level, pos);
                BlockGenericFluid.place(level, pos, (FluidGeneric) this.getFluid(), this.getAmount(stackInHand));
                return this.getAmount(stackInHand);
            }
            return placed;
        }
        return blockHitResult != null ?
               this.tryPlaceContainedLiquid(player, level, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null, stackInHand) :
               0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, !this.isFull(stackInHand) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stackInHand);
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stackInHand);
        }
        BlockPos pos = hitResult.getBlockPos();
        if (level.mayInteract(player, pos) && player.mayUseItemAt(pos, hitResult.getDirection(), stackInHand)) {
            BlockState stateAtPos = level.getBlockState(pos);
            //The bucket is empty
            if (this.getFluid() == Fluids.EMPTY) {
                if (stateAtPos.getBlock() instanceof IBlockFluidContainer) {
                    Fluid fluid = ((IBlockFluidContainer) stateAtPos.getBlock()).getFluid(level, pos);
                    if (fluid != Fluids.EMPTY) {
                        int amount = ((IBlockFluidContainer) stateAtPos.getBlock()).getAmountRemoved(level, pos, this.getMaxAmount());
                        if (amount > 0) {
                            player.awardStat(Stats.ITEM_USED.get(this));
                            SoundEvent sound = this.getFluid().getAttributes().getEmptySound();
                            if (sound == null) {
                                sound = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
                            }
                            player.playSound(sound, 1.0F, 1.0F);
                            ItemStack fillStack = fillBucket(stackInHand, player, this.getFullBucket(fluid), amount);
                            if (!level.isClientSide) {
                                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, new ItemStack(this.getFullBucket(fluid)));
                            }
                            return new InteractionResultHolder<>(InteractionResult.SUCCESS, fillStack);
                        }
                    }
                }
                return new InteractionResultHolder<>(InteractionResult.FAIL, stackInHand);
            }
            //The bucket is not full, but has fluid
            if (!this.isFull(stackInHand) &&
                stateAtPos.getBlock() instanceof IBlockFluidContainer &&
                this.getFluid() == ((IBlockFluidContainer) stateAtPos.getBlock()).getFluid(level, pos)) {
                int amount = ((IBlockFluidContainer) stateAtPos.getBlock()).getAmountRemoved(level, pos, this.getMissingAmount(stackInHand));
                if (amount > 0) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    SoundEvent sound = this.getFluid().getAttributes().getEmptySound();
                    if (sound == null) {
                        sound = this.getFluid().is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
                    }
                    player.playSound(sound, 1.0F, 1.0F);
                    ItemStack fillStack = fillBucket(stackInHand, player, this.getFullBucket(this.getFluid()), this.getAmount(stackInHand) + amount);
                    if (!level.isClientSide) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, new ItemStack(this.getFullBucket(this.getFluid())));
                    }
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, fillStack);
                }
            }
            BlockPos movedPos = stateAtPos.getBlock() instanceof IBlockFluidContainer &&
                                this.getFluid() == ((IBlockFluidContainer) stateAtPos.getBlock()).getFluid(level, pos) ?
                                pos :
                                hitResult.getBlockPos().relative(hitResult.getDirection());
            int placed = this.tryPlaceContainedLiquid(player, level, movedPos, hitResult, stackInHand);
            if (placed > 0) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, movedPos, stackInHand);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, this.getStackAfterPlacement(player, stackInHand, placed));
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, stackInHand);
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, stackInHand);
    }
}
