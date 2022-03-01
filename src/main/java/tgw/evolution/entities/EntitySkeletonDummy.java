package tgw.evolution.entities;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tgw.evolution.inventory.AdditionalSlotType;

public class EntitySkeletonDummy extends Skeleton {

    private final HumanoidArm mainArm;

    public EntitySkeletonDummy(Level level, NonNullList<ItemStack> equipment, HumanoidArm mainArm) {
        super(EntityType.SKELETON, level);
        for (EquipmentSlot type : AdditionalSlotType.SLOTS) {
            this.setItemSlot(type, equipment.get(type.ordinal()));
        }
        this.mainArm = mainArm;
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.mainArm;
    }
}
