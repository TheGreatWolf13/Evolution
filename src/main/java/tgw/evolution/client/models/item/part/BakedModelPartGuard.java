package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import tgw.evolution.capabilities.modular.part.PartGuard;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPartGuard;

public class BakedModelPartGuard extends BakedModelPart<PartTypes.Guard, ItemPartGuard, PartGuard, BakedModelPartGuard.BakedModelFinalPartGuard> {

    public BakedModelPartGuard(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartGuard(new BakedModelFinalPartGuard(baseModel)));
    }

    public static class ItemOverridesPartGuard extends ItemOverridesPart<PartTypes.Guard, ItemPartGuard, PartGuard, BakedModelFinalPartGuard> {

        public ItemOverridesPartGuard(BakedModelFinalPartGuard finalModel) {
            super(finalModel, PartGuard.DUMMY);
        }

        @Override
        protected void setModelData(PartGuard part) {
            this.finalModel.setData(part.getType(), part.getMaterialInstance().getMaterial());
        }
    }

    public static class BakedModelFinalPartGuard extends BakedModelFinalPart<PartTypes.Guard, ItemPartGuard, PartGuard> {
        public BakedModelFinalPartGuard(BakedModel baseModel) {
            super(baseModel, PartTypes.Guard.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            //noinspection ConstantConditions
            return EvolutionResources.MODULAR_GUARDS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        public void setData(PartTypes.Guard type, ItemMaterial material) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
        }
    }
}
