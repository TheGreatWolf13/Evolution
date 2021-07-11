package tgw.evolution.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import tgw.evolution.util.MathHelper;

import java.util.Map;
import java.util.WeakHashMap;

public class EffectDizziness extends Effect {

    private static final Map<LivingEntity, Integer> AFFECTED = new WeakHashMap<>();
    public static int tick;

    public EffectDizziness() {
        super(EffectType.HARMFUL, 0x3a_5785);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        if (entity.world.isRemote) {
            return;
        }
        if (entity instanceof PlayerEntity) {
            return;
        }
        int tick = 0;
        if (AFFECTED.containsKey(entity)) {
            tick = AFFECTED.get(entity);
        }
        else {
            AFFECTED.put(entity, tick);
        }
        entity.moveStrafing = Math.signum(MathHelper.cos(tick * MathHelper.TAU / (80 >> amplifier)));
        entity.setSprinting(false);
        AFFECTED.put(entity, ++tick);
    }

    @Override
    public void removeAttributesModifiersFromEntity(LivingEntity entity, AbstractAttributeMap attributes, int amplifier) {
        AFFECTED.remove(entity);
    }
}
