package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.math.DirectionUtil;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(SimpleBakedModel.Builder.class)
public class SimpleBakedModel_BuilderMixin {

    @Final
    @Shadow
    @Mutable
    private final List<BakedQuad> unculledFaces = new RArrayList<>();

    @Redirect(method = "<init>(ZZZLnet/minecraft/client/renderer/block/model/ItemTransforms;" +
                       "Lnet/minecraft/client/renderer/block/model/ItemOverrides;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core" +
                                                                                                                          "/Direction;values()" +
                                                                                                                          "[Lnet/minecraft/core" +
                                                                                                                          "/Direction;"))
    private Direction[] proxyInit0() {
        return DirectionUtil.ALL;
    }

    @Nullable
    @Redirect(method = "<init>(ZZZLnet/minecraft/client/renderer/block/model/ItemTransforms;" +
                       "Lnet/minecraft/client/renderer/block/model/ItemOverrides;)V", at = @At(value = "INVOKE", target = "Lcom/google/common" +
                                                                                                                          "/collect/Lists;" +
                                                                                                                          "newArrayList()" +
                                                                                                                          "Ljava/util/ArrayList;"))
    private ArrayList proxyInit1() {
        return null;
    }

    @Redirect(method = "<init>(ZZZLnet/minecraft/client/renderer/block/model/ItemTransforms;" +
                       "Lnet/minecraft/client/renderer/block/model/ItemOverrides;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put" +
                                                                                                                          "(Ljava/lang/Object;" +
                                                                                                                          "Ljava/lang/Object;)" +
                                                                                                                          "Ljava/lang/Object;"))
    private Object proxyInit2(Map map, Object dir, Object list) {
        return map.put(dir, new RArrayList<>());
    }
}
