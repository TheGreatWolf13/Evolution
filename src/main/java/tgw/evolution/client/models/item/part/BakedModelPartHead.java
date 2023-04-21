package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.capabilities.modular.part.PartHead;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.items.modular.part.ItemPartHead;

public class BakedModelPartHead extends BakedModelPart<PartTypes.Head, ItemPartHead, PartHead, BakedModelPartHead.BakedModelFinalPartHead> {

    public BakedModelPartHead(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartHead(new BakedModelFinalPartHead(baseModel)));
    }

    public static class ItemOverridesPartHead extends ItemOverridesPart<PartTypes.Head, ItemPartHead, PartHead, BakedModelFinalPartHead> {

        public ItemOverridesPartHead(BakedModelFinalPartHead finalModel) {
            super(finalModel, PartHead.DUMMY);
        }

        @Override
        protected void setModelData(PartHead part) {
            this.finalModel.setData(part.getType(), part.getMaterialInstance().getMaterial(), part.isSharp());
        }
    }

    public static class BakedModelFinalPartHead extends BakedModelFinalPart<PartTypes.Head, ItemPartHead, PartHead> {
        public final ModelProperty<Boolean> isSharp = new ModelProperty<>();

        public BakedModelFinalPartHead(BakedModel baseModel) {
            super(baseModel, PartTypes.Head.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
            builder.withInitial(this.isSharp, false);
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            if (Boolean.TRUE == extraData.getData(this.isSharp)) {
                //noinspection ConstantConditions
                return EvolutionResources.MODULAR_HEADS_SHARP.get(extraData.getData(this.type), extraData.getData(this.material));
            }
            //noinspection ConstantConditions
            return EvolutionResources.MODULAR_HEADS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        protected void setData(PartTypes.Head type, EvolutionMaterials material, boolean isSharp) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
            this.modelData.setData(this.isSharp, isSharp);
        }
    }
}
