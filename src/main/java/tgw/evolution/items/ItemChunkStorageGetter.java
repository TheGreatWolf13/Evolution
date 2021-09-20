package tgw.evolution.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;

public class ItemChunkStorageGetter extends ItemEv {

    private final EnumStorage element;

    public ItemChunkStorageGetter(Item.Properties properties, EnumStorage element) {
        super(properties);
        this.element = element;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClientSide) {
            Chunk chunk = world.getChunkAt(player.blockPosition());
            ChunkPos chunkPos = chunk.getPos();
            IChunkStorage chunkStorage = chunk.getCapability(CapabilityChunkStorage.INSTANCE).orElseThrow(IllegalStateException::new);
            player.displayClientMessage(new TranslationTextComponent("Chunk " +
                                                                     chunkPos +
                                                                     " contains " +
                                                                     chunkStorage.getElementStored(this.element) +
                                                                     " " +
                                                                     this.element.getName()), false);
        }
        return new ActionResult<>(ActionResultType.CONSUME, player.getItemInHand(hand));
    }
}
