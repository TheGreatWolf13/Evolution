package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.capabilities.modular.part.HeadPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;

public class BakedModelPartHead extends BakedModelPart<PartTypes.Head, HeadPart, BakedModelPartHead.BakedModelFinalPartHead> {

    public BakedModelPartHead(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartHead(new BakedModelFinalPartHead(baseModel)));
    }

    public static class ItemOverridesPartHead extends ItemOverridesPart<PartTypes.Head, HeadPart, BakedModelFinalPartHead> {

        public ItemOverridesPartHead(BakedModelFinalPartHead finalModel) {
            super(finalModel, HeadPart.DUMMY);
        }

        @Override
        protected void setModelData(HeadPart part) {
            this.finalModel.setData(part.getType(), part.getMaterial().getMaterial(), part.isSharp());
        }
    }

    public static class BakedModelFinalPartHead extends BakedModelFinalPart<PartTypes.Head> {
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
            if (extraData.getData(this.isSharp)) {
                return EvolutionResources.MODULAR_HEADS_SHARP.get(extraData.getData(this.type), extraData.getData(this.material));
            }
            return EvolutionResources.MODULAR_HEADS.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        protected void setData(PartTypes.Head type, ItemMaterial material, boolean isSharp) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
            this.modelData.setData(this.isSharp, isSharp);
        }
    }
}
