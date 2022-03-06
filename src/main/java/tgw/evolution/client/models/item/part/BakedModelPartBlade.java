package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.capabilities.modular.part.BladePart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.ItemMaterial;

public class BakedModelPartBlade extends BakedModelPart<PartTypes.Blade, BladePart, BakedModelPartBlade.BakedModelFinalPartBlade> {

    public BakedModelPartBlade(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartBlade(new BakedModelFinalPartBlade(baseModel)));
    }

    public static class ItemOverridesPartBlade extends ItemOverridesPart<PartTypes.Blade, BladePart, BakedModelFinalPartBlade> {

        public ItemOverridesPartBlade(BakedModelFinalPartBlade finalModel) {
            super(finalModel, BladePart.DUMMY);
        }

        @Override
        protected void setModelData(BladePart part) {
            this.finalModel.setData(part.getType(), part.getMaterial().getMaterial(), part.isSharp());
        }
    }

    public static class BakedModelFinalPartBlade extends BakedModelFinalPart<PartTypes.Blade> {

        public final ModelProperty<Boolean> isSharp = new ModelProperty<>();

        public BakedModelFinalPartBlade(BakedModel baseModel) {
            super(baseModel, PartTypes.Blade.NULL);
        }

        @Override
        protected void appendToEmptyModelData(ModelDataMap.Builder builder) {
            builder.withInitial(this.isSharp, false);
        }

        @Override
        protected ModelResourceLocation getModel(IModelData extraData) {
            if (extraData.getData(this.isSharp)) {
                return EvolutionResources.MODULAR_BLADES_SHARP.get(extraData.getData(this.type), extraData.getData(this.material));
            }
            return EvolutionResources.MODULAR_BLADES.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        public void setData(PartTypes.Blade type, ItemMaterial material, boolean isSharp) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
            this.modelData.setData(this.isSharp, isSharp);
        }
    }
}
