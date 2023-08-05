package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Map;

@Mixin(ShovelItem.class)
public abstract class Mixin_M_ShovelItem extends DiggerItem {

    @Shadow @Final protected static Map<Block, BlockState> FLATTENABLES;

    public Mixin_M_ShovelItem(float f,
                              float g,
                              Tier tier,
                              TagKey<Block> tagKey,
                              Properties properties) {
        super(f, g, tier, tagKey, properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        if (hitResult.getDirection() == Direction.DOWN) {
            return InteractionResult.PASS;
        }
        BlockState flattenable = FLATTENABLES.get(state.getBlock());
        BlockState resultState = null;
        if (flattenable != null && level.getBlockState_(x, y + 1, z).isAir()) {
            level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            resultState = flattenable;
        }
        else if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            if (!level.isClientSide()) {
                level.levelEvent_(LevelEvent.SOUND_EXTINGUISH_FIRE, x, y, z, 0);
            }
            CampfireBlock.dowse(player, level, new BlockPos(x, y, z), state);
            resultState = state.setValue(CampfireBlock.LIT, false);
        }
        if (resultState != null) {
            if (!level.isClientSide) {
                level.setBlock(new BlockPos(x, y, z), resultState, 11);
                player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
