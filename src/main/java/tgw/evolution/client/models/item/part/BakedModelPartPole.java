package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.capabilities.modular.part.PolePart;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;

public class BakedModelPartPole extends BakedModelPart<PartTypes.Pole, PolePart, BakedModelPartPole.BakedModelFinalPartPole> {

    public BakedModelPartPole(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartPole(new BakedModelFinalPartPole(baseModel)));
    }

    public static class ItemOverridesPartPole extends ItemOverridesPart<PartTypes.Pole, PolePart, BakedModelFinalPartPole> {

        public ItemOverridesPartPole(BakedModelFinalPartPole finalModel) {
            super(finalModel, PolePart.DUMMY);
        }

        @Override
        protected void setModelData(PolePart part) {
            this.finalModel.setData(part.getType(), part.getMaterial().getMaterial());
        }
    }

    public static class BakedModelFinalPartPole extends BakedModelFinalPart<PartTypes.Pole> {

        public BakedModelFinalPartPole(BakedModel baseModel) {
            super(baseModel, PartTypes.Pole.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            return EvolutionResources.MODULAR_POLES.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        protected void setData(PartTypes.Pole type, ItemMaterial material) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
        }
    }
}
