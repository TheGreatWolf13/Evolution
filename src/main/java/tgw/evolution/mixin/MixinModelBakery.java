package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ModelBakery.class)
public abstract class MixinModelBakery {

    @Shadow @Final public static ModelResourceLocation MISSING_MODEL_LOCATION;
    @Shadow @Final private static Logger LOGGER;
    @Mutable @Shadow @Final private Map<ResourceLocation, Pair<TextureAtlas, TextureAtlas.Preparations>> atlasPreparations;
    @Mutable @Shadow @Final private Map<Triple<ResourceLocation, Transformation, Boolean>, BakedModel> bakedCache;
    @Mutable @Shadow @Final private Map<ResourceLocation, BakedModel> bakedTopLevelModels;
    @Mutable @Shadow @Final private Set<ResourceLocation> loadingStack;
    @Mutable @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Mutable @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public UnbakedModel getModel(ResourceLocation location) {
        if (this.unbakedCache.containsKey(location)) {
            return this.unbakedCache.get(location);
        }
        if (this.loadingStack.contains(location)) {
            throw new IllegalStateException("Circular reference while loading " + location);
        }
        this.loadingStack.add(location);
        UnbakedModel unbakedmodel = this.unbakedCache.get(MISSING_MODEL_LOCATION);
        while (!this.loadingStack.isEmpty()) {
            ResourceLocation loc = ((OSet<ResourceLocation>) this.loadingStack).getElement();
            assert loc != null;
            try {
                if (!this.unbakedCache.containsKey(loc)) {
                    this.loadModel(loc);
                }
            }
            catch (ModelBakery.BlockStateDefinitionException e) {
                LOGGER.warn(e.getMessage());
                this.unbakedCache.put(loc, unbakedmodel);
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", loc, location, exception);
                this.unbakedCache.put(loc, unbakedmodel);
            }
            finally {
                this.loadingStack.remove(loc);
            }
        }
        return this.unbakedCache.getOrDefault(location, unbakedmodel);
    }

    @Shadow
    protected abstract void loadModel(ResourceLocation resourceLocation) throws Exception;

    @Redirect(method = "<init>", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadingStack:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit(ModelBakery instance, Set<ResourceLocation> value) {
        this.loadingStack = new OHashSet<>();
    }

    @Redirect(method = "<init>", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;unbakedCache:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit0(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.unbakedCache = new O2OHashMap<>();
    }

    @Redirect(method = "<init>", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakedCache:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit1(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.bakedCache = new O2OHashMap<>();
    }

    @Redirect(method = "<init>", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;topLevelModels:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit2(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.topLevelModels = new O2OHashMap<>();
    }

    @Redirect(method = "<init>", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakedTopLevelModels:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit3(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.bakedTopLevelModels = new O2OHashMap<>();
    }

    @Redirect(method = "<init>", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;atlasPreparations:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit4(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.atlasPreparations = new O2OHashMap<>();
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false),
            require = 5)
    private @Nullable HashMap onInitRemoveMap() {
        return null;
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;", remap = false),
            require = 1)
    private @Nullable HashSet onInitRemoveSet() {
        return null;
    }
}
