package tgw.evolution.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import tgw.evolution.inventory.SlotType;

public class EntityPlayerDummy extends RemoteClientPlayerEntity {

    private final byte model;

    public EntityPlayerDummy(ClientWorld world, GameProfile profile, NonNullList<ItemStack> equipment, byte model) {
        super(world, profile);
        this.model = model;
        for (EquipmentSlotType type : SlotType.SLOTS) {
            this.setItemSlot(type, equipment.get(type.ordinal()));
        }
    }

    @Override
    public HandSide getMainArm() {
        return (this.model & 1 << 7) != 0 ? HandSide.RIGHT : HandSide.LEFT;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return (this.model & part.getMask()) == part.getMask();
    }

    @Override
    public boolean isSpectator() {
        return false;
    }
}
