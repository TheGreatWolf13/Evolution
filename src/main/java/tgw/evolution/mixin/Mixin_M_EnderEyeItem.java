package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(EnderEyeItem.class)
public abstract class Mixin_M_EnderEyeItem extends Item {

    public Mixin_M_EnderEyeItem(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        if (state.is(Blocks.END_PORTAL_FRAME) && !state.getValue(EndPortalFrameBlock.HAS_EYE)) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            BlockState newState = state.setValue(EndPortalFrameBlock.HAS_EYE, true);
            BlockPos pos = new BlockPos(x, y, z);
            Block.pushEntitiesUp(state, newState, level, pos);
            level.setBlock(pos, newState, 2);
            level.updateNeighbourForOutputSignal(pos, Blocks.END_PORTAL_FRAME);
            player.getItemInHand(hand).shrink(1);
            level.levelEvent_(LevelEvent.END_PORTAL_FRAME_FILL, x, y, z, 0);
            BlockPattern.BlockPatternMatch portal = EndPortalFrameBlock.getOrCreatePortalShape().find(level, pos);
            if (portal != null) {
                BlockPos frontTopLeft = portal.getFrontTopLeft();
                int portalX = frontTopLeft.getX() - 3;
                int portalY = frontTopLeft.getY();
                int portalZ = frontTopLeft.getZ() - 3;
                for (int dx = 0; dx < 3; ++dx) {
                    for (int dz = 0; dz < 3; ++dz) {
                        level.setBlock(new BlockPos(portalX + dx, portalY, portalZ + dz), Blocks.END_PORTAL.defaultBlockState(), 2);
                    }
                }
                level.globalLevelEvent_(LevelEvent.SOUND_END_PORTAL_SPAWN, portalX + 1, portalY, portalZ + 1, 0);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
