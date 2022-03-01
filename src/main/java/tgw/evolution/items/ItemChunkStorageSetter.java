package tgw.evolution.items;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;

public class ItemChunkStorageSetter extends ItemEv {

    private final EnumStorage element;

    public ItemChunkStorageSetter(Properties properties, EnumStorage element) {
        super(properties);
        this.element = element;
    }

    private void addRemoveChunkStorage(Level level, Player player, int amount) {
        LevelChunk chunk = level.getChunkAt(player.blockPosition());
        ChunkPos chunkPos = chunk.getPos();
        IChunkStorage chunkStorage = chunk.getCapability(CapabilityChunkStorage.INSTANCE).orElseThrow(IllegalStateException::new);
        if (player.isCrouching()) {
            if (chunkStorage.removeElement(this.element, amount)) {
                player.displayClientMessage(new TextComponent("Removed " +
                                                              amount +
                                                              " " +
                                                              this.element.getName() +
                                                              " from chunk " +
                                                              chunkPos +
                                                              ": " +
                                                              chunkStorage.getElementStored(this.element)), false);
            }
        }
        else {
            int elementAdded = chunkStorage.addElement(this.element, amount);
            player.displayClientMessage(new TextComponent("Added " +
                                                          elementAdded +
                                                          " " +
                                                          this.element.getName() +
                                                          " to chunk " +
                                                          chunkPos +
                                                          ": " +
                                                          chunkStorage.getElementStored(this.element)), false);
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        Level level = entity.level;
        if (!level.isClientSide && entity instanceof Player player) {
            this.addRemoveChunkStorage(level, player, 100);
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            this.addRemoveChunkStorage(level, player, 1);
        }
        return new InteractionResultHolder<>(InteractionResult.CONSUME, player.getItemInHand(hand));
    }
}
