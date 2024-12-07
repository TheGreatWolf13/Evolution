package tgw.evolution.client.util;

import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.EvolutionClient;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.network.Message;
import tgw.evolution.network.PacketCSSimpleMessage;

public class EvolutionInput extends Input {

    private boolean crawl;
    private boolean crawlToggled;
    private boolean onClimbable;
    private final Options options;
    private int tick;

    public EvolutionInput(Options options) {
        this.options = options;
    }

    private boolean canCrawl(Player player) {
        if (player.getVehicle() != null) {
            return false;
        }
        if (player.isInAnyFluid()) {
            return false;
        }
        return !this.onClimbable || !this.jumping && player.isOnGround();
    }

    public void tick(LocalPlayer player) {
        //Crawl cooldown
        boolean wasShiftKeyDown = this.shiftKeyDown;
        this.onClimbable = player.onClimbable();
        Pose pose = player.getPose();
        boolean isCrawl = pose == Pose.SWIMMING && !player.isInWater();
        boolean isSwimming = !isCrawl && pose == Pose.SWIMMING && player.isInWater();
        if (this.crawl != isCrawl) {
            EvolutionClient.resetCooldowns();
            this.crawl = isCrawl;
        }
        //Default
        boolean inverted = player.hasEffect(EvolutionEffects.DISORIENTED);
        this.up = inverted ? this.options.keyDown.isDown() : this.options.keyUp.isDown();
        this.down = inverted ? this.options.keyUp.isDown() : this.options.keyDown.isDown();
        this.left = inverted ? this.options.keyRight.isDown() : this.options.keyLeft.isDown();
        this.right = inverted ? this.options.keyLeft.isDown() : this.options.keyRight.isDown();
        this.forwardImpulse = this.up == this.down ? 0.0F : this.up ? 1 : isSwimming ? 0 : -1;
        this.leftImpulse = this.left == this.right || isSwimming ? 0.0F : this.left ? 1 : -1;
        MobEffectInstance dizziness = player.getEffect(EvolutionEffects.DIZZINESS);
        if (dizziness != null) {
            if (this.forwardImpulse != 0) {
                this.leftImpulse += 1.5f * Mth.cos(this.tick * Mth.TWO_PI / (32 << dizziness.getAmplifier()));
                this.tick++;
            }
            else if (this.leftImpulse != 0) {
                this.forwardImpulse += 1.5f * Mth.cos(this.tick * Mth.TWO_PI / (32 << dizziness.getAmplifier()));
                this.tick++;
            }
        }
        else {
            this.tick = 0;
        }
        if (!player.isVerticalMotionLocked()) {
            this.jumping = inverted ? this.options.keyShift.isDown() : this.options.keyJump.isDown();
            this.shiftKeyDown = inverted ? this.options.keyJump.isDown() : this.options.keyShift.isDown();
            this.crawlToggled = EvolutionClient.KEY_CRAWL.isDown();
        }
        //Prevent jumping when crawling
        boolean isJumpPressed = this.jumping;
        if (player.getSwimAmount(EvolutionClient.getPartialTicks()) > 0 && !player.isInWater() && !this.onClimbable) {
            this.jumping = false;
        }
        //Stand up if jump is pressed
        if (this.crawlToggled && !this.onClimbable && player.isOnGround() && isJumpPressed) {
            BlockPos playerPos = player.blockPosition();
            if (!player.level.getBlockState_(playerPos.getX(), playerPos.getY() + 1, playerPos.getZ()).getMaterial().blocksMotion()) {
                this.crawlToggled = false;
                EvolutionClient.KEY_CRAWL.setDown(true);
                EvolutionClient.KEY_CRAWL.release();
            }
        }
        //Crawl
        this.updateClientCrawlState(player);
        //Ladders
        if (this.onClimbable) {
            if (this.shiftKeyDown && !wasShiftKeyDown) {
                player.setDeltaMovement(0, 0, 0);
            }
        }
    }

    @Override
    public void tick(boolean slow) {
        throw new IllegalStateException("Should not be called");
    }

    private void updateClientCrawlState(LocalPlayer player) {
        boolean shouldCrawl = this.crawlToggled;
        if (shouldCrawl) {
            shouldCrawl = this.canCrawl(player);
        }
        if (!shouldCrawl) {
            if (this.crawlToggled && this.onClimbable) {
                BlockPos playerPos = player.blockPosition();
                if (player.level.getBlockState_(playerPos.getX(), playerPos.getY() + 2, playerPos.getZ()).getMaterial().blocksMotion()) {
                    shouldCrawl = true;
                }
            }
        }
        if (shouldCrawl != player.isCrawling()) {
            player.connection.send(new PacketCSSimpleMessage(shouldCrawl ? Message.C2S.CRAWL_START : Message.C2S.CRAWL_END));
            player.setCrawling(shouldCrawl);
        }
    }
}
