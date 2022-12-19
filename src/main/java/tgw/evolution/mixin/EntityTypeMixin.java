package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType$Builder;sized(FF)" +
                                                                        "Lnet/minecraft/world/entity/EntityType$Builder;"), index = 0)
    private static float modifyClinit(float width) {
        if (width == 0.7f) {
            return 0.75f;
        }
        return width;
    }
}
