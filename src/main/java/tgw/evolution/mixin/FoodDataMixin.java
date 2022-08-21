package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    /**
     * @author TheGreatWolf
     * @reason Do nothing, since we handle our own hunger system.
     */
    @Overwrite
    public void addAdditionalSaveData(CompoundTag tag) {
    }

    /**
     * @author TheGreatWolf
     * @reason Do nothing, since we handle our own hunger system.
     */
    @Overwrite
    public void readAdditionalSaveData(CompoundTag tag) {
    }

    /**
     * @author TheGreatWolf
     * @reason Do nothing, since we handle our own hunger system.
     */
    @Overwrite
    public void tick(Player player) {
    }
}
