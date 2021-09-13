package tgw.evolution.client.util;

import net.minecraft.client.GameSettings;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.MathHelper;

@OnlyIn(Dist.CLIENT)
public class MovementInputEvolution extends MovementInputFromOptions {

    private final GameSettings gameSettings;
    private int tick;

    public MovementInputEvolution(GameSettings gameSettings) {
        super(gameSettings);
        this.gameSettings = gameSettings;
    }

    @Override
    public void tick(boolean slow) {
        boolean inverted = ClientEvents.getInstance().areControlsInverted();
        boolean dizziness = ClientEvents.getInstance().isPlayerDizzy();
        this.up = inverted ? this.gameSettings.keyDown.isDown() : this.gameSettings.keyUp.isDown();
        this.down = inverted ? this.gameSettings.keyUp.isDown() : this.gameSettings.keyDown.isDown();
        this.left = inverted ? this.gameSettings.keyRight.isDown() : this.gameSettings.keyLeft.isDown();
        this.right = inverted ? this.gameSettings.keyLeft.isDown() : this.gameSettings.keyRight.isDown();
        this.forwardImpulse = this.up == this.down ? 0.0F : this.up ? 1 : -1;
        this.leftImpulse = 0;
        if (dizziness) {
            this.leftImpulse = Math.signum(MathHelper.cos(this.tick * MathHelper.TAU / (80 >> ClientEvents.getInstance().getDizzinessAmplifier())));
        }
        this.leftImpulse += this.left == this.right ? 0.0F : this.left ? 1 : -1;
        this.leftImpulse = MathHelper.clamp(this.leftImpulse, -1, 1);
        this.jumping = inverted ? this.gameSettings.keyShift.isDown() : this.gameSettings.keyJump.isDown();
        this.shiftKeyDown = inverted ? this.gameSettings.keyJump.isDown() : this.gameSettings.keyShift.isDown();
        if (!dizziness) {
            this.tick = 0;
        }
        else {
            this.tick++;
        }
    }
}
