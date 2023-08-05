package tgw.evolution.items;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionCreativeTabs;
import tgw.evolution.util.math.MathHelper;

public class ItemClayMolded extends ItemBlock {

    public final boolean single;

    public ItemClayMolded(Block block) {
        this(block, false);
    }

    public ItemClayMolded(Block block, boolean single) {
        super(block, new Properties().tab(EvolutionCreativeTabs.MISC));
        this.single = single;
    }

    @Override
    public InteractionResult place(Level level,
                                   int x,
                                   int y,
                                   int z,
                                   Player player,
                                   InteractionHand hand,
                                   BlockHitResult hitResult,
                                   boolean canPlace) {
        if (player.isSecondaryUseActive()) {
            if (!canPlace) {
                return InteractionResult.FAIL;
            }
            if (!BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP)) {
                return InteractionResult.FAIL;
            }
            if (!level.isUnobstructed_(EvolutionBlocks.PIT_KILN.defaultBlockState(), x, y, z, player)) {
                return InteractionResult.FAIL;
            }
            if (!this.placeBlock(level, x, y, z, EvolutionBlocks.PIT_KILN.defaultBlockState())) {
                return InteractionResult.FAIL;
            }
            if (level.getBlockEntity_(x, y, z) instanceof TEPitKiln tile) {
                if (this.single) {
                    tile.setNWStack(player.getItemInHand(hand));
                    tile.setSingle(true);
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                    return InteractionResult.SUCCESS;
                }
                int hitX = MathHelper.getIndex(2, 0, 16, (hitResult.x() - x) * 16);
                int hitZ = MathHelper.getIndex(2, 0, 16, (hitResult.z() - z) * 16);
                if (hitX == 0) {
                    if (hitZ == 0) {
                        tile.setNWStack(player.getItemInHand(hand));
                        level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                        return InteractionResult.SUCCESS;
                    }
                    tile.setSWStack(player.getItemInHand(hand));
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                    return InteractionResult.SUCCESS;
                }
                if (hitZ == 0) {
                    tile.setNEStack(player.getItemInHand(hand));
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                    return InteractionResult.SUCCESS;
                }
                tile.setSEStack(player.getItemInHand(hand));
                level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                return InteractionResult.SUCCESS;
            }
        }
        return super.place(level, x, y, z, player, hand, hitResult, canPlace);
    }
}
