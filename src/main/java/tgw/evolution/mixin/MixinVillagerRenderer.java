package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.hitbox.hrs.LegacyHRVillager;

@Mixin(VillagerRenderer.class)
public abstract class MixinVillagerRenderer extends MobRenderer<Villager, VillagerModel<Villager>> implements LegacyHRVillager {

    public MixinVillagerRenderer(EntityRendererProvider.Context pContext,
                                 VillagerModel<Villager> pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }

    /**
     * @author TheGreatWolf
     * @reason Fix HRs.
     */
    @Override
    @Overwrite
    public void scale(Villager entity, PoseStack matrices, float partialTicks) {
        this.setScale(entity, matrices, partialTicks);
    }
}
