package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(CommandBlock.class)
public abstract class Mixin_M_CommandBlock extends BaseEntityBlock implements GameMasterBlock {

    @Shadow @Final private boolean automatic;

    public Mixin_M_CommandBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void execute(BlockState blockState, Level level, BlockPos blockPos, BaseCommandBlock baseCommandBlock, boolean bl);

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
                    this.execute(state, level, new BlockPos(x, y, z), commandBlock, notEmpty);
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
                    this.execute(state, level, new BlockPos(x, y, z), commandBlock, notEmpty);
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
