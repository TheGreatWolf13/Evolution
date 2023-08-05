package tgw.evolution.patches.obj;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public interface IBlockEntityTagOutput {

    void accept(int x, int y, int z, BlockEntityType<?> type, @Nullable CompoundTag tag);
}
