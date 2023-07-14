package tgw.evolution.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.inventory.AdditionalSlotType;

public class EntityPlayerDummy extends RemotePlayer {

    private final byte model;

    public EntityPlayerDummy(ClientLevel world, GameProfile profile, NonNullList<ItemStack> equipment, byte model) {
        super(world, profile);
        this.model = model;
        for (EquipmentSlot type : AdditionalSlotType.SLOTS) {
            this.setItemSlot(type, equipment.get(type.ordinal()));
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return (this.model & 1 << 7) != 0 ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
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
