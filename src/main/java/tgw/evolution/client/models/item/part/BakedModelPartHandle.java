package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import tgw.evolution.capabilities.modular.part.PartHandle;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPartHandle;

public class BakedModelPartHandle
        extends BakedModelPart<PartTypes.Handle, ItemPartHandle, PartHandle, BakedModelPartHandle.BakedModelFinalPartHandle> {

    public BakedModelPartHandle(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartHandle(new BakedModelFinalPartHandle(baseModel)));
    }

    public static class ItemOverridesPartHandle extends ItemOverridesPart<PartTypes.Handle, ItemPartHandle, PartHandle, BakedModelFinalPartHandle> {

        public ItemOverridesPartHandle(BakedModelFinalPartHandle finalModel) {
            super(finalModel, PartHandle.DUMMY);
        }

        @Override
        protected void setModelData(PartHandle part) {
            this.finalModel.setData(part.getType(), part.getMaterialInstance().getMaterial());
        }
    }

    public static class BakedModelFinalPartHandle extends BakedModelFinalPart<PartTypes.Handle, ItemPartHandle, PartHandle> {

        public BakedModelFinalPartHandle(BakedModel baseModel) {
            super(baseModel, PartTypes.Handle.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            //noinspection ConstantConditions
            return EvolutionResources.MODULAR_HANDLES.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        protected void setData(PartTypes.Handle type, ItemMaterial material) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
        }
    }
}
