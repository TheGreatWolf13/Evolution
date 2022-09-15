package tgw.evolution.client.models.item.part;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.items.modular.part.ItemPart;

import java.util.List;
import java.util.Random;

public abstract class BakedModelPart<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>,
        M extends BakedModelFinalPart<T, I, P>>
        implements BakedModel {

    private final BakedModel baseModel;
    private final ItemOverridesPart<T, I, P, M> overrides;

    public BakedModelPart(BakedModel baseModel, ItemOverridesPart<T, I, P, M> overrides) {
        this.baseModel = baseModel;
        this.overrides = overrides;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return this.baseModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean usesBlockLight() {
        return this.baseModel.usesBlockLight();
    }
}
