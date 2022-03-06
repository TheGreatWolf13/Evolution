package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.capabilities.modular.part.HalfHeadPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;

public class BakedModelPartHalfHead extends BakedModelPart<PartTypes.HalfHead, HalfHeadPart, BakedModelPartHalfHead.BakedModelFinalPartHalfHead> {

    public BakedModelPartHalfHead(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartHalfHead(new BakedModelFinalPartHalfHead(baseModel)));
    }

    public static class ItemOverridesPartHalfHead extends ItemOverridesPart<PartTypes.HalfHead, HalfHeadPart, BakedModelFinalPartHalfHead> {

        public ItemOverridesPartHalfHead(BakedModelFinalPartHalfHead finalModel) {
            super(finalModel, HalfHeadPart.DUMMY);
        }

        @Override
        protected void setModelData(HalfHeadPart part) {
            this.finalModel.setData(part.getType(), part.getMaterial().getMaterial(), part.isSharp());
        }
    }

    public static class BakedModelFinalPartHalfHead extends BakedModelFinalPart<PartTypes.HalfHead> {

        public final ModelProperty<Boolean> isSharp = new ModelProperty<>();

        public BakedModelFinalPartHalfHead(BakedModel baseModel) {
            super(baseModel, PartTypes.HalfHead.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
            builder.withInitial(this.isSharp, false);
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            if (extraData.getData(this.isSharp)) {
                return EvolutionResources.MODULAR_HALF_HEADS_SHARP.get(extraData.getData(this.type), extraData.getData(this.material));
            }
            return EvolutionResources.MODULAR_HALF_HEADS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        public void setData(PartTypes.HalfHead type, ItemMaterial material, boolean isSharp) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
            this.modelData.setData(this.isSharp, isSharp);
        }
    }
}
