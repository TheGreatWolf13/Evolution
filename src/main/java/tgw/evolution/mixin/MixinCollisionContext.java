package tgw.evolution.mixin;

import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchCollisionContext;

@Mixin(CollisionContext.class)
public interface MixinCollisionContext extends PatchCollisionContext {
}
