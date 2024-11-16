package tgw.evolution.mixin;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchChannel;

@Mixin(Channel.class)
public abstract class MixinChannel implements PatchChannel {

    @Unique private final float[] pos = new float[3];
    @Shadow @Final private int source;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void setSelfPosition(Vec3 pos) {
        Evolution.deprecatedMethod();
        this.setSelfPosition(pos.x, pos.y, pos.z);
    }

    @Override
    public void setSelfPosition(double x, double y, double z) {
        this.pos[0] = (float) x;
        this.pos[1] = (float) y;
        this.pos[2] = (float) z;
        AL10.alSourcefv(this.source, AL10.AL_POSITION, this.pos);
    }
}
