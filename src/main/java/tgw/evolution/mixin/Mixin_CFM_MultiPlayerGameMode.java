package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.items.ItemUtils;
import tgw.evolution.network.PacketCSEntityInteraction;
import tgw.evolution.network.PacketCSPlayerAction;
import tgw.evolution.patches.PatchMultiPlayerGameMode;
import tgw.evolution.util.collection.L2OPair;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(MultiPlayerGameMode.class)
public abstract class Mixin_CFM_MultiPlayerGameMode implements PatchMultiPlayerGameMode {

    @Shadow @Final private static Logger LOGGER;
    @Unique private final Object2ObjectLinkedOpenHashMap<L2OPair<ServerboundPlayerActionPacket.Action>, Vec3> unAckedActions_;
    @Mutable @Shadow @Final @RestoreFinal private ClientPacketListener connection;
    @Unique private int creativeCooldownOff;
    @Shadow private BlockPos destroyBlockPos;
    @Shadow private int destroyDelay;
    @Shadow private float destroyProgress;
    @Shadow private float destroyTicks;
    @Shadow private ItemStack destroyingItem;
    @Shadow private boolean isDestroying;
    @Shadow private GameType localPlayerMode;
    @Mutable @Shadow @Final @RestoreFinal private Minecraft minecraft;
    @Shadow @Final @DeleteField private Object2ObjectLinkedOpenHashMap<Pair<BlockPos, ServerboundPlayerActionPacket.Action>, Vec3> unAckedActions;
    @Unique private boolean wasLastOnBlock;

    @ModifyConstructor
    public Mixin_CFM_MultiPlayerGameMode(Minecraft mc, ClientPacketListener listener) {
        this.destroyBlockPos = new BlockPos.MutableBlockPos();
        this.destroyingItem = ItemStack.EMPTY;
        this.localPlayerMode = GameType.DEFAULT_MODE;
        this.unAckedActions_ = new Object2ObjectLinkedOpenHashMap();
        this.minecraft = mc;
        this.connection = listener;
    }

    /**
     * @author TheGreatWolf
     * @reason Fix destroy texture
     */
    @Overwrite
    public boolean continueDestroyBlock(BlockPos pos, Direction face) {
        Evolution.deprecatedMethod();
        return false;
    }

