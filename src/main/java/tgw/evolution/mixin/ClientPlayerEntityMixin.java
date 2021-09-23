package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.Pose;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.items.IEvolutionItem;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    @Final
    public ClientPlayNetHandler connection;
    @Shadow
    public MovementInput input;
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

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to fix some issues with sprinting conditions.
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
        this.crouching = !this.abilities.flying &&
                         !this.isSwimming() &&
                         this.canEnterPose(Pose.CROUCHING) &&
                         (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
        this.input.tick(this.isMovingSlowly());
        ForgeHooksClient.onInputUpdate(this, this.input);
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
        //TODO use my stats
        boolean hasFoodToRun = this.getFoodData().getFoodLevel() > 6.0F || this.abilities.mayfly;
        boolean effectsAllowSprinting = this.effectsAllowSprinting();
        boolean itemsAllowSprinting = this.itemsAllowSprinting();
        if ((this.onGround || this.isUnderWater()) &&
            !isSneaking &&
            !hasImpulseToStartSprint &&
            !this.isSprinting() &&
            this.hasEnoughImpulseToStartSprinting() &&
            hasFoodToRun &&
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
            hasFoodToRun &&
            effectsAllowSprinting &&
            itemsAllowSprinting &&
            this.minecraft.options.keySprint.isDown()) {
            this.setSprinting(true);
        }
        if (this.isSprinting()) {
            boolean cannotBeSwimming = !this.input.hasForwardImpulse() || !hasFoodToRun || !effectsAllowSprinting || !itemsAllowSprinting;
            boolean cannotBeRunning = cannotBeSwimming || this.horizontalCollision || this.isInWater() && !this.isUnderWater();
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
        if (this.abilities.mayfly) {
            if (this.minecraft.gameMode.isAlwaysFlying()) {
                if (!this.abilities.flying) {
                    this.abilities.flying = true;
                    isFlying = true;
                    this.onUpdateAbilities();
                }
            }
            else if (!isJumping && this.input.jumping) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                }
                else if (!this.isSwimming()) {
                    this.abilities.flying = !this.abilities.flying;
                    isFlying = true;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }
        if (this.input.jumping && !isFlying && !isJumping && !this.abilities.flying && !this.isPassenger() && !this.onClimbable()) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.CHEST);
            if (itemstack.canElytraFly(this) && this.tryToStartFallFlying()) {
                this.connection.send(new CEntityActionPacket(this, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }
        this.wasFallFlying = this.isFallFlying();
        if (this.isInWater() && this.input.shiftKeyDown && this.isAffectedByFluids()) {
            this.goDownInWater();
        }
        if (this.isEyeInFluid(FluidTags.WATER)) {
            int i = this.isSpectator() ? 10 : 1;
            this.waterVisionTime = MathHelper.clamp(this.waterVisionTime + i, 0, 600);
        }
        else if (this.waterVisionTime > 0) {
            this.isEyeInFluid(FluidTags.WATER);
            this.waterVisionTime = MathHelper.clamp(this.waterVisionTime - 10, 0, 600);
        }
        if (this.abilities.flying && this.isControlledCamera()) {
            int j = 0;
            if (this.input.shiftKeyDown) {
                j--;
            }
            if (this.input.jumping) {
                j++;
            }
            if (j != 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, j * this.abilities.getFlyingSpeed() * 3.0F, 0));
            }
        }
        if (this.isRidingJumpable()) {
            IJumpingMount jumpingMount = (IJumpingMount) this.getVehicle();
            if (this.jumpRidingTicks < 0) {
                this.jumpRidingTicks++;
                if (this.jumpRidingTicks == 0) {
                    this.jumpRidingScale = 0.0F;
                }
            }
            if (isJumping && !this.input.jumping) {
                this.jumpRidingTicks = -10;
                jumpingMount.onPlayerJump(MathHelper.floor(this.getJumpRidingScale() * 100.0F));
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
        if (this.onGround && this.abilities.flying && !this.minecraft.gameMode.isAlwaysFlying()) {
            this.abilities.flying = false;
            this.onUpdateAbilities();
        }
    }

    private boolean effectsAllowSprinting() {
        if (this.hasEffect(EvolutionEffects.DEHYDRATION.get())) {
            return false;
        }
        return !this.hasEffect(Effects.BLINDNESS);
    }

    @Shadow
    public abstract float getJumpRidingScale();

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle first person camera.
     */
    @Overwrite
    @Override
    public float getViewXRot(float partialTicks) {
        return super.getViewXRot(partialTicks);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle first person camera.
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

    @Shadow
    protected abstract boolean isControlledCamera();

    /**
     * @author MGSchultz
     * <p>
     * Replace to fix inconsist render between client and server.
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
            if (item instanceof IEvolutionItem) {
                return !((IEvolutionItem) item).useItemPreventsSprinting();
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
     * @author MGSchultz
     * <p>
     * Overwrite to fix inconsistency with jumping.
     */
    @Override
    @Overwrite
    public void serverAiStep() {
        super.serverAiStep();
        if (this.isControlledCamera()) {
            this.xxa = this.input.leftImpulse;
            this.zza = this.input.forwardImpulse;
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
            this.xBob += (this.xRot - this.xBob) * 0.5;
            this.yBob += (this.yRot - this.yBob) * 0.5;
        }
    }
}
