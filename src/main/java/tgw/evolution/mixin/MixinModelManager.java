package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.ModelRegistry;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(ModelManager.class)
public abstract class MixinModelManager implements IKeyedReloadListener {

    private static final List<ResourceLocation> DEPENDENCY = List.of(ReloadListernerKeys.TEXTURES);
    @Shadow private @Nullable AtlasSet atlases;
    @Shadow private Map<ResourceLocation, BakedModel> bakedRegistry;
    @Shadow @Final private BlockModelShaper blockModelShaper;
    @Shadow private BakedModel missingModel;
    @Shadow private Object2IntMap<BlockState> modelGroups;
    @Shadow @Final private TextureManager textureManager;

    @Overwrite
    public void apply(ModelBakery bakery, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.startTick();
        profiler.push("upload");
        if (this.atlases != null) {
            this.atlases.close();
        }
        this.atlases = bakery.uploadTextures(this.textureManager, profiler);
        this.bakedRegistry = bakery.getBakedTopLevelModels();
        ModelRegistry.register(this.bakedRegistry);
        this.modelGroups = bakery.getModelGroups();
        this.missingModel = this.bakedRegistry.get(ModelBakery.MISSING_MODEL_LOCATION);
        profiler.popPush("cache");
        this.blockModelShaper.rebuildCache();
        profiler.pop();
        profiler.endTick();
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.MODELS;
    }
}
