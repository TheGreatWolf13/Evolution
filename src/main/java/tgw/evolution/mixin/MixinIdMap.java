package tgw.evolution.mixin;

import net.minecraft.core.IdMap;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchIdMap;

@Mixin(IdMap.class)
public interface MixinIdMap<T> extends Iterable<T>, PatchIdMap<T> {

}
