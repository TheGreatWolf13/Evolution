package tgw.evolution.client.models.item.part;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPart;

import java.util.List;
import java.util.Random;

public abstract class BakedModelFinalPart<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> implements BakedModel {

    public final ModelProperty<ItemMaterial> material = new ModelProperty<>();
    public final ModelProperty<T> type = new ModelProperty<>();
    protected final BakedModel baseModel;
    protected final IModelData modelData;

    public BakedModelFinalPart(BakedModel baseModel, T nullPart) {
        this.baseModel = baseModel;
        this.modelData = this.getEmptyIModelData(nullPart);
    }

    protected abstract void appendToEmptyModelData(ModelDataMap.Builder builder);

    private ModelDataMap getEmptyIModelData(T nullPart) {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(this.material, ItemMaterial.ANDESITE);
        builder.withInitial(this.type, nullPart);
        this.appendToEmptyModelData(builder);
        return builder.build();
    }

    protected abstract ModelResourceLocation getModel(IModelData extraData);

    @Override
    public @NotNull IModelData getModelData(BlockAndTintGetter level,
                                            BlockPos pos,
                                            BlockState state,
                                            IModelData tileData) {
        return this.modelData;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.baseModel.getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        throw new AssertionError("IBakedModel::getParticleIcon should never be called, only IForgeBakedModel::getParticleIcon");
    }

    @Override
    public TextureAtlasSprite getParticleIcon(IModelData data) {
        return this.baseModel.getParticleIcon(data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return this.getQuads(state, side, rand, this.modelData);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(this.getModel(extraData));
        return model.getQuads(state, side, rand);
    }

    @Override
    public ItemTransforms getTransforms() {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(this.getModel(this.modelData));
        return model.getTransforms();
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
        //Lights up the whole model in the gui
        return false;
    }
}
