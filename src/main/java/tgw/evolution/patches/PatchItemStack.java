package tgw.evolution.patches;

import net.minecraft.world.entity.LivingEntity;

public interface PatchItemStack {

    void onUsingTick(LivingEntity entity, int useRemaining);
}
