package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Interface present in all {@code Item}s from the Mod to control tooltips and other properties.
 */
public interface IEvolutionItem {

    default float getRenderOffsetX() {
        return 0;
    }

    default float getRenderOffsetY() {
        return 0;
    }

    default float getRenderOffsetZ() {
        return 0;
    }

    default int getTooltipLines() {
        return 0;
    }

    boolean isCorrectToolForDrops(ItemStack stack, BlockState state, @Nullable Level level, @Nullable BlockPos pos);

    /**
     * @return Whether this item should prevent the player from sprinting and cancel the sprinting if it's being used.
     */
    default boolean useItemPreventsSprinting() {
        return false;
    }

    /**
     * @return The slow down rate of this item when it's being used. {@code 1.0} means no slow down, while {@code 0.0} means full totally stopped.
     */
    default double useItemSlowDownRate() {
        return 1.0;
    }

    default boolean usesModularRendering() {
        return false;
    }
}
