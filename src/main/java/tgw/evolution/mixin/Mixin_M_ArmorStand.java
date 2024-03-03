package tgw.evolution.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(ArmorStand.class)
public abstract class Mixin_M_ArmorStand extends LivingEntity {

    public Mixin_M_ArmorStand(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private EquipmentSlot getClickedSlot(Vec3 vec3) {
        throw new AbstractMethodError();
    }

    @Unique
    private EquipmentSlot getClickedSlot(double y) {
        boolean small = this.isSmall();
        if (small) {
            y *= 2;
        }
        if (y >= 0.1 && y < 0.1 + (small ? 0.8 : 0.45) && this.hasItemInSlot(EquipmentSlot.FEET)) {
            return EquipmentSlot.FEET;
        }
        if (y >= 0.9 + (small ? 0.3 : 0) && y < 0.9 + (small ? 1 : 0.7) && this.hasItemInSlot(EquipmentSlot.CHEST)) {
            return EquipmentSlot.CHEST;
        }
        if (y >= 0.4 && y < 0.4 + (small ? 1 : 0.8) && this.hasItemInSlot(EquipmentSlot.LEGS)) {
            return EquipmentSlot.LEGS;
        }
        if (y >= 1.6 && this.hasItemInSlot(EquipmentSlot.HEAD)) {
            return EquipmentSlot.HEAD;
        }
        if (!this.hasItemInSlot(EquipmentSlot.MAINHAND) && this.hasItemInSlot(EquipmentSlot.OFFHAND)) {
            return EquipmentSlot.OFFHAND;
        }
        return EquipmentSlot.MAINHAND;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionHand) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult interactAt_(Player player, double hitX, double hitY, double hitZ, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isMarker() && !stack.is(Items.NAME_TAG)) {
            if (player.isSpectator()) {
                return InteractionResult.SUCCESS;
            }
            if (player.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
            EquipmentSlot desiredSlot = Mob.getEquipmentSlotForItem(stack);
            if (stack.isEmpty()) {
                EquipmentSlot clickedSlot = this.getClickedSlot(hitY);
                EquipmentSlot equipmentSlot3 = this.isDisabled(clickedSlot) ? desiredSlot : clickedSlot;
                if (this.hasItemInSlot(equipmentSlot3) && this.swapItem(player, equipmentSlot3, stack, hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
            else {
                if (this.isDisabled(desiredSlot)) {
                    return InteractionResult.FAIL;
                }
                if (desiredSlot.getType() == EquipmentSlot.Type.HAND && !this.isShowArms()) {
                    return InteractionResult.FAIL;
                }
                if (this.swapItem(player, desiredSlot, stack, hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Shadow
    protected abstract boolean isDisabled(EquipmentSlot equipmentSlot);

    @Shadow
    public abstract boolean isMarker();

    @Shadow
    public abstract boolean isShowArms();

    @Shadow
    public abstract boolean isSmall();

    @Shadow
    protected abstract boolean swapItem(Player player, EquipmentSlot equipmentSlot, ItemStack itemStack, InteractionHand interactionHand);
}
