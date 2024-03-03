package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(BeehiveBlock.class)
public abstract class Mixin_M_BeehiveBlock extends BaseEntityBlock {

    @Shadow @Final public static IntegerProperty HONEY_LEVEL;

    public Mixin_M_BeehiveBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static void dropHoneycomb(Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract void angerNearbyBees(Level level, BlockPos blockPos);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (state.getValue(HONEY_LEVEL) >= 5) {
            BlockPos pos = new BlockPos(x, y, z);
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                this.trySpawnDripParticles(level, pos, state);
            }
        }
    }

    @Shadow
    protected abstract boolean hiveContainsBees(Level level, BlockPos blockPos);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerDestroy(Level level,
                              Player player,
                              BlockPos blockPos,
                              BlockState blockState,
                              @Nullable BlockEntity blockEntity,
                              ItemStack itemStack) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerDestroy_(Level level, Player player, int x, int y, int z, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy_(level, player, x, y, z, state, te, stack);
        if (!level.isClientSide && te instanceof BeehiveBlockEntity tile) {
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
                tile.emptyAllLivingFromHive(player, state, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                BlockPos pos = new BlockPos(x, y, z);
                level.updateNeighbourForOutputSignal(pos, this);
                this.angerNearbyBees(level, pos);
            }
            CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer) player, state, stack, tile.getOccupantCount());
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            if (level.getBlockEntity_(x, y, z) instanceof BeehiveBlockEntity tile) {
                ItemStack stack = new ItemStack(this);
                int i = state.getValue(HONEY_LEVEL);
                boolean notEmpty = !tile.isEmpty();
                if (notEmpty || i > 0) {
                    CompoundTag compoundTag;
                    if (notEmpty) {
                        compoundTag = new CompoundTag();
                        compoundTag.put("Bees", tile.writeBees());
                        BlockItem.setBlockEntityData(stack, BlockEntityType.BEEHIVE, compoundTag);
                    }
                    compoundTag = new CompoundTag();
                    compoundTag.putInt("honey_level", i);
                    stack.addTagElement("BlockStateTag", compoundTag);
                    ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        }
        super.playerWillDestroy_(level, x, y, z, state, player);
    }

    @Shadow
    public abstract void releaseBeesAndResetHoneyLevel(Level level,
                                                       BlockState blockState,
                                                       BlockPos blockPos,
                                                       @Nullable Player player,
                                                       BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus);

    @Shadow
    public abstract void resetHoneyLevel(Level level, BlockState blockState, BlockPos blockPos);

    @Shadow
    protected abstract void trySpawnDripParticles(Level level, BlockPos blockPos, BlockState blockState);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        if (fromState.getBlock() instanceof FireBlock) {
            if (level.getBlockEntity_(x, y, z) instanceof BeehiveBlockEntity tile) {
                tile.emptyAllLivingFromHive(null, state, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        int honeyLevel = state.getValue(HONEY_LEVEL);
        boolean handled = false;
        if (honeyLevel >= 5) {
            Item item = stack.getItem();
            if (stack.is(Items.SHEARS)) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
                BlockPos pos = new BlockPos(x, y, z);
                dropHoneycomb(level, pos);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                handled = true;
                level.gameEvent(player, GameEvent.SHEAR, pos);
            }
            else if (stack.is(Items.GLASS_BOTTLE)) {
                stack.shrink(1);
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, new ItemStack(Items.HONEY_BOTTLE));
                }
                else if (!player.getInventory().add(new ItemStack(Items.HONEY_BOTTLE))) {
                    player.drop(new ItemStack(Items.HONEY_BOTTLE), false);
                }
                handled = true;
                level.gameEvent(player, GameEvent.FLUID_PICKUP, new BlockPos(x, y, z));
            }
            if (!level.isClientSide() && handled) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }
        }
        if (handled) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!CampfireBlock.isSmokeyPos(level, pos)) {
                if (this.hiveContainsBees(level, pos)) {
                    this.angerNearbyBees(level, pos);
                }
                this.releaseBeesAndResetHoneyLevel(level, state, pos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
            else {
                this.resetHoneyLevel(level, state, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use_(state, level, x, y, z, player, hand, hitResult);
    }
}
