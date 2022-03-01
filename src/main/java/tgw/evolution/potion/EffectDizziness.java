package tgw.evolution.potion;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.util.math.MathHelper;

public class EffectDizziness extends MobEffect {

    private static final Int2IntMap AFFECTED = new Int2IntOpenHashMap();

    public EffectDizziness() {
        super(MobEffectCategory.HARMFUL, 0x3a_5785);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide) {
            return;
        }
        if (entity instanceof Player) {
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
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        AFFECTED.remove(entity.getId());
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
