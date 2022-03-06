package tgw.evolution.client.models.item.modular;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.IModularTool;

import javax.annotation.Nullable;

public class ItemOverridesModularTool extends ItemOverrides {

    private final BakedModelFinalModularTool finalModel;

    public ItemOverridesModularTool(BakedModel baseModel) {
        this.finalModel = new BakedModelFinalModularTool(baseModel);
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        this.finalModel.setModelData(tool.getHeadType(), tool.getHeadMaterial(), tool.getHandleType(), tool.getHandleMaterial(), tool.isSharpened());
        return this.finalModel;
    }
}
