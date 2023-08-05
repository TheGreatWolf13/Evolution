package tgw.evolution.mixin;

import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchShapeGetter;

@Mixin(ClipContext.ShapeGetter.class)
public interface MixinShapeGetter extends PatchShapeGetter {
}
