package tgw.evolution.patches;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface PatchItemStack {

    default void mineBlock_(Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        throw new AbstractMethodError();
    }

    void onUsingTick(LivingEntity entity, int useRemaining);

    default InteractionResult useOn_(Player player, InteractionHand hand, BlockHitResult hitResult) {
        throw new AbstractMethodError();
    }
}
