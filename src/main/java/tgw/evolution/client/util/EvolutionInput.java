package tgw.evolution.client.util;

import net.minecraft.client.Options;
import net.minecraft.client.player.KeyboardInput;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.math.MathHelper;

@OnlyIn(Dist.CLIENT)
public class EvolutionInput extends KeyboardInput {

    private final Options options;
    private int tick;

    public EvolutionInput(Options options) {
        super(options);
        this.options = options;
    }

    @Override
    public void tick(boolean slow) {
        boolean inverted = ClientEvents.getInstance().areControlsInverted();
        boolean dizziness = ClientEvents.getInstance().isPlayerDizzy();
        this.up = inverted ? this.options.keyDown.isDown() : this.options.keyUp.isDown();
        this.down = inverted ? this.options.keyUp.isDown() : this.options.keyDown.isDown();
        this.left = inverted ? this.options.keyRight.isDown() : this.options.keyLeft.isDown();
        this.right = inverted ? this.options.keyLeft.isDown() : this.options.keyRight.isDown();
        this.forwardImpulse = this.up == this.down ? 0.0F : this.up ? 1 : -1;
        this.leftImpulse = this.left == this.right ? 0.0F : this.left ? 1 : -1;
        if (dizziness) {
            if (this.forwardImpulse != 0) {
                this.leftImpulse += 1.5 * MathHelper.cos(this.tick * MathHelper.TAU / (32 << ClientEvents.getInstance().getDizzinessAmplifier()));
            }
            else if (this.leftImpulse != 0) {
                this.forwardImpulse += 1.5 * MathHelper.cos(this.tick * MathHelper.TAU / (32 << ClientEvents.getInstance().getDizzinessAmplifier()));
            }
            else {
                dizziness = false;
            }
        }
        else {
            this.tick = 0;
        }
        this.jumping = inverted ? this.options.keyShift.isDown() : this.options.keyJump.isDown();
        this.shiftKeyDown = inverted ? this.options.keyJump.isDown() : this.options.keyShift.isDown();
        if (dizziness) {
            this.tick++;
        }
    }
}
