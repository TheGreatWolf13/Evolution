package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.world.util.MutableCollisionContext;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ClipContext.class)
public abstract class MixinClipContext {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/CollisionContext;of" +
                                                                     "(Lnet/minecraft/world/entity/Entity;)" +
                                                                     "Lnet/minecraft/world/phys/shapes/CollisionContext;"))
    private CollisionContext onInit(Entity entity) {
        return new MutableCollisionContext().set(entity);
    }
}
