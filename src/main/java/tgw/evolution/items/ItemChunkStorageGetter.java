package tgw.evolution.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;

public class ItemChunkStorageGetter extends ItemEv {

    private final EnumStorage element;

    public ItemChunkStorageGetter(Item.Properties properties, EnumStorage element) {
        super(properties);
        this.element = element;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            Chunk chunk = worldIn.getChunkAt(new BlockPos(playerIn));
            ChunkPos chunkPos = chunk.getPos();
            CapabilityChunkStorage.getChunkStorage(chunk).map(chunkStorages -> {
                playerIn.sendMessage(new TranslationTextComponent("Chunk " +
                                                                  chunkPos +
                                                                  " contains " +
                                                                  chunkStorages.getElementStored(this.element) +
                                                                  " " +
                                                                  this.element.getName()));
                return true;
            }).orElseGet(() -> {
                playerIn.sendMessage(new StringTextComponent("No chunk storage found for chunk " + chunkPos));
                return false;
            });
        }
        return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
