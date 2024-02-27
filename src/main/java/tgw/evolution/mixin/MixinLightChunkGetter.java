package tgw.evolution.mixin;

import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchLightChunkGetter;

@Mixin(LightChunkGetter.class)
public interface MixinLightChunkGetter extends PatchLightChunkGetter {
}
