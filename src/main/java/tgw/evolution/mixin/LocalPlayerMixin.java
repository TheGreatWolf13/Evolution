package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.util.EvolutionInput;
import tgw.evolution.items.IEvolutionItem;
import tgw.evolution.patches.IPlayerPatch;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements IPlayerPatch {

    @Shadow
    @Final
    public ClientPacketListener connection;
    @Shadow
    public Input input;
    @Shadow
    public int sprintTime;
    @Shadow
    public float xBob;
    @Shadow
    public float xBobO;
    @Shadow
    public float yBob;
    @Shadow
    public float yBobO;
    @Shadow
    @Final
    protected Minecraft minecraft;
    @Shadow
    protected int sprintTriggerTime;
    @Shadow
    private boolean crouching;
    @Shadow
    private float jumpRidingScale;
    @Shadow
    private int jumpRidingTicks;
    @Shadow
    private boolean wasFallFlying;
    @Shadow
    private int waterVisionTime;

    public LocalPlayerMixin(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to fix some issues with sprinting conditions.
     */
    @Override
    @Overwrite
    public void aiStep() {
        this.sprintTime++;
        if (this.sprintTriggerTime > 0) {
            this.sprintTriggerTime--;
        }
        this.handleNetherPortalClient();
        boolean isJumping = this.input.jumping;
        boolean isSneaking = this.input.shiftKeyDown;
        boolean hasImpulseToStartSprint = this.hasEnoughImpulseToStartSprinting();
        this.crouching = !this.getAbilities().flying &&
                         !this.isSwimming() &&
                         this.canEnterPose(Pose.CROUCHING) &&
                         (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
        ((EvolutionInput) this.input).tick(this);
        ForgeHooksClient.onMovementInputUpdate(this, this.input);
        this.minecraft.getTutorial().onInput(this.input);
        if (!this.noPhysics) {
            this.moveTowardsClosestSpace(this.getX() - this.getBbWidth() * 0.35, this.getZ() + this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() - this.getBbWidth() * 0.35, this.getZ() - this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + this.getBbWidth() * 0.35, this.getZ() - this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + this.getBbWidth() * 0.35, this.getZ() + this.getBbWidth() * 0.35);
        }
        if (isSneaking) {
            this.sprintTriggerTime = 0;
        }
        boolean effectsAllowSprinting = this.getEffectHelper().canSprint();
        boolean itemsAllowSprinting = this.itemsAllowSprinting();
        if ((this.onGround || this.isUnderWater()) &&
            !isSneaking &&
            !hasImpulseToStartSprint &&
            !this.isSprinting() &&
            this.hasEnoughImpulseToStartSprinting() &&
            effectsAllowSprinting &&
            itemsAllowSprinting) {
            if (this.sprintTriggerTime <= 0 && !this.minecraft.options.keySprint.isDown()) {
                this.sprintTriggerTime = 7;
            }
            else {
                this.setSprinting(true);
            }
        }
        if (!this.isSprinting() &&
            (!this.isInWater() || this.isUnderWater()) &&
            this.hasEnoughImpulseToStartSprinting() &&
            effectsAllowSprinting &&
            itemsAllowSprinting &&
            this.minecraft.options.keySprint.isDown()) {
            this.setSprinting(true);
        }
        if (this.isSprinting()) {
            boolean cannotBeSwimming = !this.input.hasForwardImpulse() || !effectsAllowSprinting || !itemsAllowSprinting;
            boolean cannotBeRunning = cannotBeSwimming ||
                                      this.horizontalCollision && !this.minorHorizontalCollision ||
                                      this.isInWater() && !this.isUnderWater();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.shiftKeyDown && cannotBeSwimming || !this.isInWater()) {
                    this.setSprinting(false);
                }
            }
            else if (cannotBeRunning) {
                this.setSprinting(false);
            }
        }
        boolean isFlying = false;
        if (this.getAbilities().mayfly) {
            assert this.minecraft.gameMode != null;
            if (this.minecraft.gameMode.isAlwaysFlying()) {
                if (!this.getAbilities().flying) {
                    this.getAbilities().flying = true;
                    isFlying = true;
                    this.onUpdateAbilities();
                }
            }
            else if (!isJumping && this.input.jumping) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                }
                else if (!this.isSwimming()) {
                    this.getAbilities().flying = !this.getAbilities().flying;
                    isFlying = true;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }
        if (this.input.jumping && !isFlying && !isJumping && !this.getAbilities().flying && !this.isPassenger() && !this.onClimbable()) {
            ItemStack chestStack = this.getItemBySlot(EquipmentSlot.CHEST);
            if (chestStack.canElytraFly(this) && this.tryToStartFallFlying()) {
                this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        }
        this.wasFallFlying = this.isFallFlying();
        if (this.isInWater() && this.input.shiftKeyDown && this.isAffectedByFluids()) {
            this.goDownInWater();
        }
        if (this.isEyeInFluid(FluidTags.WATER)) {
            int i = this.isSpectator() ? 10 : 1;
            this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
        }
        else if (this.waterVisionTime > 0) {
            this.isEyeInFluid(FluidTags.WATER);
            this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
        }
        if (this.getAbilities().flying && this.isControlledCamera()) {
            int j = 0;
            if (this.input.shiftKeyDown) {
                j--;
            }
            if (this.input.jumping) {
                j++;
            }
            if (j != 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, j * this.getAbilities().getFlyingSpeed() * 3.0F, 0));
            }
        }
        if (this.isRidingJumpable()) {
            PlayerRideableJumping jumpingMount = (PlayerRideableJumping) this.getVehicle();
            if (this.jumpRidingTicks < 0) {
                this.jumpRidingTicks++;
                if (this.jumpRidingTicks == 0) {
                    this.jumpRidingScale = 0.0F;
                }
            }
            if (isJumping && !this.input.jumping) {
                this.jumpRidingTicks = -10;
                assert jumpingMount != null;
                jumpingMount.onPlayerJump(Mth.floor(this.getJumpRidingScale() * 100.0F));
                this.sendRidingJump();
            }
            else if (!isJumping && this.input.jumping) {
                this.jumpRidingTicks = 0;
                this.jumpRidingScale = 0.0F;
            }
            else if (isJumping) {
                this.jumpRidingTicks++;
                if (this.jumpRidingTicks < 10) {
                    this.jumpRidingScale = this.jumpRidingTicks * 0.1F;
                }
                else {
                    this.jumpRidingScale = 0.8F + 2.0F / (this.jumpRidingTicks - 9) * 0.1F;
                }
            }
        }
        else {
            this.jumpRidingScale = 0.0F;
        }
        super.aiStep();
        if (this.onGround && this.getAbilities().flying) {
            assert this.minecraft.gameMode != null;
            if (!this.minecraft.gameMode.isAlwaysFlying()) {
                this.getAbilities().flying = false;
                this.onUpdateAbilities();
            }
        }
    }

    @Shadow
    public abstract float getJumpRidingScale();

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle first person camera.
     */
    @Overwrite
    @Override
    public float getViewXRot(float partialTicks) {
        return super.getViewXRot(partialTicks);
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle first person camera.
     */
    @Overwrite
    @Override
    public float getViewYRot(float partialTicks) {
        return super.getViewYRot(partialTicks);
    }

    @Shadow
    protected abstract void handleNetherPortalClient();

    @Shadow
    protected abstract boolean hasEnoughImpulseToStartSprinting();

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle the possibility of second person camera.
     */
    @Overwrite
    protected boolean isControlledCamera() {
        return this.minecraft.getCameraEntity() == this || !this.isSpectator();
    }

    /**
     * @author TheGreatWolf
     * @reason Replace to fix inconsist render between client and server.
     */
    @Override
    @Overwrite
    public boolean isCrouching() {
        return this.getPose() == Pose.CROUCHING;
    }

    @Shadow
    public abstract boolean isMovingSlowly();

    @Shadow
    public abstract boolean isRidingJumpable();

    @Override
    @Shadow
    public abstract boolean isShiftKeyDown();

    @Override
    @Shadow
    public abstract boolean isUnderWater();

    @Override
    @Shadow
    public abstract boolean isUsingItem();

    private boolean itemsAllowSprinting() {
        if (this.isUsingItem()) {
            Item item = this.getUseItem().getItem();
            if (item instanceof IEvolutionItem evItem) {
                return !evItem.useItemPreventsSprinting();
            }
        }
        return true;
    }

    @Shadow
    protected abstract void moveTowardsClosestSpace(double p_244389_1_, double p_244389_3_);

    @Override
    @Shadow
    public abstract void onUpdateAbilities();

    @Shadow
    protected abstract void sendRidingJump();

    /**
     * @author TheGreatWolf
     * @reason Overwrite to fix inconsistency with jumping.
     */
    @Override
    @Overwrite
    public void serverAiStep() {
        super.serverAiStep();
        if (this.isControlledCamera()) {
            if (!this.isLongitudinalMotionLocked()) {
                this.zza = this.input.forwardImpulse;
            }
            else {
                this.zza = 0;
            }
            if (!this.isLateralMotionLocked()) {
                this.xxa = this.input.leftImpulse;
            }
            else {
                this.xxa = 0;
            }
            if (!this.jumping) {
                this.jumping = this.input.jumping;
            }
            else {
                if (this.onGround || this.onClimbable() || this.isInWater()) {
                    this.jumping = this.input.jumping;
                }
            }
            this.yBobO = this.yBob;
            this.xBobO = this.xBob;
            this.xBob += (this.getXRot() - this.xBob) * 0.5;
            this.yBob += (this.getYRot() - this.yBob) * 0.5;
        }
    }
}