    @Override
    public boolean continueDestroyBlock_(int x, int y, int z, Direction face, BlockHitResult hitResult) {
        this.ensureHasSentCarriedItem();
        if (this.destroyDelay > 0) {
            --this.destroyDelay;
            return true;
        }
        assert this.minecraft.level != null;
        if (this.localPlayerMode.isCreative() && this.minecraft.level.getWorldBorder().isWithinBounds_(x, z)) {
            this.destroyDelay = 5 - this.creativeCooldownOff;
            if (this.creativeCooldownOff < 5) {
                ++this.creativeCooldownOff;
            }
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, x, y, z, face,
                                 hitResult.x(), hitResult.y(), hitResult.z());
            this.destroyBlock_(x, y, z);
            return true;
        }
        this.creativeCooldownOff = 0;
        if (this.sameDestroyTarget(x, y, z)) {
            BlockState state = this.minecraft.level.getBlockState_(x, y, z);
            if (state.isAir()) {
                this.isDestroying = false;
                return false;
            }
            assert this.minecraft.player != null;
            this.destroyProgress += state.getDestroyProgress_(this.minecraft.player, this.minecraft.player.level, x, y, z);
            if (this.destroyTicks % 4.0F == 0.0F) {
                SoundType soundtype = state.getSoundType();
                this.minecraft.getSoundManager()
                              .play(new SimpleSoundInstance(soundtype.getHitSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F,
                                                            soundtype.getPitch() * 0.5F, x + 0.5, y + 0.5, z + 0.5));
            }
            ++this.destroyTicks;
            if (this.destroyProgress >= 1.0F) {
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, x, y, z, face);
                this.destroyBlock_(x, y, z);
                this.destroyProgress = 0;
                this.destroyTicks = 0;
                this.destroyDelay = 5;
            }
            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos.asLong(), this.getBlockBreakingProgress());
            return true;
        }
        return this.startDestroyBlock_(hitResult);
    }

    @Overwrite
    public boolean destroyBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.destroyBlock_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean destroyBlock_(int x, int y, int z) {
        Level level = this.minecraft.level;
        assert this.minecraft.player != null;
        assert level != null;
        if (this.minecraft.player.blockActionRestricted_(level, x, y, z, this.localPlayerMode)) {
            return false;
        }
        BlockState state = level.getBlockState_(x, y, z);
        if (!this.minecraft.player.getMainHandItem().getItem().canAttackBlock_(state, level, x, y, z, this.minecraft.player)) {
            return false;
        }
        Block block = state.getBlock();
        if (block instanceof GameMasterBlock && !this.minecraft.player.canUseGameMasterBlocks()) {
            return false;
        }
        if (state.isAir()) {
            return false;
        }
        block.playerWillDestroy_(level, x, y, z, state, this.minecraft.player);
        FluidState fluidState = level.getFluidState_(x, y, z);
        boolean set = level.setBlock_(x, y, z, fluidState.createLegacyBlock(),
                                      BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD);
        if (set) {
            block.destroy_(level, x, y, z, state);
        }
        return set;
    }

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    private int getBlockBreakingProgress() {
        if (this.destroyProgress == 0) {
            return -1;
        }
        return Mth.floor(this.destroyProgress * 10.0f);
    }

    @Overwrite
    public void handleBlockBreakAck(ClientLevel level, BlockPos pos, BlockState state, ServerboundPlayerActionPacket.Action action, boolean bl) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void handleBlockBreakAck_(ClientLevel level,
                                     long pos, BlockState state,
                                     ServerboundPlayerActionPacket.Action action,
                                     boolean allGood) {
        Vec3 position = this.unAckedActions_.remove(new L2OPair<>(pos, action));
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        BlockState stateAtPos = level.getBlockState_(x, y, z);
        if ((position == null || !allGood || action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && stateAtPos != state) &&
            stateAtPos != state) {
            level.setKnownState_(x, y, z, state);
            Player player = this.minecraft.player;
            if (position != null) {
                assert player != null;
                if (level == player.level && player.isColliding_(x, y, z, state)) {
                    player.absMoveTo(position.x, position.y, position.z);
                }
            }
        }
        while (this.unAckedActions_.size() >= 50) {
            L2OPair<ServerboundPlayerActionPacket.Action> pair = this.unAckedActions_.firstKey();
            this.unAckedActions_.removeFirst();
            LOGGER.error("Too many unacked block actions, dropping {}, {}", pair.l, pair.r);
        }
    }

    @Overwrite
    public InteractionResult interactAt(Player player, Entity entity, EntityHitResult entityHitResult, InteractionHand hand) {
        this.ensureHasSentCarriedItem();
        Vec3 position = entity.position();
        double x = entityHitResult.x() - position.x;
        double y = entityHitResult.y() - position.y;
        double z = entityHitResult.z() - position.z;
        this.connection.send(new PacketCSEntityInteraction(entity, player.isShiftKeyDown(), hand, x, y, z));
        return this.localPlayerMode == GameType.SPECTATOR ? InteractionResult.PASS : entity.interactAt_(player, x, y, z, hand);
    }

    @Overwrite
    @DeleteMethod
    private boolean sameDestroyTarget(BlockPos pos) {
        throw new AbstractMethodError();
    }

    private boolean sameDestroyTarget(int x, int y, int z) {
        assert this.minecraft.player != null;
        return x == this.destroyBlockPos.getX() &&
               y == this.destroyBlockPos.getY() &&
               z == this.destroyBlockPos.getZ() &&
               ItemUtils.isSameIgnoreCount(this.minecraft.player.getMainHandItem(), this.destroyingItem);
    }

    @Overwrite
    @DeleteMethod
    private void sendBlockAction(ServerboundPlayerActionPacket.Action action, BlockPos blockPos, Direction direction) {
        throw new AbstractMethodError();
    }

    @Unique
    private void sendBlockAction(ServerboundPlayerActionPacket.Action action,
                                 int x,
                                 int y,
                                 int z,
                                 Direction direction,
                                 double hitX,
                                 double hitY,
                                 double hitZ) {
        LocalPlayer player = this.minecraft.player;
        assert player != null;
        long pos = BlockPos.asLong(x, y, z);
        this.unAckedActions_.put(new L2OPair<>(pos, action), player.position());
        this.connection.send(new PacketCSPlayerAction(action, pos, direction, hitX, hitY, hitZ));
    }

    @Unique
    private void sendBlockAction(ServerboundPlayerActionPacket.Action action, int x, int y, int z, Direction direction) {
        this.sendBlockAction(action, x, y, z, direction, Double.NaN, Double.NaN, Double.NaN);
    }

    /**
     * @author TheGreatWolf
     * @reason Fix destroy texture.
     */
    @Overwrite
    public boolean startDestroyBlock(BlockPos pos, Direction face) {
        Evolution.deprecatedMethod();
        return false;
    }

    @Override
    public boolean startDestroyBlock_(BlockHitResult hitResult) {
        assert this.minecraft.player != null;
        assert this.minecraft.level != null;
        int x = hitResult.posX();
        int y = hitResult.posY();
        int z = hitResult.posZ();
        Direction face = hitResult.getDirection();
        this.creativeCooldownOff = 0;
        if (this.minecraft.player.blockActionRestricted_(this.minecraft.level, x, y, z, this.localPlayerMode)) {
            return false;
        }
        if (!this.minecraft.level.getWorldBorder().isWithinBounds_(x, z)) {
            return false;
        }
        if (this.localPlayerMode.isCreative()) {
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, x, y, z, face,
                                 hitResult.x(), hitResult.y(), hitResult.z());
            this.destroyBlock_(x, y, z);
            this.destroyDelay = 5;
        }
        else if (!this.isDestroying || !this.sameDestroyTarget(x, y, z)) {
            if (this.isDestroying) {
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                                     this.destroyBlockPos.getX(), this.destroyBlockPos.getY(), this.destroyBlockPos.getZ(), face);
                this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos.asLong(), -1);
            }
            ((BlockPos.MutableBlockPos) this.destroyBlockPos).set(x, y, z);
            BlockState state = this.minecraft.level.getBlockState_(x, y, z);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, x, y, z, face,
                                 hitResult.x(), hitResult.y(), hitResult.z());
            boolean notAir = !state.isAir();
            if (notAir && this.destroyProgress == 0.0F) {
                state.attack_(this.minecraft.level, x, y, z, face, hitResult.x(), hitResult.y(), hitResult.z(), this.minecraft.player);
            }
            if (notAir && state.getDestroyProgress_(this.minecraft.player, this.minecraft.player.level, x, y, z) >= 1.0F) {
                this.destroyBlock_(x, y, z);
                this.destroyProgress = 0;
                this.destroyTicks = 0.0F;
            }
            else {
                this.isDestroying = true;
                this.destroyingItem = this.minecraft.player.getMainHandItem();
                this.destroyProgress = 0;
                this.destroyTicks = 0.0F;
                this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(),
                                                          this.destroyBlockPos.asLong(),
                                                          this.getBlockBreakingProgress());
            }
        }
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason Use patched methods.
     */
    @Overwrite
    public void stopDestroyBlock() {
        if (this.isDestroying) {
            assert this.minecraft.level != null;
            assert this.minecraft.player != null;
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                                 this.destroyBlockPos.getX(), this.destroyBlockPos.getY(), this.destroyBlockPos.getZ(), Direction.DOWN);
            this.isDestroying = false;
            this.destroyProgress = 0.0F;
            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos.asLong(), -1);
            this.minecraft.player.resetAttackStrengthTicker();
        }
    }

    @Overwrite
    public InteractionResult useItemOn(LocalPlayer player, ClientLevel level, InteractionHand hand, BlockHitResult hitResult) {
        this.wasLastOnBlock = false;
        this.ensureHasSentCarriedItem();
        int x = hitResult.posX();
        int z = hitResult.posZ();
        if (!level.getWorldBorder().isWithinBounds_(x, z)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (this.localPlayerMode == GameType.SPECTATOR) {
            this.connection.send(new ServerboundUseItemOnPacket(hand, hitResult));
            return InteractionResult.SUCCESS;
        }
        boolean notEmptyHanded = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        boolean usingItem = player.isSecondaryUseActive() && notEmptyHanded;
        InteractionResult interactionResult;
        if (!usingItem) {
            interactionResult = level.getBlockState_(x, hitResult.posY(), z).use(level, player, hand, hitResult);
            if (interactionResult.consumesAction()) {
                this.connection.send(new ServerboundUseItemOnPacket(hand, hitResult));
                this.wasLastOnBlock = true;
                return interactionResult;
            }
        }
        if (!stack.isEmpty() && !player.getCooldowns().isOnCooldown(stack.getItem())) {
            if (this.localPlayerMode.isCreative()) {
                int oldCount = stack.getCount();
                interactionResult = stack.useOn_(player, hand, hitResult);
                stack.setCount(oldCount);
            }
            else {
                interactionResult = stack.useOn_(player, hand, hitResult);
            }
            if (interactionResult.consumesAction()) {
                this.connection.send(new ServerboundUseItemOnPacket(hand, hitResult));
                return interactionResult;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean wasLastInteractionUsedOnBlock() {
        return this.wasLastOnBlock;
    }
}
