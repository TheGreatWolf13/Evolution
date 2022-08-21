package tgw.evolution.potion;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.util.collection.ChanceEffectHolder;
import tgw.evolution.util.collection.EffectHolder;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

public class EffectDehydration extends EffectGeneric {

    private OList<EffectHolder> causes;
    private OList<ChanceEffectHolder> mayCause;

    public EffectDehydration() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    @Override
    @NotNull
    public ObjectList<EffectHolder> causesEffect() {
        if (this.causes == null) {
            this.causes = new OArrayList<>();
            this.causes.add(new EffectHolder(1, MobEffects.MOVEMENT_SLOWDOWN,
                                             a -> EvolutionEffects.infiniteOf(MobEffects.MOVEMENT_SLOWDOWN, a - 1, true, false, false)));
            this.causes.trimCollection();
        }
        return this.causes;
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
    @NotNull
    public ObjectList<ChanceEffectHolder> mayCauseEffect() {
        if (this.mayCause == null) {
            this.mayCause = new OArrayList<>();
            this.mayCause.add(new ChanceEffectHolder(0, EvolutionEffects.DIZZINESS.get(), a -> 0.05f * (a + 1),
                                                     a -> new MobEffectInstance(EvolutionEffects.DIZZINESS.get(), 400 * (a + 1), a, true, false,
                                                                                false)));
            this.mayCause.trimCollection();
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
