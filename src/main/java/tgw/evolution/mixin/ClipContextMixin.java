package tgw.evolution.mixin;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.math.ClipContextMutable;
import tgw.evolution.world.util.MutableCollisionContext;

@Mixin(ClipContext.class)
public abstract class ClipContextMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/CollisionContext;empty()" +
                                                                     "Lnet/minecraft/world/phys/shapes/CollisionContext;"))
    private CollisionContext onInit() {
        //noinspection ConstantConditions
        if ((Object) this instanceof ClipContextMutable) {
            return new MutableCollisionContext();
        }
        return CollisionContext.empty();
    }
}
