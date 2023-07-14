package tgw.evolution.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.util.collection.O2FPair;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public interface IConsumable {

    /**
     * @return The time to consume the item, in ticks.
     */
    int getConsumeTime();

    /**
     * @return A list made of pairs. Each pair contains an effect this item can apply paired with its chance, in float.
     */
    OList<O2FPair<MobEffectInstance>> getEffects();

    /**
     * @return The animation to use while consuming.
     */
    UseAnim getUseAnimation();

    abstract class Properties {

        private final UseAnim anim;
        private int consumeTime = 32;
        private OList<O2FPair<MobEffectInstance>> effects = OList.emptyList();

        public Properties(UseAnim anim) {
            this.anim = anim;
        }

        protected void consumeTimeInternal(int consumeTime) {
            this.consumeTime = consumeTime;
        }

        protected void effectInternal(MobEffectInstance effect, float chance) {
            if (this.effects == OList.<O2FPair<MobEffectInstance>>emptyList()) {
                this.effects = new OArrayList<>();
            }
            this.effects.add(O2FPair.of(effect, chance));
        }

        public UseAnim getAnim() {
            return this.anim;
        }

        public int getConsumeTime() {
            return this.consumeTime;
        }

        public OList<O2FPair<MobEffectInstance>> getEffects() {
            return this.effects;
        }
    }

    class DrinkProperties extends Properties {

        private final int thirst;

        public DrinkProperties(int thirst) {
            super(UseAnim.DRINK);
            this.thirst = thirst;
        }

        public DrinkProperties consumeTime(int consumeTime) {
            this.consumeTimeInternal(consumeTime);
            return this;
        }

        public DrinkProperties effect(MobEffectInstance effect, float chance) {
            this.effectInternal(effect, chance);
            return this;
        }

        public int getThirst() {
            return this.thirst;
        }
    }

    class FoodProperties extends Properties {

        private final int hunger;

        public FoodProperties(int hunger) {
            super(UseAnim.EAT);
            this.hunger = hunger;
        }

        public FoodProperties consumeTime(int consumeTime) {
            this.consumeTimeInternal(consumeTime);
            return this;
        }

        public FoodProperties effect(MobEffectInstance effect, float chance) {
            this.effectInternal(effect, chance);
            return this;
        }

        public int getHunger() {
            return this.hunger;
        }
    }
}
