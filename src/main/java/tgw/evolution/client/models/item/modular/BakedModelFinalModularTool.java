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
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.util.collection.RArrayList;

import java.util.List;
import java.util.Random;

public class BakedModelFinalModularTool implements BakedModel {

    public static final ModelProperty<ItemMaterial> HEAD_MATERIAL = new ModelProperty<>();
    public static final ModelProperty<PartTypes.Head> HEAD_TYPE = new ModelProperty<>();
    public static final ModelProperty<PartTypes.Handle> HANDLE_TYPE = new ModelProperty<>();
    public static final ModelProperty<ItemMaterial> HANDLE_MATERIAL = new ModelProperty<>();
    public static final ModelProperty<Boolean> IS_SHARP = new ModelProperty<>();

    private final BakedModel baseModel;
    private final IModelData modelData = getEmptyIModelData();
    private final List<BakedQuad> quadHolder = new RArrayList<>();
    private boolean isSweeping;
    private boolean isThrowing;

    public BakedModelFinalModularTool(BakedModel baseModel) {
        this.baseModel = baseModel;
    }

    public static ModelDataMap getEmptyIModelData() {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(HEAD_MATERIAL, ItemMaterial.ANDESITE);
        builder.withInitial(HEAD_TYPE, PartTypes.Head.NULL);
        builder.withInitial(HANDLE_MATERIAL, ItemMaterial.ANDESITE);
        builder.withInitial(HANDLE_TYPE, PartTypes.Handle.NULL);
        builder.withInitial(IS_SHARP, false);
        return builder.build();
    }

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
        BakedModel head;
        if (Boolean.TRUE == extraData.getData(IS_SHARP)) {
            //noinspection ConstantConditions
            head = Minecraft.getInstance()
                            .getModelManager()
                            .getModel(EvolutionResources.MODULAR_HEADS_SHARP.get(extraData.getData(HEAD_TYPE), extraData.getData(HEAD_MATERIAL)));
        }
        else {
            //noinspection ConstantConditions
            head = Minecraft.getInstance()
                            .getModelManager()
                            .getModel(EvolutionResources.MODULAR_HEADS.get(extraData.getData(HEAD_TYPE), extraData.getData(HEAD_MATERIAL)));
        }
        //noinspection ConstantConditions
        BakedModel handle = Minecraft.getInstance()
                                     .getModelManager()
                                     .getModel(EvolutionResources.MODULAR_HANDLES.get(extraData.getData(HANDLE_TYPE),
                                                                                      extraData.getData(HANDLE_MATERIAL)));
        this.quadHolder.clear();
        this.quadHolder.addAll(head.getQuads(state, side, rand));
        this.quadHolder.addAll(handle.getQuads(state, side, rand));
        return this.quadHolder;
    }

    @Override
    public ItemTransforms getTransforms() {
        if (this.isThrowing) {
            return Minecraft.getInstance().getModelManager().getModel(EvolutionResources.TOOL_THROWING).getTransforms();
        }
        if (this.isSweeping) {
            return Minecraft.getInstance().getModelManager().getModel(EvolutionResources.TOOL_SWEEP).getTransforms();
        }
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

    public void setSweeping(boolean sweeping) {
        this.isSweeping = sweeping;
    }

    public void setThrowing(boolean throwing) {
        this.isThrowing = throwing;
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
