package tgw.evolution.inventory;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import tgw.evolution.inventory.extendedinventory.ContainerExtendedHandler;
import tgw.evolution.inventory.extendedinventory.IExtendedItemHandler;

import java.util.HashMap;
import java.util.Map;

public class SlotExtended extends SlotItemHandler {

    public static final String[] TEXTURES = {"hat", "body", "legs", "feet", "cloak", "mask", "back", "tactical"};
    private static AtlasSpriteHolder sprites;
    private final PlayerEntity player;
    private final int slot;

    public SlotExtended(PlayerEntity player, IItemHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.player = player;
        this.slot = index;
        this.backgroundLocation = new ResourceLocation("evolution:textures/item/slot_" + TEXTURES[this.slot] + ".png");
        if (this.player.world.isRemote && sprites == null) {
            sprites = new AtlasSpriteHolder();
        }
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        if (this.isBlocked(playerIn)) {
            return false;
        }
        return super.canTakeStack(playerIn);
    }

    @Override
    public TextureAtlasSprite getBackgroundSprite() {
        return sprites != null ? sprites.getSpriteForString("evolution:textures/item/slot_" + TEXTURES[this.slot] + ".png") : null;
    }

    private boolean isBlocked(PlayerEntity player) {
        switch (this.slot) {
            case ContainerExtendedHandler.HAT:
                return !player.inventory.armorInventory.get(3).isEmpty();
            case ContainerExtendedHandler.BODY:
                return !player.inventory.armorInventory.get(2).isEmpty();
            case ContainerExtendedHandler.LEGS:
                return !player.inventory.armorInventory.get(1).isEmpty();
            case ContainerExtendedHandler.FEET:
                return !player.inventory.armorInventory.get(0).isEmpty();
        }
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        switch (this.slot) {
            case ContainerExtendedHandler.HAT:
                if (!this.player.inventory.armorInventory.get(3).isEmpty()) {
                    return false;
                }
                break;
            case ContainerExtendedHandler.BODY:
                if (!this.player.inventory.armorInventory.get(2).isEmpty()) {
                    return false;
                }
                break;
            case ContainerExtendedHandler.LEGS:
                if (!this.player.inventory.armorInventory.get(1).isEmpty()) {
                    return false;
                }
                break;
            case ContainerExtendedHandler.FEET:
                if (!this.player.inventory.armorInventory.get(0).isEmpty()) {
                    return false;
                }
                break;
        }
        return ((IExtendedItemHandler) this.getItemHandler()).isItemValidForSlot(this.slot, stack, this.player);
    }

    final class AtlasSpriteHolder {

        private final Map<String, TextureAtlasSprite> spriteMap = new HashMap<>();

        TextureAtlasSprite getSpriteForString(String id) {
            return this.spriteMap.computeIfAbsent(id, key -> new TextureAtlasSprite(SlotExtended.this.backgroundLocation, 16, 16) {
                {
                    this.func_217789_a(16, 16, 0, 0);
                }
            });
        }
    }
}
