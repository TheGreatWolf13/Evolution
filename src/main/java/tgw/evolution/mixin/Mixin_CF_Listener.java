package tgw.evolution.mixin;

import com.mojang.blaze3d.audio.Listener;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.util.math.Vec3d;

@Mixin(Listener.class)
public abstract class Mixin_CF_Listener {

    @Unique private final Vec3d pos;
    @DeleteField @Shadow private Vec3 position;

    @ModifyConstructor
    public Mixin_CF_Listener() {
        this.pos = new Vec3d();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Vec3 getListenerPosition() {
        return this.pos;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void setListenerPosition(Vec3 pos) {
        this.pos.set(pos);
        AL10.alListener3f(AL10.AL_POSITION, (float) pos.x, (float) pos.y, (float) pos.z);
    }
}
