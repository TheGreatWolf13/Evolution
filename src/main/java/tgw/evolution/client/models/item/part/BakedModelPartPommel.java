package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.capabilities.modular.part.PommelPart;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;

public class BakedModelPartPommel extends BakedModelPart<PartTypes.Pommel, PommelPart, BakedModelPartPommel.BakedModelFinalPartPommel> {

    public BakedModelPartPommel(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartPommel(new BakedModelFinalPartPommel(baseModel)));
    }

    public static class ItemOverridesPartPommel extends ItemOverridesPart<PartTypes.Pommel, PommelPart, BakedModelFinalPartPommel> {

        public ItemOverridesPartPommel(BakedModelFinalPartPommel finalModel) {
            super(finalModel, PommelPart.DUMMY);
        }

        @Override
        protected void setModelData(PommelPart part) {
            this.finalModel.setData(part.getType(), part.getMaterial().getMaterial());
        }
    }

    public static class BakedModelFinalPartPommel extends BakedModelFinalPart<PartTypes.Pommel> {

        public BakedModelFinalPartPommel(BakedModel baseModel) {
            super(baseModel, PartTypes.Pommel.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            return EvolutionResources.MODULAR_POMMELS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        protected void setData(PartTypes.Pommel type, ItemMaterial material) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
        }
    }
}
