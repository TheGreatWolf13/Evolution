package tgw.evolution.client.models.item.modular;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.IModularTool;
import tgw.evolution.init.EvolutionCapabilities;

public class ItemOverridesModularTool extends ItemOverrides {

    private final BakedModelFinalModularTool finalModel;

    public ItemOverridesModularTool(BakedModel baseModel) {
        this.finalModel = new BakedModelFinalModularTool(baseModel);
    }

//    private static boolean isBasicAttacking(ItemStack stack, @Nullable LivingEntity entity) {
//        IModularTool tool = IModularTool.get(stack);
//        return entity != null && ((ILivingEntityPatch) entity).shouldRenderSpecialAttack() && entity.getMainHandItem() == stack;
//    }
//
//    private static boolean isThrowing(ItemStack stack, @Nullable LivingEntity entity) {
//        return entity != null && entity.isUsingItem() && entity.getUseItem() == stack && ((IThrowable) stack.getItem()).isThrowable(stack);
//    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        IModularTool tool = EvolutionCapabilities.getCapability(stack, CapabilityModular.TOOL, IModularTool.NULL);
        this.finalModel.setModelData(tool.getHead().getType(), tool.getHead().getMaterialInstance().getMaterial(), tool.getHandle().getType(),
                                     tool.getHandle().getMaterialInstance().getMaterial(), tool.isSharpened());
//        boolean isThrowing = isThrowing(stack, entity);
//        boolean isBasicAttacking = !isThrowing && isBasicAttacking(stack, entity);
//        this.finalModel.setThrowing(isThrowing);
//        this.finalModel.setBasicAttacking(isBasicAttacking);
//        if (isBasicAttacking) {
//            this.setupBasicAttack(entity);
//        }
        return this.finalModel;
    }

//    private void setupBasicAttack(@Nullable LivingEntity entity) {
//        if (entity instanceof ILivingEntityPatch patch) {
//            float progress = patch.getSpecialAttackProgress(Evolution.PROXY.getPartialTicks());
//            if (progress < 0.5f) {
//                this.finalModel.setBasicAttack(0, 3, 1.5f, 0, -90, 40);
//            }
//            else if (progress < 0.75f) {
//                float t = MathHelper.animInterval(progress, 0.5f, 0.75f);
//                this.finalModel.setBasicAttack(Mth.lerp(t, 0, -0.75f), Mth.lerp(t, 3, -3), Mth.lerp(t, 1.5f, -5.5f),
//                                               MathHelper.lerpDeg(t, 0, -90, false), -90,
//                                               MathHelper.lerpDeg(t, 40, 45, false));
//            }
//            else {
//                this.finalModel.setBasicAttack(-0.75f, -3, -5.5f, -90, -90, 45);
//            }
//        }
//    }
}
