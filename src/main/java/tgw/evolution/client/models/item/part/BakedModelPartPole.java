package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import tgw.evolution.capabilities.modular.part.PartPole;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.Material;
import tgw.evolution.items.modular.part.ItemPartPole;

public class BakedModelPartPole extends BakedModelPart<PartTypes.Pole, ItemPartPole, PartPole, BakedModelPartPole.BakedModelFinalPartPole> {

    public BakedModelPartPole(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartPole(new BakedModelFinalPartPole(baseModel)));
    }

    public static class ItemOverridesPartPole extends ItemOverridesPart<PartTypes.Pole, ItemPartPole, PartPole, BakedModelFinalPartPole> {

        public ItemOverridesPartPole(BakedModelFinalPartPole finalModel) {
            super(finalModel, PartPole.DUMMY);
        }

        @Override
        protected void setModelData(PartPole part) {
            this.finalModel.setData(part.getType(), part.getMaterialInstance().getMaterial());
        }
    }

    public static class BakedModelFinalPartPole extends BakedModelFinalPart<PartTypes.Pole, ItemPartPole, PartPole> {

        public BakedModelFinalPartPole(BakedModel baseModel) {
            super(baseModel, PartTypes.Pole.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            //noinspection ConstantConditions
            return EvolutionResources.MODULAR_POLES.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        protected void setData(PartTypes.Pole type, Material material) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
        }
    }
}
