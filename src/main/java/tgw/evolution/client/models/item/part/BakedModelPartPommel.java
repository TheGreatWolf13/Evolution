package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import tgw.evolution.capabilities.modular.part.PartPommel;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPartPommel;

public class BakedModelPartPommel
        extends BakedModelPart<PartTypes.Pommel, ItemPartPommel, PartPommel, BakedModelPartPommel.BakedModelFinalPartPommel> {

    public BakedModelPartPommel(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartPommel(new BakedModelFinalPartPommel(baseModel)));
    }

    public static class ItemOverridesPartPommel extends ItemOverridesPart<PartTypes.Pommel, ItemPartPommel, PartPommel, BakedModelFinalPartPommel> {

        public ItemOverridesPartPommel(BakedModelFinalPartPommel finalModel) {
            super(finalModel, PartPommel.DUMMY);
        }

        @Override
        protected void setModelData(PartPommel part) {
            this.finalModel.setData(part.getType(), part.getMaterialInstance().getMaterial());
        }
    }

    public static class BakedModelFinalPartPommel extends BakedModelFinalPart<PartTypes.Pommel, ItemPartPommel, PartPommel> {

        public BakedModelFinalPartPommel(BakedModel baseModel) {
            super(baseModel, PartTypes.Pommel.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            //noinspection ConstantConditions
            return EvolutionResources.MODULAR_POMMELS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        protected void setData(PartTypes.Pommel type, ItemMaterial material) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
        }
    }
}
