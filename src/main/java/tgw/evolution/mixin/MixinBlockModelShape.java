package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.maps.R2OHashMap;

import java.util.IdentityHashMap;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(BlockModelShaper.class)
public abstract class MixinBlockModelShape {

    @Mutable @Shadow @Final private Map<BlockState, BakedModel> modelByStateCache;

    @Redirect(method = "<init>",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;modelByStateCache:Ljava/util/Map;"))
    private void onInit(BlockModelShaper instance, Map<BlockState, BakedModel> value) {
        this.modelByStateCache = new R2OHashMap<>();
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newIdentityHashMap()Ljava/util/IdentityHashMap;", remap = false))
    private @Nullable IdentityHashMap onInitRemoveMap() {
        return null;
    }
}
