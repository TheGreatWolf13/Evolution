package tgw.evolution.client.models.item.modular;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class BakedModelFinalModularTool implements BakedModel {

    public static final ModelProperty<ItemMaterial> HEAD_MATERIAL = new ModelProperty<>();
    public static final ModelProperty<PartTypes.Head> HEAD_TYPE = new ModelProperty<>();
    public static final ModelProperty<PartTypes.Handle> HANDLE_TYPE = new ModelProperty<>();
    public static final ModelProperty<ItemMaterial> HANDLE_MATERIAL = new ModelProperty<>();
    public static final ModelProperty<Boolean> IS_SHARP = new ModelProperty<>();

    private final BakedModel baseModel;
    private final IModelData modelData = getEmptyIModelData();

    public BakedModelFinalModularTool(BakedModel baseModel) {
        this.baseModel = baseModel;
    }

    public static ModelDataMap getEmptyIModelData() {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(HEAD_MATERIAL, ItemMaterial.STONE_ANDESITE);
        builder.withInitial(HEAD_TYPE, PartTypes.Head.NULL);
        builder.withInitial(HANDLE_MATERIAL, ItemMaterial.STONE_ANDESITE);
        builder.withInitial(HANDLE_TYPE, PartTypes.Handle.NULL);
        builder.withInitial(IS_SHARP, false);
        return builder.build();
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull BlockAndTintGetter level,
                                   @Nonnull BlockPos pos,
                                   @Nonnull BlockState state,
                                   @Nonnull IModelData tileData) {
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
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        return this.baseModel.getParticleIcon(data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return this.getQuads(state, side, rand, this.modelData);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        BakedModel head;
        if (extraData.getData(IS_SHARP)) {
            head = Minecraft.getInstance()
                            .getModelManager()
                            .getModel(EvolutionResources.MODULAR_HEADS_SHARP.get(extraData.getData(HEAD_TYPE), extraData.getData(HEAD_MATERIAL)));
        }
        else {
            head = Minecraft.getInstance()
                            .getModelManager()
                            .getModel(EvolutionResources.MODULAR_HEADS.get(extraData.getData(HEAD_TYPE), extraData.getData(HEAD_MATERIAL)));
        }
        BakedModel handle = Minecraft.getInstance()
                                     .getModelManager()
                                     .getModel(EvolutionResources.MODULAR_HANDLES.get(extraData.getData(HANDLE_TYPE),
                                                                                      extraData.getData(HANDLE_MATERIAL)));
        List<BakedQuad> combinedQuadsList = new ArrayList<>(head.getQuads(state, side, rand));
        combinedQuadsList.addAll(handle.getQuads(state, side, rand));
        return combinedQuadsList;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    public void setModelData(PartTypes.Head headType,
                             ItemMaterial headMaterial,
                             PartTypes.Handle handleType,
                             ItemMaterial handleMaterial,
                             boolean isSharp) {
        this.modelData.setData(HEAD_TYPE, headType);
        this.modelData.setData(HEAD_MATERIAL, headMaterial);
        this.modelData.setData(HANDLE_TYPE, handleType);
        this.modelData.setData(HANDLE_MATERIAL, handleMaterial);
        this.modelData.setData(IS_SHARP, isSharp);
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
