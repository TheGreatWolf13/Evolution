package tgw.evolution.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;

import java.util.List;
import java.util.Random;

public class SimpleWeightedModel implements IDynamicBakedModel {

    private final RList<BakedModel> list;

    public SimpleWeightedModel(RList<BakedModel> list) {
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
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state,
                                             @Nullable Direction side,
                                             @NotNull Random rand,
                                             @NotNull IModelData extraData) {
        return this.list.get(rand.nextInt(this.list.size())).getQuads(state, side, rand, extraData);
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.list.get(0).getTransforms();
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
        return this.list.get(0).handlePerspective(cameraTransformType, poseStack);
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
    public boolean useAmbientOcclusion(BlockState state) {
        return this.list.get(0).useAmbientOcclusion(state);
    }

    @Override
    public boolean usesBlockLight() {
        return this.list.get(0).usesBlockLight();
    }

    public static class Builder {
        private final RList<BakedModel> list = new RArrayList<>();

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
            this.list.trimCollection();
            return new SimpleWeightedModel(this.list);
        }
    }
}
