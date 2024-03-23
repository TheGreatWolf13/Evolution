package tgw.evolution.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.lists.custom.WeightedList;
import tgw.evolution.util.math.IRandom;

import java.util.List;
import java.util.Random;

public class ComplexWeightedModel implements IDynamicBakedModel {

    private final WeightedList<BakedModel> list;

    public ComplexWeightedModel(WeightedList<BakedModel> list) {
        this.list = list;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.list.get(0).getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull IModelData data) {
        return this.list.get(0).getParticleIcon(data);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.list.get(0).getParticleIcon();
    }

    @Override
    public OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData) {
        return this.list.getWeighted(rand).getQuads(state, side, rand, extraData);
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
        private final WeightedList<BakedModel> list = new WeightedList<>();

        public Builder add(@Nullable BakedModel model, int weight) {
            if (model != null && weight >= 1) {
                this.list.add(model, weight);
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
            return new ComplexWeightedModel(this.list);
        }
    }
}
