package tgw.evolution.potion;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import tgw.evolution.util.MathHelper;

public class EffectDizziness extends Effect {

    private static final Int2IntMap AFFECTED = new Int2IntOpenHashMap();

    public EffectDizziness() {
        super(EffectType.HARMFUL, 0x3a_5785);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide) {
            return;
        }
        if (entity instanceof PlayerEntity) {
            return;
        }
        int tick = AFFECTED.getOrDefault(entity.getId(), 0);
        entity.zza = Math.signum(MathHelper.cos(tick * MathHelper.TAU / (80 >> amplifier)));
        entity.setSprinting(false);
        AFFECTED.put(entity.getId(), ++tick);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        AFFECTED.remove(entity.getId());
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
