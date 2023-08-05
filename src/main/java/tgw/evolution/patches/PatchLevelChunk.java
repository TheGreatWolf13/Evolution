package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.chunk.CapabilityChunkStorage;
import tgw.evolution.patches.obj.IBlockEntityTagOutput;

import java.util.function.Consumer;

public interface PatchLevelChunk {

    default @Nullable BlockEntity getBlockEntity_(int x, int y, int z, LevelChunk.EntityCreationType creationType) {
        throw new AbstractMethodError();
    }

    default CapabilityChunkStorage getChunkStorage() {
        throw new AbstractMethodError();
    }

    default void replaceWithPacketData_(FriendlyByteBuf buf, CompoundTag tag, Consumer<IBlockEntityTagOutput> consumer) {
        throw new AbstractMethodError();
    }
}
