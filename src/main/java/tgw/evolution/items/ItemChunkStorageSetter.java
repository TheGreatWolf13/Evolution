package tgw.evolution.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;

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
        Chunk chunk = world.getChunkAt(player.blockPosition());
        ChunkPos chunkPos = chunk.getPos();
        IChunkStorage chunkStorage = chunk.getCapability(CapabilityChunkStorage.INSTANCE).orElseThrow(IllegalStateException::new);
        if (player.isCrouching()) {
            if (chunkStorage.removeElement(this.element, amount)) {
                player.displayClientMessage(new StringTextComponent("Removed " +
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
            player.displayClientMessage(new StringTextComponent("Added " +
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
        World world = entity.level;
        if (!world.isClientSide && entity instanceof PlayerEntity) {
            this.addRemoveChunkStorage(world, (PlayerEntity) entity, 100);
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClientSide) {
            this.addRemoveChunkStorage(world, player, 1);
        }
        return new ActionResult<>(ActionResultType.CONSUME, player.getItemInHand(hand));
    }
}
