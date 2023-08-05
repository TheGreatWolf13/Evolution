package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Optional;

@Mixin(AxeItem.class)
public abstract class Mixin_M_AxeItem extends DiggerItem {

    public Mixin_M_AxeItem(float f,
                           float g,
                           Tier tier,
                           TagKey<Block> tagKey,
                           Properties properties) {
        super(f, g, tier, tagKey, properties);
    }

    @Shadow
    protected abstract Optional<BlockState> getStripped(BlockState blockState);

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        Optional<BlockState> stripped = this.getStripped(state);
        BlockState chosenState = null;
        if (stripped.isPresent()) {
            level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            chosenState = stripped.get();
        }
        else {
            Optional<BlockState> copper = WeatheringCopper.getPrevious(state);
            if (copper.isPresent()) {
                level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, new BlockPos(x, y, z), 0);
                chosenState = copper.get();
            }
            else {
                BlockState waxedState = null;
                Block waxedBlock = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock());
                if (waxedBlock != null) {
                    waxedState = waxedBlock.withPropertiesOf(state);
                }
                if (waxedState != null) {
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, new BlockPos(x, y, z), 0);
                    chosenState = waxedState;
                }
            }
        }
        ItemStack stack = player.getItemInHand(hand);
        if (chosenState != null) {
            if (player instanceof ServerPlayer p) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger_(p, x, y, z, stack);
            }
            level.setBlock(new BlockPos(x, y, z), chosenState, 11);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
