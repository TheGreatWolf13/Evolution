package tgw.evolution.mixin;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.Map;

@Mixin(EntityModelSet.class)
public abstract class Mixin_CF_EntityModelSet implements ResourceManagerReloadListener {

    @Shadow @DeleteField private Map<ModelLayerLocation, LayerDefinition> roots;
    @Unique private O2OMap<ModelLayerLocation, LayerDefinition> roots_;

    @ModifyConstructor
    public Mixin_CF_EntityModelSet() {
        this.roots_ = O2OMap.emptyMap();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ModelPart bakeLayer(ModelLayerLocation location) {
        LayerDefinition layerDefinition = this.roots_.get(location);
        if (layerDefinition == null) {
            throw new IllegalArgumentException("No model for layer " + location);
        }
        return layerDefinition.bakeRoot();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.roots_ = ((O2OMap<ModelLayerLocation, LayerDefinition>) LayerDefinitions.createRoots()).view();
    }
}
