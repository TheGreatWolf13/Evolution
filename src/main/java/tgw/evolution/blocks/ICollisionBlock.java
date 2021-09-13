package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;

public interface ICollisionBlock {
    void collision(LivingEntity entity, double speed);

    float getSlowdownSide(BlockState state);

    float getSlowdownTop(BlockState state);
}
