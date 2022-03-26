package tgw.evolution.client.models.item.modular;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.IModularTool;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.items.IThrowable;
import tgw.evolution.patches.ILivingEntityPatch;

import javax.annotation.Nullable;

public class ItemOverridesModularTool extends ItemOverrides {

    private final BakedModelFinalModularTool finalModel;

    public ItemOverridesModularTool(BakedModel baseModel) {
        this.finalModel = new BakedModelFinalModularTool(baseModel);
    }

    private static boolean isSweeping(ItemStack stack, @Nullable LivingEntity entity) {
        IModularTool tool = IModularTool.get(stack);
        if (tool.getHead().getType() == PartTypes.Head.SPEAR) {
            return entity != null &&
                   ((ILivingEntityPatch) entity).renderMainhandSpecialAttack() &&
                   entity.getMainHandItem() == stack &&
                   ((ILivingEntityPatch) entity).getMainhandSpecialAttackProgress(Minecraft.getInstance().getFrameTime()) >= 0.5f;
        }
        return entity != null && ((ILivingEntityPatch) entity).renderMainhandSpecialAttack() && entity.getMainHandItem() == stack;
    }

    private static boolean isThrowing(ItemStack stack, @Nullable LivingEntity entity) {
        return entity != null && entity.isUsingItem() && entity.getUseItem() == stack && ((IThrowable) stack.getItem()).isThrowable(stack);
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        this.finalModel.setModelData(tool.getHead().getType(), tool.getHead().getMaterial().getMaterial(), tool.getHandle().getType(),
                                     tool.getHandle().getMaterial().getMaterial(), tool.isSharpened());
        boolean isThrowing = isThrowing(stack, entity);
        boolean isSweeping = !isThrowing && isSweeping(stack, entity);
        this.finalModel.setThrowing(isThrowing);
        this.finalModel.setSweeping(isSweeping);
        return this.finalModel;
    }
}
