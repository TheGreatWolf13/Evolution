package tgw.evolution.client.models.item.part;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.capabilities.modular.part.PartBlade;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.items.modular.part.ItemPartBlade;

public class BakedModelPartBlade extends BakedModelPart<PartTypes.Blade, ItemPartBlade, PartBlade, BakedModelPartBlade.BakedModelFinalPartBlade> {

    public BakedModelPartBlade(BakedModel baseModel) {
        super(baseModel, new ItemOverridesPartBlade(new BakedModelFinalPartBlade(baseModel)));
    }

    public static class ItemOverridesPartBlade extends ItemOverridesPart<PartTypes.Blade, ItemPartBlade, PartBlade, BakedModelFinalPartBlade> {

        public ItemOverridesPartBlade(BakedModelFinalPartBlade finalModel) {
            super(finalModel, PartBlade.DUMMY);
        }

        @Override
        protected void setModelData(PartBlade part) {
            this.finalModel.setData(part.getType(), part.getMaterialInstance().getMaterial(), part.isSharp());
        }
    }

    public static class BakedModelFinalPartBlade extends BakedModelFinalPart<PartTypes.Blade, ItemPartBlade, PartBlade> {

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
            if (Boolean.TRUE == extraData.getData(this.isSharp)) {
                //noinspection ConstantConditions
                return EvolutionResources.MODULAR_BLADES_SHARP.get(extraData.getData(this.type), extraData.getData(this.material));
            }
            //noinspection ConstantConditions
            return EvolutionResources.MODULAR_BLADES.get(extraData.getData(this.type), extraData.getData(this.material));
        }

        public void setData(PartTypes.Blade type, EvolutionMaterials material, boolean isSharp) {
            this.modelData.setData(this.type, type);
            this.modelData.setData(this.material, material);
            this.modelData.setData(this.isSharp, isSharp);
        }
    }
}
