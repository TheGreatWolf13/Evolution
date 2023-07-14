package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.math.DirectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(SimpleBakedModel.Builder.class)
public abstract class MixinSimpleBakedModel_Builder {

    @Final @Shadow @Mutable private final List<BakedQuad> unculledFaces = new OArrayList<>();

    @Redirect(method = "<init>(ZZZLnet/minecraft/client/renderer/block/model/ItemTransforms;" +
                       "Lnet/minecraft/client/renderer/block/model/ItemOverrides;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core" +
                                                                                                                          "/Direction;values()" +
                                                                                                                          "[Lnet/minecraft/core" +
                                                                                                                          "/Direction;"))
    private Direction[] onInit() {
        return DirectionUtil.ALL;
    }

    @Redirect(method = "<init>(ZZZLnet/minecraft/client/renderer/block/model/ItemTransforms;" +
                       "Lnet/minecraft/client/renderer/block/model/ItemOverrides;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put" +
                                                "(Ljava/lang/Object;" +
                                                "Ljava/lang/Object;)" +
                                                "Ljava/lang/Object;"))
    private @Nullable Object onInit(Map map, Object dir, Object list) {
        return map.put(dir, new OArrayList<>());
    }

    @Redirect(method = "<init>(ZZZLnet/minecraft/client/renderer/block/model/ItemTransforms;" +
                       "Lnet/minecraft/client/renderer/block/model/ItemOverrides;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false), require = 2)
    private @Nullable ArrayList onInitRemoveList() {
        return null;
    }
}
