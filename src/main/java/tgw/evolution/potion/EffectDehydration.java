package tgw.evolution.potion;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.EffectHolder;
import tgw.evolution.util.collection.lists.OList;

public class EffectDehydration extends EffectGeneric {

    private @Nullable OList<EffectHolder> causes;
    private @Nullable OList<ChanceEffectHolder> mayCause;

    public EffectDehydration() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    @Override
    public OList<EffectHolder> causesEffect() {
        if (this.causes == null) {
            this.causes = OList.of(new EffectHolder(1, MobEffects.MOVEMENT_SLOWDOWN, a -> EvolutionEffects.infiniteOf(MobEffects.MOVEMENT_SLOWDOWN, a - 1, true, false, false)));
        }
        return this.causes.view();
    }

    @Override
    public int customDescriptionUntil() {
        return 2;
    }

    @Override
    public boolean disablesNaturalRegen() {
        return true;
    }

    @Override
    public boolean disablesSprint() {
        return true;
    }

    @Override
    public DamageSource dmgSource() {
        return EvolutionDamage.DEHYDRATION;
    }

    @Override
    public OList<ChanceEffectHolder> mayCauseEffect() {
        if (this.mayCause == null) {
            this.mayCause = OList.of(new ChanceEffectHolder(0, EvolutionEffects.DIZZINESS, a -> 0.05f * (a + 1), a -> new MobEffectInstance(EvolutionEffects.DIZZINESS, 400 * (a + 1), a, true, false, false)));
        }
        return this.mayCause;
    }

    @Override
    public float regen(int lvl) {
        return lvl >= 2 ? -1 : 0;
    }

    @Override
    public boolean shouldHurt(int lvl) {
        return lvl >= 2;
    }

    @Override
    public float staminaMod() {
        return 0.1f;
    }

    @Override
    public int tickInterval(int lvl) {
        if (lvl <= 2) {
            return 80;
        }
        return Math.max(80 >> lvl - 2, 1);
    }
}
