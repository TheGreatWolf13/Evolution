package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.Evolution;
import tgw.evolution.network.PacketSCBlockBreakAck;
import tgw.evolution.patches.PatchServerPlayerGameMode;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.BlockPosUtil;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode implements PatchServerPlayerGameMode {

    @Shadow @Final private static Logger LOGGER;
    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;
    @Shadow private BlockPos delayedDestroyPos;
    @Shadow private int delayedTickStart;
    @Shadow private BlockPos destroyPos;
    @Shadow private int destroyProgressStart;
    @Shadow private GameType gameModeForPlayer;
    @Shadow private int gameTicks;
    @Shadow private boolean hasDelayedDestroy;
    @Shadow private boolean isDestroyingBlock;
    @Shadow private int lastSentState;

    @Overwrite
    public void destroyAndAck(BlockPos pos, ServerboundPlayerActionPacket.Action action, String string) {
        Evolution.deprecatedMethod();
        this.destroyAndAck_(pos.asLong(), action);
    }

    @Override
    public void destroyAndAck_(long pos, ServerboundPlayerActionPacket.Action action) {
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        if (this.destroyBlock_(x, y, z)) {
            this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, true));
        }
        else {
            this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, false));
        }
    }

    @Overwrite
    public boolean destroyBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.destroyBlock_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean destroyBlock_(int x, int y, int z) {
        BlockState state = this.level.getBlockState_(x, y, z);
        if (!this.player.getMainHandItem().getItem().canAttackBlock_(state, this.level, x, y, z, this.player)) {
            return false;
        }
        Block block = state.getBlock();
        if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
            this.level.sendBlockUpdated_(x, y, z, state, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
            return false;
        }
        if (this.player.blockActionRestricted_(this.level, x, y, z, this.gameModeForPlayer)) {
            return false;
        }
        block.playerWillDestroy_(this.level, x, y, z, state, this.player);
        boolean removed = this.level.removeBlock_(x, y, z, false);
        if (removed) {
            block.destroy_(this.level, x, y, z, state);
        }
        if (this.isCreative()) {
            return true;
        }
        ItemStack mainHandItem = this.player.getMainHandItem();
        ItemStack copy = mainHandItem.copy();
        mainHandItem.mineBlock_(this.level, state, x, y, z, this.player);
        if (removed && this.player.hasCorrectToolForDrops(state)) {
            block.playerDestroy_(this.level, this.player, x, y, z, state, this.level.getBlockEntity_(x, y, z), copy);
        }
        return true;
    }

    @Overwrite
    public void handleBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int buildHeight) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void handleBlockBreakAction_(long pos,
                                        ServerboundPlayerActionPacket.Action action,
                                        Direction direction,
                                        double hitX,
                                        double hitY,
                                        double hitZ,
                                        int buildHeight) {
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        double dx = this.player.getX() - x - 0.5;
        double dy = this.player.getY() - y + 1;
        double dz = this.player.getZ() - z - 0.5;
        double distSqr = dx * dx + dy * dy + dz * dz;
        if (distSqr > 9 * 9 || distSqr > 6 * 6 && !this.player.isCreative()) {
            BlockState state;
            if (this.player.level.getServer() != null &&
                BlockPosUtil.getChessBoardDistance(this.player.chunkPosition(),
                                                   SectionPos.blockToSectionCoord(x),
                                                   SectionPos.blockToSectionCoord(z)) <
                this.player.level.getServer().getPlayerList().getViewDistance()) {
                state = this.level.getBlockState_(x, y, z);
            }
            else {
                state = Blocks.AIR.defaultBlockState();
            }
            this.player.connection.send(new PacketSCBlockBreakAck(pos, state, action, false));
        }
        else if (y >= buildHeight) {
            this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, false));
        }
        else {
            BlockState stateAtPos;
            switch (action) {
                case START_DESTROY_BLOCK -> {
                    if (!this.level.mayInteract_(this.player, x, y, z)) {
                        this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, false));
                        return;
                    }
                    if (this.isCreative()) {
                        this.destroyAndAck_(pos, action);
                        return;
                    }
                    if (this.player.blockActionRestricted_(this.level, x, y, z, this.gameModeForPlayer)) {
                        this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, false));
                        return;
                    }
                    this.destroyProgressStart = this.gameTicks;
                    float h = 1.0F;
                    stateAtPos = this.level.getBlockState_(x, y, z);
                    if (!stateAtPos.isAir()) {
                        assert !Double.isNaN(hitX) && !Double.isNaN(hitY) && !Double.isNaN(hitZ) : "Received invalid hit pos!";
                        stateAtPos.attack_(this.level, x, y, z, direction, hitX, hitY, hitZ, this.player);
                        h = stateAtPos.getDestroyProgress_(this.player, this.player.level, x, y, z);
                    }
                    if (!stateAtPos.isAir() && h >= 1.0F) {
                        this.destroyAndAck_(pos, action);
                    }
                    else {
                        if (this.isDestroyingBlock) {
                            this.player.connection.send(
                                    new PacketSCBlockBreakAck(this.destroyPos.asLong(), this.level.getBlockState_(this.destroyPos),
                                                              ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, false));
                        }
                        this.isDestroyingBlock = true;
                        ((BlockPos.MutableBlockPos) this.destroyPos).set(x, y, z);
                        int j = (int) (h * 10.0F);
                        this.level.destroyBlockProgress(this.player.getId(), pos, j);
                        this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, true));
                        this.lastSentState = j;
                    }
                }
                case STOP_DESTROY_BLOCK -> {
                    if (x == this.destroyPos.getX() && y == this.destroyPos.getY() && z == this.destroyPos.getZ()) {
                        int k = this.gameTicks - this.destroyProgressStart;
                        stateAtPos = this.level.getBlockState_(x, y, z);
                        if (!stateAtPos.isAir()) {
                            float l = stateAtPos.getDestroyProgress_(this.player, this.player.level, x, y, z) * (k + 1);
                            if (l >= 0.7F) {
                                this.isDestroyingBlock = false;
                                this.level.destroyBlockProgress(this.player.getId(), pos, -1);
                                this.destroyAndAck_(pos, action);
                                return;
                            }
                            if (!this.hasDelayedDestroy) {
                                this.isDestroyingBlock = false;
                                this.hasDelayedDestroy = true;
                                ((BlockPos.MutableBlockPos) this.delayedDestroyPos).set(x, y, z);
                                this.delayedTickStart = this.destroyProgressStart;
                            }
                        }
                    }
                    this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, true));
                }
                case ABORT_DESTROY_BLOCK -> {
                    this.isDestroyingBlock = false;
                    if (x != this.destroyPos.getX() || y != this.destroyPos.getY() || z != this.destroyPos.getZ()) {
                        LOGGER.warn("Mismatch in destroy block pos: {} [{}, {}, {}]", this.destroyPos, x, y, z);
                        this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                        this.player.connection.send(
                                new PacketSCBlockBreakAck(this.destroyPos.asLong(), this.level.getBlockState_(this.destroyPos), action, true));
                    }
                    this.level.destroyBlockProgress(this.player.getId(), pos, -1);
                    this.player.connection.send(new PacketSCBlockBreakAck(pos, this.level.getBlockState_(x, y, z), action, true));
                }
            }
        }
    }

    @Overwrite
    private float incrementDestroyProgress(BlockState state, BlockPos pos, int destroyStart) {
        int delta = this.gameTicks - destroyStart;
        float progress = state.getDestroyProgress_(this.player, this.player.level, pos.getX(), pos.getY(), pos.getZ()) * (delta + 1);
        int k = (int) (progress * 10.0F);
        if (k != this.lastSentState) {
            this.level.destroyBlockProgress(this.player.getId(), pos.asLong(), k);
            this.lastSentState = k;
        }
        return progress;
    }

    @Shadow
    public abstract boolean isCreative();

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;" +
                                                                    "destroyPos:Lnet/minecraft/core/BlockPos;"))
    private void onInit0(ServerPlayerGameMode instance, BlockPos value) {
        this.destroyPos = new BlockPos.MutableBlockPos();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;" +
                                                                    "delayedDestroyPos:Lnet/minecraft/core/BlockPos;"))
    private void onInit1(ServerPlayerGameMode instance, BlockPos value) {
        this.delayedDestroyPos = new BlockPos.MutableBlockPos();
    }

    @Overwrite
    public void tick() {
        ++this.gameTicks;
        BlockState state;
        if (this.hasDelayedDestroy) {
            state = this.level.getBlockState_(this.delayedDestroyPos);
            if (state.isAir()) {
                this.hasDelayedDestroy = false;
            }
            else {
                float f = this.incrementDestroyProgress(state, this.delayedDestroyPos, this.delayedTickStart);
                if (f >= 1.0F) {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock_(this.delayedDestroyPos.getX(), this.delayedDestroyPos.getY(), this.delayedDestroyPos.getZ());
                }
            }
        }
        else if (this.isDestroyingBlock) {
            state = this.level.getBlockState_(this.destroyPos);
            if (state.isAir()) {
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos.asLong(), -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
            }
            else {
                this.incrementDestroyProgress(state, this.destroyPos, this.destroyProgressStart);
            }
        }
    }

    @Overwrite
    public InteractionResult useItemOn(ServerPlayer player,
                                       Level level,
                                       ItemStack stack,
                                       InteractionHand hand,
                                       BlockHitResult hitResult) {
        int x = hitResult.posX();
        int y = hitResult.posY();
        int z = hitResult.posZ();
        BlockState blockState = level.getBlockState_(x, y, z);
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider menuProvider = blockState.getMenuProvider(level, new BlockPos(x, y, z));
            if (menuProvider != null) {
                player.openMenu(menuProvider);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        boolean notEmptyHanded = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        boolean usingItem = player.isSecondaryUseActive() && notEmptyHanded;
        if (!usingItem) {
            InteractionResult result = blockState.use(level, player, hand, hitResult);
            if (result.consumesAction()) {
                //Here it was previously a copy of the stack, but is it necessary?
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger_(player, x, y, z, stack);
                return result;
            }
        }
        if (!stack.isEmpty() && !player.getCooldowns().isOnCooldown(stack.getItem())) {
            ItemStack copy = stack.copy();
            InteractionResult result;
            if (this.isCreative()) {
                int i = stack.getCount();
                result = stack.useOn_(player, hand, hitResult);
                stack.setCount(i);
            }
            else {
                result = stack.useOn_(player, hand, hitResult);
            }
            if (result.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger_(player, x, y, z, copy);
            }
            return result;
        }
        return InteractionResult.PASS;
    }
}
