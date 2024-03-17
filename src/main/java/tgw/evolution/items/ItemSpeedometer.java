package tgw.evolution.items;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.util.math.Metric;
import tgw.evolution.util.physics.SI;

public class ItemSpeedometer extends ItemGeneric {

    public ItemSpeedometer(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide && 0 <= slotId && slotId < 9 && entity instanceof Player player) {
            Vec3 velocity = player.getDeltaMovement();
            double velX = velocity.x / (SI.METER / SI.SECOND);
            double velY = velocity.y / (SI.METER / SI.SECOND);
            double velZ = velocity.z / (SI.METER / SI.SECOND);
            double velXSqr = velX * velX;
            double velZSqr = velZ * velZ;
            double horizSpeed = Math.sqrt(velXSqr + velZSqr);
            double speed = Math.sqrt(velXSqr + velY * velY + velZSqr);
            player.displayClientMessage(new TextComponent("vx = " +
                                                          Metric.THREE_PLACES_FULL.format(velX) +
                                                          "; vy = " +
                                                          Metric.THREE_PLACES_FULL.format(velY) +
                                                          "; vz = " +
                                                          Metric.THREE_PLACES_FULL.format(velZ) +
                                                          "; sh = " +
                                                          Metric.THREE_PLACES_FULL.format(horizSpeed) +
                                                          "; s = " +
                                                          Metric.THREE_PLACES_FULL.format(speed) + " [m/s]"), true);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
}
