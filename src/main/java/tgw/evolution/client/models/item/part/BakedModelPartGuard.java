package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import tgw.evolution.capabilities.modular.part.GuardPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;

public class BakedModelPartGuard extends BakedModelPart<PartTypes.Guard, GuardPart, BakedModelPartGuard.BakedModelFinalPartGuard> {

    public BakedModelPartGuard(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartGuard(new BakedModelFinalPartGuard(baseModel)));
    }

    public static class ItemOverridesPartGuard extends ItemOverridesPart<PartTypes.Guard, GuardPart, BakedModelFinalPartGuard> {

        public ItemOverridesPartGuard(BakedModelFinalPartGuard finalModel) {
            super(finalModel, GuardPart.DUMMY);
        }

        @Override
        protected void setModelData(GuardPart part) {
            this.finalModel.setData(part.getType(), part.getMaterial().getMaterial());
        }
    }

    public static class BakedModelFinalPartGuard extends BakedModelFinalPart<PartTypes.Guard> {
        public BakedModelFinalPartGuard(BakedModel baseModel) {
            super(baseModel, PartTypes.Guard.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            return EvolutionResources.MODULAR_GUARDS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        public void setData(PartTypes.Guard type, ItemMaterial material) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
        }
    }
}
