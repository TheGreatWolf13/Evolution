package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(CommandBlock.class)
public abstract class Mixin_M_CommandBlock extends BaseEntityBlock implements GameMasterBlock {

    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private boolean automatic;

    public Mixin_M_CommandBlock(Properties properties) {
        super(properties);
    }

    @Unique
    private static void execute(BlockState blockState, Level level, int x, int y, int z, BaseCommandBlock baseCommandBlock, boolean bl) {
        if (bl) {
            baseCommandBlock.performCommand(level);
        }
        else {
            baseCommandBlock.setSuccessCount(0);
        }
        executeChain(level, x, y, z, blockState.getValue(FACING));
    }

    @Unique
    private static void executeChain(Level level, int x, int y, int z, Direction direction) {
        GameRules gameRules = level.getGameRules();
        int max = gameRules.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
        int num;
        BlockState stateAtPos;
        int x0 = x;
        int y0 = y;
        int z0 = z;
        for (num = max; num-- > 0; direction = stateAtPos.getValue(FACING)) {
            x0 += direction.getStepX();
            y0 += direction.getStepY();
            z0 += direction.getStepZ();
            stateAtPos = level.getBlockState_(x0, y0, z0);
            if (!stateAtPos.is(Blocks.CHAIN_COMMAND_BLOCK)) {
                break;
            }
            if (!(level.getBlockEntity_(x0, y0, z0) instanceof CommandBlockEntity te)) {
                break;
            }
            if (te.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
                break;
            }
            if (te.isPowered() || te.isAutomatic()) {
                BaseCommandBlock commandBlock = te.getCommandBlock();
                if (te.markConditionMet()) {
                    if (!commandBlock.performCommand(level)) {
                        break;
                    }
                    level.updateNeighbourForOutputSignal_(x0, y0, z0, stateAtPos.getBlock());
                }
                else if (te.isConditional()) {
                    commandBlock.setSuccessCount(0);
                }
            }
        }
        if (num <= 0) {
            LOGGER.warn("Command Block chain tried to execute more than {} steps!", Math.max(max, 0));
        }
    }

    @Overwrite
    @DeleteMethod
    private static void executeChain(Level level, BlockPos blockPos, Direction direction) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @DeleteMethod
    private void execute(BlockState blockState, Level level, BlockPos blockPos, BaseCommandBlock baseCommandBlock, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        if (!level.isClientSide) {
            if (level.getBlockEntity_(x, y, z) instanceof CommandBlockEntity tile) {
                BlockPos pos = new BlockPos(x, y, z);
                boolean hasPower = level.hasNeighborSignal(pos);
                boolean isPowered = tile.isPowered();
                tile.setPowered(hasPower);
                if (!isPowered && !tile.isAutomatic() && tile.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
                    if (hasPower) {
                        tile.markConditionMet();
                        level.scheduleTick(pos, this, 1);
                    }
                }
            }
        }
    }

    @Override
    @Overwrite
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (level.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ()) instanceof CommandBlockEntity tile) {
            BaseCommandBlock commandBlock = tile.getCommandBlock();
            if (stack.hasCustomHoverName()) {
                commandBlock.setName(stack.getHoverName());
            }
            if (!level.isClientSide) {
                if (BlockItem.getBlockEntityData(stack) == null) {
                    commandBlock.setTrackOutput(level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK));
                    tile.setAutomatic(this.automatic);
                }
                if (tile.getMode() == CommandBlockEntity.Mode.SEQUENCE) {
                    boolean hasNeighborSignal = level.hasNeighborSignal(pos);
                    tile.setPowered(hasNeighborSignal);
                }
            }
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getBlockEntity_(x, y, z) instanceof CommandBlockEntity tile) {
            BaseCommandBlock commandBlock = tile.getCommandBlock();
            boolean notEmpty = !StringUtil.isNullOrEmpty(commandBlock.getCommand());
            CommandBlockEntity.Mode mode = tile.getMode();
            boolean conditionMet = tile.wasConditionMet();
            if (mode == CommandBlockEntity.Mode.AUTO) {
                tile.markConditionMet();
                if (conditionMet) {
                    execute(state, level, x, y, z, commandBlock, notEmpty);
                }
                else if (tile.isConditional()) {
                    commandBlock.setSuccessCount(0);
                }
                if (tile.isPowered() || tile.isAutomatic()) {
                    level.scheduleTick(new BlockPos(x, y, z), this, 1);
                }
            }
            else if (mode == CommandBlockEntity.Mode.REDSTONE) {
                if (conditionMet) {
                    execute(state, level, x, y, z, commandBlock, notEmpty);
                }
                else if (tile.isConditional()) {
                    commandBlock.setSuccessCount(0);
                }
            }
            level.updateNeighbourForOutputSignal_(x, y, z, this);
        }
    }

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
        if (player.canUseGameMasterBlocks() && level.getBlockEntity_(x, y, z) instanceof CommandBlockEntity tile) {
            player.openCommandBlock(tile);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
