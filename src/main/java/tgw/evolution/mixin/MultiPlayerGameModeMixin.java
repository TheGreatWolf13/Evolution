package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.items.ItemUtils;
import tgw.evolution.patches.ILevelPatch;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Unique private int creativeCooldownOff;
    @Shadow private BlockPos destroyBlockPos;
    @Shadow private int destroyDelay;
    @Shadow private float destroyProgress;
    @Shadow private float destroyTicks;
    @Shadow private ItemStack destroyingItem;
    @Shadow private boolean isDestroying;
    @Shadow private GameType localPlayerMode;
    @Shadow @Final private Minecraft minecraft;

    /**
     * @author TheGreatWolf
     * @reason Fix destroy texture
     */
    @Overwrite
    public boolean continueDestroyBlock(BlockPos pos, Direction face) {
        this.ensureHasSentCarriedItem();
        if (this.destroyDelay > 0) {
            --this.destroyDelay;
            return true;
        }
        assert this.minecraft.level != null;
        if (this.localPlayerMode.isCreative() && this.minecraft.level.getWorldBorder().isWithinBounds(pos)) {
            this.destroyDelay = 5 - this.creativeCooldownOff;
            if (this.creativeCooldownOff < 5) {
                ++this.creativeCooldownOff;
            }
            BlockState state = this.minecraft.level.getBlockState(pos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pos, state, 1.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, face);
            this.destroyBlock(pos);
            return true;
        }
        this.creativeCooldownOff = 0;
        if (this.sameDestroyTarget(pos)) {
            BlockState state = this.minecraft.level.getBlockState(pos);
            if (state.isAir()) {
                this.isDestroying = false;
                return false;
            }
            assert this.minecraft.player != null;
            this.destroyProgress += state.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, pos);
            if (this.destroyTicks % 4.0F == 0.0F) {
                SoundType soundtype = state.getSoundType(this.minecraft.level, pos, this.minecraft.player);
                this.minecraft.getSoundManager()
                              .play(new SimpleSoundInstance(soundtype.getHitSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F,
                                                            soundtype.getPitch() * 0.5F, pos));
            }
            ++this.destroyTicks;
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pos, state, Mth.clamp(this.destroyProgress, 0.0F, 1.0F));
            if (this.destroyProgress >= 1.0F) {
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, face);
                this.destroyBlock(pos);
                this.destroyProgress = 0;
                this.destroyTicks = 0;
                this.destroyDelay = 5;
            }
            ((ILevelPatch) this.minecraft.level).destroyBlockProgress(this.minecraft.player.getId(),
                                                                      this.destroyBlockPos.asLong(),
                                                                      this.getBlockBreakingProgress());
            return true;
        }
        return this.startDestroyBlock(pos, face);
    }

    @Shadow
    public abstract boolean destroyBlock(BlockPos pPos);

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    private int getBlockBreakingProgress() {
        if (this.destroyProgress == 0) {
            return -1;
        }
        return Mth.floor(this.destroyProgress * 10.0f);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;" +
                                                                    "destroyBlockPos:Lnet/minecraft/core/BlockPos;", opcode = Opcodes.PUTFIELD))
    private void onInit(MultiPlayerGameMode instance, BlockPos value) {
        this.destroyBlockPos = new BlockPos.MutableBlockPos();
    }

    /**
     * @author TheGreatWolf
     * @reason Fix destroy texture
     */
    @Overwrite
    private boolean sameDestroyTarget(BlockPos pos) {
        assert this.minecraft.player != null;
        return pos.equals(this.destroyBlockPos) && ItemUtils.isSameIgnoreCount(this.minecraft.player.getMainHandItem(), this.destroyingItem);
    }

    @Shadow
    protected abstract void sendBlockAction(ServerboundPlayerActionPacket.Action pAction, BlockPos pPos, Direction pDir);

    /**
     * @author TheGreatWolf
     * @reason Fix destroy texture.
     */
    @Overwrite
    public boolean startDestroyBlock(BlockPos pos, Direction face) {
        assert this.minecraft.player != null;
        assert this.minecraft.level != null;
        this.creativeCooldownOff = 0;
        if (this.minecraft.player.blockActionRestricted(this.minecraft.level, pos, this.localPlayerMode)) {
            return false;
        }
        if (!this.minecraft.level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (this.localPlayerMode.isCreative()) {
            BlockState blockstate = this.minecraft.level.getBlockState(pos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pos, blockstate, 1.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, face);
            this.destroyBlock(pos);
            this.destroyDelay = 5;
        }
        else if (!this.isDestroying || !this.sameDestroyTarget(pos)) {
            if (this.isDestroying) {
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, face);
                ((ILevelPatch) this.minecraft.level).destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos.asLong(), -1);
            }
            ((BlockPos.MutableBlockPos) this.destroyBlockPos).set(pos);
            BlockState state = this.minecraft.level.getBlockState(pos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pos, state, 0.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, face);
            boolean notAir = !state.isAir();
            if (notAir && this.destroyProgress == 0.0F) {
                state.attack(this.minecraft.level, pos, this.minecraft.player);
            }
            if (notAir && state.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, pos) >= 1.0F) {
                this.destroyBlock(pos);
                this.destroyProgress = 0;
                this.destroyTicks = 0.0F;
            }
            else {
                this.isDestroying = true;
                this.destroyingItem = this.minecraft.player.getMainHandItem();
                this.destroyProgress = 0;
                this.destroyTicks = 0.0F;
                ((ILevelPatch) this.minecraft.level).destroyBlockProgress(this.minecraft.player.getId(),
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
            BlockState blockstate = this.minecraft.level.getBlockState(this.destroyBlockPos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, this.destroyBlockPos, blockstate, -1.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN);
            this.isDestroying = false;
            this.destroyProgress = 0.0F;
            ((ILevelPatch) this.minecraft.level).destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos.asLong(), -1);
            this.minecraft.player.resetAttackStrengthTicker();
        }
    }
}
