//package tgw.evolution.client.models.item.modular;
//
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.renderer.block.model.ItemOverrides;
//import net.minecraft.client.resources.model.BakedModel;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemStack;
//import org.jetbrains.annotations.Nullable;
//import tgw.evolution.capabilities.modular.CapabilityModular;
//import tgw.evolution.capabilities.modular.IModularTool;
//import tgw.evolution.init.EvolutionCapabilities;
//
//public class ItemOverridesModularTool extends ItemOverrides {
//
//    private final BakedModelFinalModularTool finalModel;
//
//    public ItemOverridesModularTool(BakedModel baseModel) {
//        this.finalModel = new BakedModelFinalModularTool(baseModel);
//    }
//
//    @Nullable
//    @Override
//    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
//        IModularTool tool = EvolutionCapabilities.getCapability(stack, CapabilityModular.TOOL, IModularTool.NULL);
//        this.finalModel.setModelData(tool.getHead().getType(), tool.getHead().getMaterialInstance().getMaterial(), tool.getHandle().getType(),
//                                     tool.getHandle().getMaterialInstance().getMaterial(), tool.isSharpened());
//        return this.finalModel;
//    }
//}
