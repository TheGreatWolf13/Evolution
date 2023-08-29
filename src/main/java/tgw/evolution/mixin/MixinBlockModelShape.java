package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.maps.R2OHashMap;

import java.util.IdentityHashMap;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(BlockModelShaper.class)
public abstract class MixinBlockModelShape {

    @Mutable @Shadow @Final private Map<BlockState, BakedModel> modelByStateCache;

    @Shadow @Final private ModelManager modelManager;

    @Contract(value = "_ -> _")
    @Shadow
    public static ModelResourceLocation stateToModelLocation(BlockState blockState) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;modelByStateCache:Ljava/util/Map;"))
    private void onInit(BlockModelShaper instance, Map<BlockState, BakedModel> value) {
        this.modelByStateCache = new R2OHashMap<>();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newIdentityHashMap()Ljava/util/IdentityHashMap;", remap = false))
    private @Nullable IdentityHashMap onInitRemoveMap() {
        return null;
    }

    @Overwrite
    public void rebuildCache() {
        this.modelByStateCache.clear();
        for (Block block : Registry.BLOCK) {
            ImmutableList<BlockState> possibleStates = block.getStateDefinition().getPossibleStates();
            for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                BlockState state = possibleStates.get(i);
                this.modelByStateCache.put(state, this.modelManager.getModel(stateToModelLocation(state)));
            }
        }
    }
}
