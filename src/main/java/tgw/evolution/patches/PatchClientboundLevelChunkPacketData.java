package tgw.evolution.patches;

import tgw.evolution.patches.obj.IBlockEntityTagOutput;

import java.util.function.Consumer;

public interface PatchClientboundLevelChunkPacketData {

    default Consumer<IBlockEntityTagOutput> getBlockEntitiesTagsConsumer_(int i, int j) {
        throw new AbstractMethodError();
    }
}
