package tgw.evolution.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.events.ClientEvents;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {

    @Shadow @Final private Options options;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private float calculatePitch(SoundInstance soundInstance) {
        if (soundInstance.getSource() == SoundSource.MASTER) {
            return Mth.clamp(soundInstance.getPitch(), 0.5F, 2.0F);
        }
        float mul = ClientEvents.getPitchMul();
        return Mth.clamp(soundInstance.getPitch(), 0.5F, 2.0F) * mul;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private float getVolume(@Nullable SoundSource soundSource) {
        if (soundSource == SoundSource.MASTER) {
            return 1.0f;
        }
        float mul = ClientEvents.getVolumeMultiplier();
        if (mul <= 0) {
            return 0;
        }
        return soundSource != null ? this.options.getSoundSourceVolume(soundSource) * mul : mul;
    }
}
