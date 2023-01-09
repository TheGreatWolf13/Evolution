package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.capabilities.modular.part.PartHalfHead;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.Material;
import tgw.evolution.items.modular.part.ItemPartHalfHead;

public class BakedModelPartHalfHead
        extends BakedModelPart<PartTypes.HalfHead, ItemPartHalfHead, PartHalfHead, BakedModelPartHalfHead.BakedModelFinalPartHalfHead> {

    public BakedModelPartHalfHead(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartHalfHead(new BakedModelFinalPartHalfHead(baseModel)));
    }

    public static class ItemOverridesPartHalfHead
            extends ItemOverridesPart<PartTypes.HalfHead, ItemPartHalfHead, PartHalfHead, BakedModelFinalPartHalfHead> {

        public ItemOverridesPartHalfHead(BakedModelFinalPartHalfHead finalModel) {
            super(finalModel, PartHalfHead.DUMMY);
        }

        @Override
        protected void setModelData(PartHalfHead part) {
            this.finalModel.setData(part.getType(), part.getMaterialInstance().getMaterial(), part.isSharp());
        }
    }

    public static class BakedModelFinalPartHalfHead extends BakedModelFinalPart<PartTypes.HalfHead, ItemPartHalfHead, PartHalfHead> {

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
            if (Boolean.TRUE == extraData.getData(this.isSharp)) {
                //noinspection ConstantConditions
                return EvolutionResources.MODULAR_HALF_HEADS_SHARP.get(extraData.getData(this.type), extraData.getData(this.material));
            }
            //noinspection ConstantConditions
            return EvolutionResources.MODULAR_HALF_HEADS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        public void setData(PartTypes.HalfHead type, Material material, boolean isSharp) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
            this.modelData.setData(this.isSharp, isSharp);
        }
    }
}
