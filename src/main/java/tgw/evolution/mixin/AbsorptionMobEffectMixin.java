package tgw.evolution.mixin;

import net.minecraft.world.effect.AbsoptionMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.patches.ILivingEntityPatch;

@Mixin(AbsoptionMobEffect.class)
public abstract class AbsorptionMobEffectMixin extends MobEffect {

    public AbsorptionMobEffectMixin(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
    }

    /**
     * @author TheGreatWolf
     * @reason Make absorption mechanic consistent
     */
    @Override
    @Overwrite
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int lvl) {
        ((ILivingEntityPatch) entity).addAbsorptionSuggestion(4 * (lvl + 1));
        super.addAttributeModifiers(entity, attributes, lvl);
    }

    /**
     * @author TheGreatWolf
     * @reason Make absorption mechanic consistent
     */
    @Override
    @Overwrite
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int lvl) {
        ((ILivingEntityPatch) entity).removeAbsorptionSuggestion(4 * (lvl + 1));
        super.removeAttributeModifiers(entity, attributes, lvl);
    }
}
