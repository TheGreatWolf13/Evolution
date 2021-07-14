package tgw.evolution.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;

public class ItemChunkStorageSetter extends ItemEv {

    private final EnumStorage element;

    public ItemChunkStorageSetter(Item.Properties properties, EnumStorage element) {
        super(properties);
        this.element = element;
    }

    /**
     * Add or remove chunk nutrient from the player's current chunk.
     *
     * @param world  The World
     * @param player The player
     * @param amount The amount to add/remove
     */
    private void addRemoveChunkStorage(World world, PlayerEntity player, int amount) {
        Chunk chunk = world.getChunkAt(new BlockPos(player));
        ChunkPos chunkPos = chunk.getPos();
        CapabilityChunkStorage.getChunkStorage(chunk).map(chunkStorages -> {
            if (player.isSneaking()) {
                boolean elementRemoved = chunkStorages.removeElement(this.element, amount);
                if (elementRemoved) {
                    player.sendMessage(new StringTextComponent("Removed " +
                                                               amount +
                                                               " " +
                                                               this.element.getName() +
                                                               " from chunk " +
                                                               chunkPos +
                                                               ": " +
                                                               chunkStorages.getElementStored(this.element)));
                }
            }
            else {
                int elementAdded = chunkStorages.addElement(this.element, amount);
                player.sendMessage(new StringTextComponent("Added " +
                                                           elementAdded +
                                                           " " +
                                                           this.element.getName() +
                                                           " to chunk " +
                                                           chunkPos +
                                                           ": " +
                                                           chunkStorages.getElementStored(this.element)));
            }
            return true;
        }).orElseGet(() -> {
            player.sendMessage(new StringTextComponent("No chunk storage found for chunk " + chunkPos));
            return false;
        });
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        World world = entity.getEntityWorld();
        if (!world.isRemote && entity instanceof PlayerEntity) {
            this.addRemoveChunkStorage(world, (PlayerEntity) entity, 100);
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            this.addRemoveChunkStorage(worldIn, playerIn, 1);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
