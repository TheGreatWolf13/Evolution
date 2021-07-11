package tgw.evolution.client;

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
    public void tick(boolean slow, boolean noDampening) {
        boolean inverted = ClientEvents.getInstance().areControlsInverted();
        boolean dizziness = ClientEvents.getInstance().isPlayerDizzy();
        this.forwardKeyDown = inverted ? this.gameSettings.keyBindBack.isKeyDown() : this.gameSettings.keyBindForward.isKeyDown();
        this.backKeyDown = inverted ? this.gameSettings.keyBindForward.isKeyDown() : this.gameSettings.keyBindBack.isKeyDown();
        this.leftKeyDown = inverted ? this.gameSettings.keyBindRight.isKeyDown() : this.gameSettings.keyBindLeft.isKeyDown();
        this.rightKeyDown = inverted ? this.gameSettings.keyBindLeft.isKeyDown() : this.gameSettings.keyBindRight.isKeyDown();
        this.moveForward = this.forwardKeyDown == this.backKeyDown ? 0.0F : this.forwardKeyDown ? 1 : -1;
        this.moveStrafe = 0;
        if (dizziness) {
            this.moveStrafe = Math.signum(MathHelper.cos(this.tick * MathHelper.TAU / (80 >> ClientEvents.getInstance().getDizzinessAmplifier())));
        }
        this.moveStrafe += this.leftKeyDown == this.rightKeyDown ? 0.0F : this.leftKeyDown ? 1 : -1;
        this.moveStrafe = MathHelper.clamp(this.moveStrafe, -1, 1);
        this.jump = inverted ? this.gameSettings.keyBindSneak.isKeyDown() : this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = inverted ? this.gameSettings.keyBindJump.isKeyDown() : this.gameSettings.keyBindSneak.isKeyDown();
        if (!noDampening && (this.sneak || slow)) {
            this.moveStrafe *= 0.3;
            this.moveForward *= 0.3;
        }
        if (!dizziness) {
            this.tick = 0;
        }
        else {
            this.tick++;
        }
    }
}
