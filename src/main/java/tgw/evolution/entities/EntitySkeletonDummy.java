package tgw.evolution.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class EntitySkeletonDummy extends SkeletonEntity {

    private final HandSide mainArm;

    public EntitySkeletonDummy(World world, NonNullList<ItemStack> equipment, HandSide mainArm) {
        super(EntityType.SKELETON, world);
        for (EquipmentSlotType type : EquipmentSlotType.values()) {
            this.setItemSlot(type, equipment.get(type.ordinal()));
        }
        this.mainArm = mainArm;
    }

    @Override
    public HandSide getMainArm() {
        return this.mainArm;
    }
}
