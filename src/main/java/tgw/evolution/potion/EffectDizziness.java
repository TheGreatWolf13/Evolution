package tgw.evolution.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.WeakHashMap;

public class EffectDizziness extends Effect {

    private static final Map<LivingEntity, Pair<Integer, Vec3d>> AFFECTED = new WeakHashMap<>();
    public static int tick = 0;
    public static Vec3d lastMotion = Vec3d.ZERO;

    public EffectDizziness() {
        super(EffectType.HARMFUL, 0x3a5785);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        if (entity.world.isRemote) {
            if (entity instanceof PlayerEntity) {
                Vec3d orthogonal = Vec3d.ZERO;
                if (entity.onGround) {
                    float amplitude = (float) (2 * Math.PI * 0.8f / (160 >> amplifier) * MathHelper.cos((float) (tick * 2 * Math.PI / (160 >> amplifier))));
                    Vec3d motion = entity.getMotion();
                    orthogonal = new Vec3d(motion.z - lastMotion.z, 0, -motion.x + lastMotion.x).normalize().mul(amplitude, 0, amplitude);
                    entity.setMotion(motion.x + orthogonal.x, motion.y, motion.z + orthogonal.z);
                }
                ++tick;
                lastMotion = orthogonal;
            }
            return;
        }
        if (entity instanceof PlayerEntity) {
            return;
        }
        int tick = 0;
        Vec3d lastMotion = Vec3d.ZERO;
        if (AFFECTED.containsKey(entity)) {
            tick = AFFECTED.get(entity).getLeft();
            lastMotion = AFFECTED.get(entity).getRight();
        }
        else {
            AFFECTED.put(entity, Pair.of(tick, Vec3d.ZERO));
        }
        Vec3d orthogonal = Vec3d.ZERO;
        if (entity.onGround) {
            float amplitude = (float) (2 * Math.PI * 0.8f / (160 >> amplifier) * MathHelper.cos((float) (tick * 2 * Math.PI / (160 >> amplifier))));
            Vec3d motion = entity.getMotion();
            orthogonal = new Vec3d(motion.z - lastMotion.z, 0, -motion.x + lastMotion.x).normalize().mul(amplitude, 0, amplitude);
            entity.setMotion(motion.x + orthogonal.x, motion.y, motion.z + orthogonal.z);
        }
        AFFECTED.put(entity, Pair.of(++tick, orthogonal));
    }

    @Override
    public void removeAttributesModifiersFromEntity(LivingEntity entity, AbstractAttributeMap attributes, int amplifier) {
        AFFECTED.remove(entity);
    }
}
