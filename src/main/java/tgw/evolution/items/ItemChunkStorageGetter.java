package tgw.evolution.items;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;
import tgw.evolution.init.EvolutionCapabilities;

public class ItemChunkStorageGetter extends ItemEv {

    private final EnumStorage element;

    public ItemChunkStorageGetter(Properties properties, EnumStorage element) {
        super(properties);
        this.element = element;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            LevelChunk chunk = level.getChunkAt(player.blockPosition());
            ChunkPos chunkPos = chunk.getPos();
            IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(chunk, CapabilityChunkStorage.INSTANCE);
            player.displayClientMessage(new TranslatableComponent("Chunk " +
                                                                  chunkPos +
                                                                  " contains " +
                                                                  chunkStorage.getElementStored(this.element) +
                                                                  " " +
                                                                  this.element.getName()), false);
        }
        return new InteractionResultHolder<>(InteractionResult.CONSUME, player.getItemInHand(hand));
    }
}
