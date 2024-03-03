package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.util.EvolutionInput;
import tgw.evolution.items.IEvolutionItem;
import tgw.evolution.network.PacketCSSkinType;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.world.util.LevelUtils;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    @Shadow @Final public ClientPacketListener connection;
    @Shadow public Input input;
    @Shadow public int sprintTime;
    @Shadow public float xBob;
    @Shadow public float xBobO;
    @Shadow public float yBob;
    @Shadow public float yBobO;
    @Shadow @Final protected Minecraft minecraft;
    @Shadow protected int sprintTriggerTime;
    @Shadow private boolean crouching;
    @Shadow private float jumpRidingScale;
    @Shadow private int jumpRidingTicks;
    @Shadow private boolean wasFallFlying;
    @Shadow private int waterVisionTime;

    public MixinLocalPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to fix some issues with sprinting conditions.
     */
    @Override
    @Overwrite
    public void aiStep() {
        assert this.minecraft.gameMode != null;
        this.sprintTime++;
        if (this.sprintTriggerTime > 0) {
            this.sprintTriggerTime--;
        }
        this.handleNetherPortalClient();
        boolean isJumping = this.input.jumping;
        boolean isSneaking = this.input.shiftKeyDown;
        boolean hadImpulseToStartSprint = this.hasEnoughImpulseToStartSprinting();
        Abilities abilities = this.getAbilities();
        this.crouching = !abilities.flying && !this.isSwimming() && this.canEnterPose(Pose.CROUCHING) && (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
        ((EvolutionInput) this.input).tick((LocalPlayer) (Object) this);
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
        boolean startedToSprint = false;
        //Try to start sprinting by double tapping W
        if ((this.onGround ||
             abilities.flying ||
             this.isUnderWater() ||
             this.isInWater() && this.getFluidHeight(FluidTags.WATER) >= 0.5 * this.getBbHeight()) &&
            !isSneaking &&
            !hadImpulseToStartSprint &&
            !this.isSprinting() &&
            this.hasEnoughImpulseToStartSprinting() &&
            effectsAllowSprinting &&
            itemsAllowSprinting) {
            if (this.sprintTriggerTime <= 0 && !this.minecraft.options.keySprint.isDown()) {
                this.sprintTriggerTime = 7;
            }
            else {
                this.setSprinting(true);
                startedToSprint = true;
            }
        }
        //Try to start sprinting by pressing Ctrl
        if (!this.isSprinting() &&
            (this.onGround ||
             abilities.flying ||
             this.isUnderWater() ||
             this.isInWater() && this.getFluidHeight(FluidTags.WATER) >= 0.5 * this.getBbHeight()) &&
            !isSneaking &&
            this.hasEnoughImpulseToStartSprinting() &&
            effectsAllowSprinting &&
            itemsAllowSprinting &&
            this.minecraft.options.keySprint.isDown()) {
            this.setSprinting(true);
            startedToSprint = true;
        }
        //Try to cancel sprinting
        if (!startedToSprint && this.isSprinting()) {
            boolean cannotBeSwimming = isSneaking || !effectsAllowSprinting || !itemsAllowSprinting;
            if (this.isSwimming()) {
                if (!this.onGround && cannotBeSwimming || !this.isInWater()) {
                    this.setSprinting(false);
                }
            }
            else if (cannotBeSwimming ||
                     this.horizontalCollision && !this.minorHorizontalCollision ||
                     this.isInWater() && !this.isUnderWater() ||
                     !this.input.hasForwardImpulse()) {
                this.setSprinting(false);
            }
        }
        if (abilities.mayfly) {
            if (this.minecraft.gameMode.isAlwaysFlying()) {
                if (!abilities.flying) {
                    this.onUpdateAbilities();
                }
            }
            else if (!isJumping && this.input.jumping) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                }
                else if (!this.isSwimming()) {
                    abilities.flying = !abilities.flying;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }
        if (this.input.jumping && !abilities.flying && !isJumping && !this.isPassenger() && !this.onClimbable()) {
            ItemStack chestStack = this.getItemBySlot(EquipmentSlot.CHEST);
            if (chestStack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(chestStack) && this.tryToStartFallFlying()) {
                this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        }
        this.wasFallFlying = this.isFallFlying();
        if (this.isEyeInFluid(FluidTags.WATER)) {
            int i = this.isSpectator() ? 10 : 1;
            this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
        }
        else if (this.waterVisionTime > 0) {
            this.isEyeInFluid(FluidTags.WATER);
            this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
        }
        if (abilities.flying && this.isControlledCamera()) {
            int j = 0;
            if (isSneaking) {
                j--;
            }
            if (this.input.jumping) {
                j++;
            }
            if (j != 0) {
                Vec3 velocity = this.getDeltaMovement();
                this.setDeltaMovement(velocity.x, velocity.y + abilities.getFlyingSpeed() * 3.0F * j, velocity.z);
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
        if (abilities.flying && this.onGround && this.input.shiftKeyDown) {
            abilities.flying = false;
            this.onUpdateAbilities();
        }
        super.aiStep();
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private boolean hasEnoughImpulseToStartSprinting() {
        if (this.horizontalCollision && !this.minorHorizontalCollision) {
            return false;
        }
        return this.input.hasForwardImpulse();
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle the possibility of second person camera.
     */
    @Overwrite
    public boolean isControlledCamera() {
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

    @Unique
    private boolean itemsAllowSprinting() {
        if (this.isUsingItem()) {
            Item item = this.getUseItem().getItem();
            if (item instanceof IEvolutionItem evItem) {
                return !evItem.useItemPreventsSprinting();
            }
        }
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible.
     */
    @Overwrite
    private void moveTowardsClosestSpace(double x, double z) {
        int posX = Mth.floor(x);
        int posZ = Mth.floor(z);
        if (this.suffocatesAt(posX, posZ)) {
            double dx = x - posX;
            double dz = z - posZ;
            Direction chosenDir = null;
            double minDist = Double.MAX_VALUE;
            for (Direction dir : DirectionUtil.HORIZ_WENS) {
                double delta = dir.getAxis() == Direction.Axis.X ? dx : dz;
                double dist = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 - delta : delta;
                if (dist < minDist && !this.suffocatesAt(posX + dir.getStepX(), posZ + dir.getStepZ())) {
                    minDist = dist;
                    chosenDir = dir;
                }
            }
            if (chosenDir != null) {
                Vec3 velocity = this.getDeltaMovement();
                if (chosenDir.getAxis() == Direction.Axis.X) {
                    this.setDeltaMovement(0.1 * chosenDir.getStepX(), velocity.y, velocity.z);
                }
                else {
                    this.setDeltaMovement(velocity.x, velocity.y, 0.1 * chosenDir.getStepZ());
                }
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.connection.send(new PacketCSSkinType());
    }

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
            this.xBob += (float) ((this.getXRot() - this.xBob) * 0.5);
            this.yBob += (float) ((this.getYRot() - this.yBob) * 0.5);
        }
    }

    @Unique
    private boolean suffocatesAt(int x, int z) {
        AABB bb = this.getBoundingBox();
        return LevelUtils.collidesWithSuffocatingBlock(this.level, this,
                                                       x + 1e-7, bb.minY + 1e-7, z + 1e-7,
                                                       x + 1 - 1e-7, bb.maxY - 1e-7, z + 1 - 1e-7);
    }
}
