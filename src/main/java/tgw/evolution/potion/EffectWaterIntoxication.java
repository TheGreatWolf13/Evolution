package tgw.evolution.potion;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public class EffectWaterIntoxication extends EffectGeneric {

    private @Nullable OList<ChanceEffectHolder> mayCause;

    public EffectWaterIntoxication() {
        super(MobEffectCategory.HARMFUL, 0x12_6eff);
    }

    @Override
    public int customDescriptionUntil() {
        return 1;
    }

    @Override
    public DamageSource dmgSource() {
        return EvolutionDamage.WATER_INTOXICATION;
    }

    @Override
    public OList<ChanceEffectHolder> mayCauseEffect() {
        if (this.mayCause == null) {
            this.mayCause = new OArrayList<>();
            this.mayCause.add(new ChanceEffectHolder(0, MobEffects.CONFUSION, a -> a > 0 ? 0.15f * a : 0.001f,
                                                     a -> new MobEffectInstance(MobEffects.CONFUSION, 200 * (a + 1), 0, true, false, false)));
            this.mayCause.add(new ChanceEffectHolder(0, MobEffects.WEAKNESS, a -> a > 0 ? 0.15f * a : 0.001f,
                                                     a -> new MobEffectInstance(MobEffects.WEAKNESS, 200 * (a + 1), a, true, false, false)));
            this.mayCause.trimCollection();
        }
        return this.mayCause;
    }

    @Override
    public float regen(int lvl) {
        if (lvl > 0) {
            return -lvl;
        }
        return 0;
    }

    @Override
    public int tickInterval(int lvl) {
        if (lvl > 0) {
            return 40;
        }
        return 1;
    }
}
