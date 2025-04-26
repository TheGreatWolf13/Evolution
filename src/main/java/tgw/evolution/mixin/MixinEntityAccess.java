package tgw.evolution.mixin;

import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchEntity;

@Mixin(EntityAccess.class)
public interface MixinEntityAccess extends PatchEntity {
}
