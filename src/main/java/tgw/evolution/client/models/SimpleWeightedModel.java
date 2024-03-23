package tgw.evolution.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.IRandom;

import java.util.List;
import java.util.Random;

public class SimpleWeightedModel implements IDynamicBakedModel {

    private final OList<BakedModel> list;

    public SimpleWeightedModel(OList<BakedModel> list) {
        this.list = list;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.list.get(0).getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(IModelData data) {
        return this.list.get(0).getParticleIcon(data);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.list.get(0).getParticleIcon();
    }

    @Override
    public OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData) {
        return this.list.get(rand.nextInt(this.list.size())).getQuads(state, side, rand, extraData);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
        Evolution.deprecatedMethod();
        return List.of();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.list.get(0).getTransforms();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.list.get(0).isCustomRenderer();
    }

    @Override
    public boolean isGui3d() {
        return this.list.get(0).isGui3d();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.list.get(0).useAmbientOcclusion();
    }

    @Override
    public boolean usesBlockLight() {
        return this.list.get(0).usesBlockLight();
    }

    public static class Builder {
        private final OList<BakedModel> list = new OArrayList<>();

        public Builder add(@Nullable BakedModel model) {
            if (model != null) {
                this.list.add(model);
            }
            return this;
        }

        public @Nullable BakedModel build() {
            if (this.list.isEmpty()) {
                return null;
            }
            if (this.list.size() == 1) {
                return this.list.get(0);
            }
            this.list.trim();
            return new SimpleWeightedModel(this.list);
        }
    }
}
